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
        if (commandLine == null || commandLine.trim().isEmpty()) {
            log.warn("The message was empty.");
            return;
        }

        // Some predefined commands
        if (commandLine.equalsIgnoreCase("kill")) {
            messageConsumerDaemon.shutdown();
        } else if (commandLine.equalsIgnoreCase("kill " + messageConsumerDaemon.getIdentifier())) {
            messageConsumerDaemon.shutdown();
        } else if (commandLine.equalsIgnoreCase("stop")) {
            messageConsumerDaemon.shutdown();
        } else if (commandLine.equalsIgnoreCase("stop " + messageConsumerDaemon.getIdentifier())) {
            messageConsumerDaemon.shutdown();
        } else if (commandLine.equalsIgnoreCase("start")) {
            messageConsumerDaemon.setPause(false);
        } else if (commandLine.equalsIgnoreCase("start " + messageConsumerDaemon.getIdentifier())) {
            messageConsumerDaemon.setPause(false);
        } else if (commandLine.equalsIgnoreCase("continue")) {
            messageConsumerDaemon.setPause(false);
        } else if (commandLine.equalsIgnoreCase("continue " + messageConsumerDaemon.getIdentifier())) {
            messageConsumerDaemon.setPause(false);
        } else if (commandLine.equalsIgnoreCase("pause")) {
            messageConsumerDaemon.setPause(true);
        } else if (commandLine.equalsIgnoreCase("pause " + messageConsumerDaemon.getIdentifier())) {
            messageConsumerDaemon.setPause(true);
        } else if (commandLine.equalsIgnoreCase("sleep")) {
            messageConsumerDaemon.setPause(true);
        } else if (commandLine.equalsIgnoreCase("sleep " + messageConsumerDaemon.getIdentifier())) {
            messageConsumerDaemon.setPause(true);
        } else
            log.warn("Command ignored " + commandLine);
    }

    final private static Logger log = Logger.getLogger(MediatorTopic.class);
}
