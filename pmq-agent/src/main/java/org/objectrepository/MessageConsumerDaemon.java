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

import org.apache.camel.CamelContext;
import org.apache.log4j.Logger;
import org.objectrepository.services.Mediator;
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
    final private static Logger log = Logger.getLogger(MessageConsumerDaemon.class);
    private GenericXmlApplicationContext context;
    private long timer;
    private long period = 10000;
    private List<Queue> taskExecutors;

    private MessageConsumerDaemon() {
        timer = System.currentTimeMillis() + period;
    }

    public void run() {

        init();
        while (keepRunning) {
            for (Queue queue : taskExecutors) {
                if (queue.getActiveCount() < queue.getMaxPoolSize()) {
                    queue.execute(mediatorInstance(queue.getQueueName(), queue.getShellScript()));
                }
            }
            heartbeat();
        }
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
        }
    }

    private Mediator mediatorInstance(String queue, String shellScript) {

        log.debug("Adding mediator");
        return new Mediator(context.getBean(MongoTemplate.class), context.getBean(CamelContext.class).createConsumerTemplate(), "activemq:" + queue, shellScript, period);
    }

    /**
     * heartbeat
     * <p/>
     * Keeps the overall environment healthy by pausing.
     */
    private void heartbeat() {

        long currentTime = System.currentTimeMillis();
        if (timer - currentTime < 0) {
            timer = currentTime + period;
            log.debug("Heartbeat not implemented");
        }

        try {
            sleep(1000);
        } catch (InterruptedException e) {
            interrupt();
        } catch (Throwable t) {
            log.error("Cannot put thread to sleep...");
            shutdown();
        }
    }

    public void shutdown() {
        log.info("Shutting down services...");
        keepRunning = false;
        context.close();
        System.exit(0);
    }

    public void setContext(GenericXmlApplicationContext context) {
        this.context = context;
    }

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

    public static synchronized MessageConsumerDaemon getInstance(List<Queue> queues) {

        if (instance == null) {
            instance = new MessageConsumerDaemon();
            instance.setTaskExecutors(queues);
            instance.setDaemon(true);
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
                System.out.println("Usage: pmq-agent.jar -messageQueues queues");
                System.exit(-1);
            }

            if (!properties.containsKey("-messageQueues")) {
                System.out.println("Expected case sensitive parameter: -messageQueues");
                System.exit(-1);
            }

            final File messageQueues = new File((String) properties.get("-messageQueues"));
            if (!messageQueues.exists()) {
                System.out.println("Expected case sensitive parameter: -messageQueues");
                System.exit(-1);
            }
            if (messageQueues.isFile()) {
                System.out.println("-messageQueues should point to a folder, not a file.");
                System.exit(-1);
            }

            final File[] files = messageQueues.listFiles();
            final List<Queue> queues = new ArrayList<Queue>();
            for (File file : files) {
                final String name = file.getName();
                String[] split = name.split("\\.", 2);
                String shellScript = file.getAbsolutePath() + "/startup.sh";
                String queueName = split[0];
                int maxTask = (split.length == 1) ? 1 : Integer.parseInt(split[1]);
                System.out.println("Candidate mq client for " + queueName + " maxTasks " + maxTask);
                if (new File(shellScript).exists()) {
                    final Queue queue = new Queue(queueName, shellScript);
                    queue.setQueueCapacity(maxTask);
                    queue.setMaxPoolSize(maxTask);
                    queue.setCorePoolSize(maxTask);
                    queues.add(queue);
                } else {
                    System.out.println("... skipping, because no startup script found at " + shellScript);
                }
            }

            if (queues.size() == 0) {
                System.out.println("No queue folders seen in " + messageQueues.getAbsolutePath());
                System.exit(-1);
            }

            getInstance(queues).run();
        }
    }
}