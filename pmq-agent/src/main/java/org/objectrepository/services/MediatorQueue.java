package org.objectrepository.services;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import org.apache.camel.ConsumerTemplate;
import org.apache.camel.ProducerTemplate;
import org.apache.commons.exec.*;
import org.apache.log4j.Logger;
import org.objectrepository.instruction.InstructionType;
import org.objectrepository.util.InstructionTypeHelper;
import org.objectrepository.util.Queue;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.io.ByteArrayOutputStream;
import java.util.Date;
import java.util.Timer;

/**
 * MediatorQueue
 * <p/>
 * Listens to a queue; accepts the message and parses the message into command line parameters.
 *
 * @author Lucien van Wouw <lwo@iisg.nl>
 * @author Jozsef Gabor Bone <bonej@ceu.hu>
 */
public class MediatorQueue implements Runnable {

    final private static Logger log = Logger.getLogger(MediatorQueue.class);
    private static final int StatusCodeTaskReceipt = 400;
    private static final int StatusCodeTaskComplete = 500;
    private static final int StatusCodeTaskError = 500;

    private MongoTemplate mongoTemplate;
    private ConsumerTemplate consumer;
    private String messageQueue;
    private String shellScript;
    private long period;
    private ProducerTemplate producer;

    public MediatorQueue(MongoTemplate mongoTemplate, ConsumerTemplate consumer, ProducerTemplate producer, String messageQueue, String shellScript, long period) {
        this.mongoTemplate = mongoTemplate;
        this.consumer = consumer;
        this.producer = producer;
        this.messageQueue = messageQueue;
        this.shellScript = shellScript;
        this.period = period;
    }

    @Override
    public void run() {

        log.debug("Start listening to " + messageQueue);
        String message = consumer.receiveBody(messageQueue, 300000, String.class);
        if (message == null) return;
        log.debug("Message received from " + messageQueue + " : " + message);

        InstructionType instructionType;
        try {
            instructionType = InstructionTypeHelper.stringToInstructionType(message);
            instructionType.setLabel(null);
        } catch (Exception e) {
            log.warn(e);
            return;
        }

        final String identifier = instructionType.getWorkflow().get(0).getIdentifier();
        final DBObject query = new BasicDBObject("workflow.identifier", identifier);
        final String collectionName = Queue.getCollectionName(messageQueue);
        if (mongoTemplate.getCollection(collectionName).findOne(query) == null) {
            log.warn("Ignoring message because it's task(identifier=" + identifier + ") no longer not exist in the collection " + collectionName);
            return;
        }

        log.info("Message received: " + identifier);
        HeartBeats.message(mongoTemplate, messageQueue, StatusCodeTaskReceipt, "Task received", identifier, 0);

        if (log.isDebugEnabled()) {
            log.debug("InstructionType as json: " + InstructionTypeHelper.instructionTypeToJson(instructionType));
        }

        final long start = new Date().getTime();
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new HeartBeat(mongoTemplate, messageQueue, StatusCodeTaskReceipt, identifier, start), period, period);


        DefaultExecutor executor = new DefaultExecutor();
        //Using Std out for the output/error stream - to do it later...
        //http://stackoverflow.com/questions/621596/how-would-you-read-image-data-in-from-a-program-like-image-magick-in-java
        ByteArrayOutputStream stdout = new ByteArrayOutputStream();
        executor.setStreamHandler(new PumpStreamHandler(stdout));
        executor.setWatchdog(new ExecuteWatchdog(ExecuteWatchdog.INFINITE_TIMEOUT));
        executor.setProcessDestroyer(new ShutdownHookProcessDestroyer());

        //Executing the command
        final CommandLine commandLine = Commandizer.makeCommand(shellScript, message);
        DefaultExecuteResultHandler resultHandler = new DefaultExecuteResultHandler();
        log.debug("Executing command: " + commandLine.toString());
        try {
            executor.execute(commandLine, resultHandler);
        } catch (Exception e) {
            timer.cancel();
            HeartBeats.message(mongoTemplate, messageQueue, StatusCodeTaskError, e.getMessage(), identifier, -1);
            producer.sendBody(identifier);
            log.info(e.getMessage());
            return;
        }

        //Anything after this will be executed only when the task completes
        boolean failure = true;
        try {
            resultHandler.waitFor();
            failure = false;
        } catch (InterruptedException e) {
            HeartBeats.message(mongoTemplate, messageQueue, StatusCodeTaskError, e.getMessage(), identifier, -1);
            producer.sendBody(identifier);
            log.error(e.getMessage());
        } finally {
            Thread.interrupted();
        }
        timer.cancel();
        if (failure) return;

        final String s = stdout.toString();
        String info = (resultHandler.getExitValue() == 0) ? null : "Fail: " + s;
        if (info != null && info.length() > 1000)
            info = "Fail (...only showing last 1000 characters of error message): " + info.substring(info.length() - 1000);
        log.info("resultHandler.exitValue=" + resultHandler.getExitValue());
        log.info("resultHandler.stdout=" + s);
        HeartBeats.message(mongoTemplate, messageQueue, StatusCodeTaskComplete, info, identifier, resultHandler.getExitValue());
        producer.sendBody(identifier);
    }
}
