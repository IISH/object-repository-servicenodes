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
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.io.File;
import java.util.Properties;

public class MessageConsumerDaemon extends Thread implements Runnable {

    private static MessageConsumerDaemon instance;
    private boolean keepRunning = true;
    final private static Logger log = Logger.getLogger(MessageConsumerDaemon.class);
    private Properties properties = new Properties();
    private GenericXmlApplicationContext context;
    private ThreadPoolTaskExecutor taskExecutor;
    private long timer;
    private long period = 30000;

    private MessageConsumerDaemon() {
        timer = System.currentTimeMillis() + period;
    }

    public void run() {

        init();
        while (keepRunning) {
            while (taskExecutor.getActiveCount() < taskExecutor.getMaxPoolSize()) {
                taskExecutor.execute(mediatorInstance());
            }
            heartbeat();
        }
    }

    public void init() {

        log.info("Startup service...");
        GenericXmlApplicationContext  context = new GenericXmlApplicationContext();
        context.setValidating(false);
        context.load("/META-INF/spring/application-context.xml", "META-INF/spring/dispatcher-servlet.xml");
        context.refresh();
        setContext(context);
        context.registerShutdownHook();
        this.taskExecutor = context.getBean(ThreadPoolTaskExecutor.class);
        this.taskExecutor.setCorePoolSize(Integer.parseInt(properties.getProperty("-maxTasks")));
        this.taskExecutor.setMaxPoolSize(Integer.parseInt(properties.getProperty("-maxTasks")));
    }

    private Mediator mediatorInstance() {

        log.debug("Adding mediator");
        return new Mediator(context.getBean(MongoTemplate.class), context.getBean(CamelContext.class).createConsumerTemplate(), "activemq:" + properties.getProperty("-messageQueue"), properties.getProperty("-shellScript"), period);
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

    public void setProperties(Properties properties) {
        this.properties = properties;
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

    public static synchronized MessageConsumerDaemon getInstance(Properties properties) {

        if (instance == null) {
            instance = new MessageConsumerDaemon();
            instance.setProperties(properties);
            instance.setDaemon(true);
        }
        return instance;
    }

    public static void main(String[] argv) {

        String shell;
        String maxTask;

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
                System.out.println("Usage: pmq-agent.jar -messageQueue QUEUE_NAME [-shellScript SHELL_SCRIPT_PATH] [-maxTasks TASK_NUMBER]");
                System.out.println("Default shell script (if not provided): /opt/QUEUE_NAME.sh");
                System.out.println("Default maximum task number (if not provided): 3");
                System.exit(0);
            }

            if (!properties.containsKey("-messageQueue")) {
                log.fatal("Expected case sensitive parameter: -messageQueue");
                System.exit(-1);
            }

            //Get -shellScript property, check if the given stagingfile exists, if it wasn't given or stagingfile doesn't
            //exists, use default one: /opt/<messageQueueName>.sh.
            shell = properties.getProperty("-shellScript");
            if (shell == null)
                shell = "/opt/" + properties.getProperty("-messageQueue") + ".sh";

            if (new File(shell).exists()) {
                properties.setProperty("-shellScript", shell);
            } else {
                log.fatal("File " + shell + " doesn't exist.");
                System.exit(-1);
            }

            //Get -maxTasks property, if it wasn't given use default: 3
            maxTask = "3";
            if (properties.containsKey("-maxTasks")) {
                try {
                    Integer.parseInt(properties.getProperty("-maxTasks"));
                    maxTask = properties.getProperty("-maxTasks");
                } catch (NumberFormatException nfe) {
                    log.fatal("Task number should be an integer value.");
                    System.exit(-1);
                }
            }
            properties.setProperty("-maxTasks", maxTask);

            getInstance(properties).run();
        }
    }
}