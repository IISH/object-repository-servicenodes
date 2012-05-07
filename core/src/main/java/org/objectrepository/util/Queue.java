package org.objectrepository.util;

import java.util.regex.Pattern;

/**
 * Queue
 * <p/>
 * Utilities for message queue activities.
 */
public class Queue {

    /**
     * queueName
     * <p/>
     * Gives the simple name of the queue, without the urn format
     *
     * @param messageQueue
     * @return
     */
    public static String queueName(String messageQueue) {
        final String[] split = messageQueue.split(":");
        return (split[split.length - 1]);
    }

    /**
     * getCollectionName
     * <p/>
     * By convention the queue name holds the collection name:
     * activemq:CollectionAction
     * To derive it, we take the first string up until the second capital.
     *
     * @param messageQueue
     * @return
     */
    public static String getCollectionName(String messageQueue) {

        final String[] split = messageQueue.split(":", 2);
        final String[] controllerAction = Pattern.compile("([A-Z])").matcher(split[1]).replaceAll(" $1").trim().toLowerCase().split("\\s", 2);
        return controllerAction[0];
    }
}
