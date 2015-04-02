/*
 * Copyright 2010 International Institute for Social History, The Netherlands.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.objectrepository;

import org.apache.camel.ConsumerTemplate;
import org.apache.camel.ProducerTemplate;
import org.apache.log4j.Logger;
import org.objectrepository.services.MediatorQueue;
import org.objectrepository.services.MediatorTopic;
import org.springframework.context.support.GenericXmlApplicationContext;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.RejectedExecutionHandler;

public class MessageConsumerDaemon extends Thread implements Runnable {

    private static MessageConsumerDaemon instance;
    private boolean keepRunning = true;
    private GenericXmlApplicationContext context;
    private long timer;
    private long timerInterval = 10000;
    private long heartbeatInterval = 10000;
    private List<Queue> taskExecutors;
    private String identifier;
    final private static Logger log = Logger.getLogger(MessageConsumerDaemon.class);
    private boolean pause = false;
    private boolean stop = false;

    private MessageConsumerDaemon() {
        timer = System.currentTimeMillis() + timerInterval;
    }

    public void run() {

        init();
        while (keepRunning) {
            if (isStop()) {
                for (Queue queue : taskExecutors) {
                    keepRunning = keepRunning && (queue.getActiveCount() != 0);
                }
            } else {
                for (Queue queue : taskExecutors) {
                    if (isPause() && queue.isTopic() || !isPause()) {
                        if (queue.getActiveCount() < queue.getMaxPoolSize()) {
                            log.debug(queue.getQueueName() + " has activeCount " + queue.getActiveCount() + " / maxPoolSize " + queue.getMaxPoolSize());
                            queue.execute(mediatorInstance(queue));
                        }
                    }
                }
            }
            heartbeat();
        }
        context.close();
    }

    public void init() {

        log.info("Startup service...");
        GenericXmlApplicationContext context = new GenericXmlApplicationContext();
        context.setValidating(false);
        context.load("/META-INF/spring/application-context.xml", "META-INF/spring/dispatcher-servlet.xml");
        context.refresh();
        setContext(context);
        context.registerShutdownHook();

        final RejectedExecutionHandler rejectedExecutionHandler = context.getBean(RejectedExecutionHandler.class);
        for (Queue taskExecutor : taskExecutors) {
            taskExecutor.setRejectedExecutionHandler(rejectedExecutionHandler);
            taskExecutor.initialize();
            log.info("Initialized " + taskExecutor.getQueueName());
        }
    }

    private Runnable mediatorInstance(Queue queue) {

        if (queue.isTopic()) {
            log.info("Adding topic consumer for " + queue.getQueueName());
            return new MediatorTopic(this, context.getBean(ConsumerTemplate.class), "activemq:topic:" + queue.getQueueName());
        } else {
            log.info("Adding queue consumer for " + queue.getQueueName());
            return new MediatorQueue(context.getBean(MongoTemplate.class), context.getBean(ConsumerTemplate.class), context.getBean(ProducerTemplate.class), "activemq:" + queue.getQueueName(), queue.getBash(), queue.getShellScript(), heartbeatInterval);
        }
    }

    /**
     * heartbeat
     * <p/>
     * Keeps the overall environment from overworking by pausing every ten seconds or so.
     */
    private void heartbeat() {

        long currentTime = System.currentTimeMillis();
        if (timer - currentTime < 0) {
            timer = currentTime + timerInterval;
            if (isPause()) {
                log.info("We are in pause mode.");
            } else {
                log.info("Actively listening to queues.");
            }
        }

        try {
            sleep(1000);
        } catch (InterruptedException e) {
            interrupt();
        } catch (Throwable t) {
            log.error("Cannot put thread to sleep..."); // Can we ignore this ?
        }
    }

    /**
     * setTaskExecutors
     * <p/>
     * Sets the queues.
     *
     * @param taskExecutors
     */
    public void setTaskExecutors(List<Queue> taskExecutors) {
        this.taskExecutors = taskExecutors;
    }

    /**
     * Method clone should not be allowed for a singleton.
     *
     * @return The cloned object that never will be returned
     * @throws CloneNotSupportedException
     */
    @Override
    public Object clone()
            throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    public static synchronized MessageConsumerDaemon getInstance(List<Queue> queues, String identifier, long heartbeatInterval) {

        if (instance == null) {
            instance = new MessageConsumerDaemon();
            instance.setTaskExecutors(queues);
            instance.setIdentifier(identifier);
            instance.setDaemon(true);
            instance.setHeartbeatInterval(heartbeatInterval);
        }
        return instance;
    }

    /**
     * main
     * <p/>
     * Accepts one folder as argument:  -messageQueues
     * That folder ought to contain one or more folders ( or symbolic links ) to the files
     * The folder has the format: [foldername] or [foldername].[maxTasks]
     * MaxTasks is to indicate the total number of jobs being able to run.
     * <p/>
     * long
     *
     * @param argv
     */
    public static void main(String[] argv) {

        if (instance == null) {
            final Properties properties = new Properties();

            if (argv.length > 0) {
                for (int i = 0; i < argv.length; i += 2) {
                    try {
                        properties.put(argv[i], argv[i + 1]);
                    } catch (ArrayIndexOutOfBoundsException arr) {
                        System.out.println("Missing value after parameter " + argv[i]);
                        System.exit(-1);
                    }
                }
            } else {
                log.fatal("Usage: pmq-agent.jar -messageQueues [queues] -heartbeatInterval [interval in ms]\n" +
                        "The queues is a folder that contains symbolic links to the startup scripts.");
                System.exit(-1);
            }

            if (log.isInfoEnabled()) {
                log.info("Arguments set: ");
                for (String key : properties.stringPropertyNames()) {
                    log.info("'" + key + "'='" + properties.getProperty(key) + "'");
                }
            }

            if (!properties.containsKey("-messageQueues")) {
                log.fatal("Expected case sensitive parameter: -messageQueues");
                System.exit(-1);
            }

            final File messageQueues = new File((String) properties.get("-messageQueues"));
            if (!messageQueues.exists()) {
                log.fatal("Cannot find folder for messageQueues: " + messageQueues.getAbsolutePath());
                System.exit(-1);
            }

            if (messageQueues.isFile()) {
                log.fatal("-messageQueues should point to a folder, not a file: " + messageQueues.getAbsolutePath());
                System.exit(-1);
            }

            if (!properties.containsKey("-bash")) {
                log.fatal("Expected bash executable: -bash");
                System.exit(-1);
            }
            final File bash = new File((String) properties.get("-bash"));
            if (!bash.exists()) {
                log.fatal("Bash does not exist here: " + bash.getAbsolutePath());
                System.exit(-1);
            }

            long heartbeatInterval = 600000;
            if (properties.containsKey("-heartbeatInterval")) {
                heartbeatInterval = Long.parseLong((String) properties.get("heartbeatInterval"));
            }

            String identifier = null;
            if (properties.containsKey("-id")) {
                identifier = (String) properties.get("-id");
            } else if (properties.containsKey("-identifier")) {
                identifier = (String) properties.get("-identifier");
            }

            final String CYGWIN_HOME = System.getenv("CYGWIN_HOME");

            final File[] files = messageQueues.listFiles();
            final String[] scriptNames = (properties.containsKey("-startup"))
                    ? new String[]{properties.getProperty("-startup")}
                    : new String[]{"/startup.sh", "\\startup.bat"};
            final List<Queue> queues = new ArrayList<Queue>();
            for (File file : files) {
                final String name = file.getName();
                final String[] split = name.split("\\.", 2);
                final String queueName = split[0];
                for (String scriptName : scriptNames) {
                    final String _shellScript = file.getAbsolutePath() + scriptName;
                    final String shellScript = (CYGWIN_HOME == null)
                            ? _shellScript
                            : _shellScript.substring(CYGWIN_HOME.length()).replace("\\", "/");
                    final int maxTask = (split.length == 1) ? 1 : Integer.parseInt(split[1]);
                    log.info("Candidate mq client for " + queueName + " maxTasks " + maxTask);
                    if (new File(_shellScript).exists()) {
                        final Queue queue = new Queue(queueName, bash.getAbsolutePath(), shellScript, false);
                        queue.setCorePoolSize(1);
                        queue.setMaxPoolSize(maxTask);
                        queue.setQueueCapacity(1);
                        queues.add(queue);
                        break;
                    } else {
                        log.warn("... skipping, because no startup script found at " + shellScript);
                    }
                }
            }

            if (queues.size() == 0) {
                log.fatal("No queue folders seen in " + messageQueues.getAbsolutePath());
                System.exit(-1);
            }

            // Add the system queue
            queues.add(new Queue("Connection", null, null, true));

            getInstance(queues, identifier, heartbeatInterval).run();
        }
        System.exit(0);
    }

    public void setContext(GenericXmlApplicationContext context) {
        this.context = context;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public boolean isPause() {
        return pause;
    }

    public void setPause(boolean pause) {
        this.pause = pause;
    }

    public boolean isStop() {
        return stop;
    }

    public void setStop(boolean stop) {
        this.stop = stop;
    }

    public void setHeartbeatInterval(long heartbeatInterval) {
        this.heartbeatInterval = heartbeatInterval;
    }
}