package org.objectrepository.instruction.dao;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.WriteConcern;
import com.mongodb.util.JSON;
import org.apache.log4j.Logger;
import org.objectrepository.instruction.InstructionType;
import org.objectrepository.instruction.StagingfileType;
import org.objectrepository.instruction.TaskType;
import org.objectrepository.util.Counting;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

public class OrMongoDBIterator implements OrIterator {

    private String orFileCollection;
    private MongoTemplate mongoTemplate;
    private String collectionName;
    private InstructionType instruction;
    private DBCursor cursor;
    private int processed = 0;

    public OrMongoDBIterator(InstructionType instruction, MongoTemplate mongoTemplate, String collectionName, String orFileCollection) {
        this.mongoTemplate = mongoTemplate;
        this.instruction = instruction;
        this.collectionName = collectionName;
        this.orFileCollection = orFileCollection;
        updateExpectedTotal(instruction.getFileSet());
        DBObject query = new BasicDBObject("fileSet", instruction.getFileSet());
        cursor = mongoTemplate.getCollection(collectionName).find(query);
    }

    @Override
    public void add(StagingfileType stagingfile) {
        stagingfile.setFileSet(instruction.getFileSet());
        log.info("Saving record " + stagingfile.getLocation());
        final TaskType task = stagingfile.getTask();
        if (task != null) {
            task.setTotal(0);
            task.setAttempts(0);
            task.setExitValue(Integer.MAX_VALUE);
            task.setProcessed(0);
        }
        mongoTemplate.save(stagingfile, collectionName);
        updateProcessed(instruction.getFileSet());
    }

    private void updateProcessed(String fileSet) {
        final DBObject query = new BasicDBObject("fileSet", fileSet);
        final DBObject update = (DBObject) JSON.parse(String.format("{$set:{task.processed:%s}}", ++processed));
        mongoTemplate.getCollection(orFileCollection).update(query, update, true, false, WriteConcern.NONE);
    }

    private void updateExpectedTotal(String fileSet) {
        final int total = Counting.countFiles(fileSet);
        final DBObject query = new BasicDBObject("fileSet", fileSet);
        final DBObject update = (DBObject) JSON.parse(String.format("{$set:{task.total:%s, task.processed:0}}", total));
        mongoTemplate.getCollection(orFileCollection).update(query, update, true, false);
    }


    @Override
    public StagingfileType getFileByLocation(String location) {
        final Query query = new Query(new Criteria("location").is(location).and("fileSet").is(instruction.getFileSet()));
        final StagingfileType stagingfileType = mongoTemplate.findOne(query, StagingfileType.class, collectionName);
        if (stagingfileType != null) stagingfileType.setTask(null);
        return stagingfileType;
    }

    @Override
    public InstructionType getInstruction() {
        return this.instruction;
    }

    @Override
    public boolean hasNext() {
        return cursor.hasNext();
    }

    @Override
    public StagingfileType next() {
        final DBObject document = cursor.next();
        return mongoTemplate.getConverter().read(StagingfileType.class, document);
    }

    @Override
    public void remove() {
        final DBObject document = cursor.curr();
        final Query query = new Query(new Criteria("_id").is(document.get("id_")));
        mongoTemplate.remove(query, collectionName);
    }

    @Override
    public int count() {
        return cursor.count();
    }

    private static Logger log = Logger.getLogger(OrMongoDBIterator.class);
}
