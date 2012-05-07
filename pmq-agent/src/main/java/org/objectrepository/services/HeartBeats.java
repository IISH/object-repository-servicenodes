package org.objectrepository.services;

/*
* HeartBeats
*
*
*
* Sends a message back with a statusCode
*
* @author: Jozsef Gabor Bone <bonej@ceu.hu>
*/

import org.apache.log4j.Logger;
import org.objectrepository.util.Queue;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.Date;

public class HeartBeats {

    public static void message(MongoTemplate mongoTemplate, String messageQueue, int statusCode, String info,
                               String identifier, int exitValue) {

        final String collectionName = Queue.getCollectionName(messageQueue);
        final Query query = new Query(new Criteria("task.identifier").is(identifier));
        final Update update = Update.update("task.statusCode", statusCode).set("task.end",
                new Date()).set("task.info", info).set("task.exitValue",exitValue);
        log.info("Sending message to database.");
        mongoTemplate.updateFirst(query, update, collectionName);
    }

    final private static Logger log = Logger.getLogger(HeartBeats.class);
}
