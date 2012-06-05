package org.objectrepository.instruction.dao;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.WriteConcern;
import org.apache.log4j.Logger;
import org.objectrepository.instruction.InstructionType;
import org.objectrepository.instruction.StagingfileType;
import org.objectrepository.instruction.TaskType;
import org.objectrepository.util.Counting;
import org.objectrepository.util.InstructionTypeHelper;
import org.objectrepository.util.Normalizers;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

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
        final TaskType task = InstructionTypeHelper.firstTask(stagingfile);
        if (task != null) {
            task.setN(0);
            task.setTotal(0);
            task.setAttempts(0);
            task.setExitValue(Integer.MAX_VALUE);
            task.setProcessed(0);
        }
        mongoTemplate.save(stagingfile, collectionName);
        updateProcessed(instruction.getFileSet());
    }

    private void updateProcessed(String fileSet) {
        final DBObject query = new BasicDBObject("fileSet", fileSet).append("workflow.n", 0);
        final Update update = Update.update("workflow.$.processed", ++processed);
        mongoTemplate.getCollection(orFileCollection).update(query, update.getUpdateObject(), true, false,
                WriteConcern.NONE);
    }

    /**
     * updateExpectedTotal
     * <p/>
     * Counts the number of files... up to a point. All in order to make a status possible: n of total files
     * processed.
     *
     * @param fileSet
     */
    private void updateExpectedTotal(String fileSet) {
        final int total = Counting.countFiles(fileSet);
        final DBObject query = new BasicDBObject("fileSet", fileSet).append("workflow.n", 0);
        final Update update = Update.update("workflow.$.total", total).set("workflow.$.processed", 0);
        mongoTemplate.getCollection(orFileCollection).update(query, update.getUpdateObject(), true, false,
                WriteConcern.NONE);
    }


    @Override
    public StagingfileType getFileByLocation(String location) {
        final Query query = new Query(new Criteria("location").is(location).and("fileSet").is(instruction.getFileSet()));
        final StagingfileType stagingfileType = mongoTemplate.findOne(query, StagingfileType.class, collectionName);
        if (stagingfileType != null) stagingfileType.getWorkflow().clear();
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

    @Override
    public int countByKey(String key, String value) {
        if (Normalizers.isEmpty(value)) {
            return -1;
        }
        final DBObject query = new BasicDBObject(key, value);
        return mongoTemplate.getCollection(collectionName).find(query).count();
    }

    private static Logger log = Logger.getLogger(OrMongoDBIterator.class);
}
