package org.objectrepository.services;

import org.apache.log4j.Logger;
import org.objectrepository.util.Queue;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.Date;

/**
 * HeartBeats
 * <p/>
 * Sends an update about the task.
 *
 * @author: Jozsef Gabor Bone <bonej@ceu.hu>
 * @author: Lucien van Wouw <lwo@iisg.nl>
 */
public class HeartBeats {

    public static void message(MongoTemplate mongoTemplate, String messageQueue, int statusCode, String info,
                               String identifier, int exitValue) {

        final String collectionName = Queue.getCollectionName(messageQueue);
        final Query query = new Query(new Criteria("task.identifier").is(identifier));
        final Update update = Update.update("task.statusCode", statusCode).set("task.end",
                new Date()).set("task.info", info).set("task.exitValue", exitValue);
        log.info("Update task.identifier:" + identifier +
                " with update task.exitValue:" + exitValue + ",task.statusCode:" + statusCode);
        mongoTemplate.updateFirst(query, update, collectionName);
    }

    final private static Logger log = Logger.getLogger(HeartBeats.class);
}
