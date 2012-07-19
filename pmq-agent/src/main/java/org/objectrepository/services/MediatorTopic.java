package org.objectrepository.services;

import org.apache.camel.ConsumerTemplate;
import org.apache.log4j.Logger;
import org.objectrepository.MessageConsumerDaemon;

public class MediatorTopic implements Runnable {

    private ConsumerTemplate consumer;
    private String messageQueue;
    MessageConsumerDaemon messageConsumerDaemon;

    public MediatorTopic(MessageConsumerDaemon messageConsumerDaemon, ConsumerTemplate consumer, String messageQueue) {
        this.consumer = consumer;
        this.messageQueue = messageQueue;
        this.messageConsumerDaemon = messageConsumerDaemon;
    }

    @Override
    public void run() {

        final String commandLine = consumer.receiveBody(messageQueue, String.class);
        if (commandLine == null || commandLine.trim().isEmpty()) return;

        // Some predefined commands
        if (commandLine.equalsIgnoreCase("kill")) {
            messageConsumerDaemon.shutdown();
        }

        if (commandLine.equalsIgnoreCase("kill " + messageConsumerDaemon.getIdentifier())) {
            messageConsumerDaemon.shutdown();
        }

        log.warn("Command ignored " + commandLine);
    }

    final private static Logger log = Logger.getLogger(MediatorTopic.class);

}
