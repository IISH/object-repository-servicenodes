package org.objectrepository.services;

import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.Date;
import java.util.TimerTask;

/**
 * HeartBeat
 * <p/>
 * Responsible for sending back messages to the queue. Thus indicating we are still alive.
 */
public class HeartBeat extends TimerTask {

    private MongoTemplate mongoTemplate;
    private String messageQueue;
    private int statusCode;
    private String identifier;
    private long start;

    public HeartBeat(MongoTemplate mongoTemplate, String messageQueue, int statusCode, String identifier, long start) {
        this.mongoTemplate = mongoTemplate;
        this.messageQueue = messageQueue;
        this.statusCode = statusCode;
        this.statusCode = statusCode;
        this.identifier = identifier;
        this.start = start;
    }

    @Override
    public void run() {
        long time = (new Date().getTime() - start) / 1000;
        long s = time % 60;
        long m = (time / 60) % 60;
        long h = (time / 3600);
        final String info = String.format("Duration: %s:%s:%s", h, m, s);
        HeartBeats.message(mongoTemplate, messageQueue, statusCode, info, identifier, Integer.MAX_VALUE);
    }
}
