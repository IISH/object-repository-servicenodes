/*
 * Copyright (c) 2012 Social History Services
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
package org.objectrepository.instruction.dao;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import org.apache.log4j.Logger;
import org.objectrepository.instruction.InstructionType;
import org.objectrepository.util.Checksum;
import org.objectrepository.util.Normalizers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.ArrayList;
import java.util.List;

/**
 * Datasource for MongoDB
 *
 * @author Lucien van Wouw <lwo@iisg.nl>
 */

public class InstructionMongoDBImpl implements InstructionDao {

    @Value("#{clientProperties['or.collection.instruction']}")
    private String orInstruction;

    @Value("#{clientProperties['or.collection.stagingfile']}")
    private String stagingfile;

    @Value("#{clientProperties['or.collection.profile']}")
    private String profile;

    @Autowired
    @Qualifier("mongoTemplateSa")
    private MongoTemplate mongoTemplate;

    @Override
    /**
     * create
     *
     * Sets a temporary collection \ bag to write a new instruction into.
     *
     * Sets the global element in a temporary collection
     *
     */
    public OrMongoDBIterator create(InstructionType instruction) throws Exception {

        final String fileSet = Normalizers.normalize(instruction.getFileSet());
        instruction.setFileSet(fileSet);
        String collectionName = getCollectionName(orInstruction, instruction.getNa(), fileSet);
        mongoTemplate.dropCollection(collectionName);
        mongoTemplate.dropCollection(getCollectionName(stagingfile, instruction.getNa(), fileSet));
        mongoTemplate.insert(instruction, collectionName);
        mongoTemplate.getCollection(collectionName).ensureIndex("fileSet");

        collectionName = getCollectionName(stagingfile, instruction.getNa(), fileSet);
        mongoTemplate.getCollection(collectionName).ensureIndex("location");
        return new OrMongoDBIterator(instruction, mongoTemplate, collectionName, orInstruction);
    }

    @Override
    /**
     * load
     *
     * Calling this method will load an instruction and global settings from the collections.
     * Console argument are preserved.
     *
     * The result can be null in unit tests.
     */
    public OrIterator load(InstructionType instruction) throws Exception {
        OrIterator i = load(instruction.getNa(), instruction.getFileSet());
        if (i == null) {
            log.warn("No profile in database. Assuming we are running a unit test.");
            i = new OrMongoDBIterator(instruction, mongoTemplate, stagingfile, orInstruction);
        }
        //syncProfile(i.getInstruction());
        i.getInstruction().setTask(instruction.getTask());
        return i;
    }

    @Override
    /**
     * load
     *
     * Calling this method will load an instruction from the collection.
     */
    public OrMongoDBIterator load(String na, String fileSet) throws Exception {
        final Query query = new Query(new Criteria("fileSet").is(Normalizers.normalize(fileSet)));
        final InstructionType instruction = mongoTemplate.findOne(query, InstructionType.class, orInstruction);
        if (instruction == null) {
            log.warn("Instruction was not found in collection: " + fileSet);
            return null;
        }
        return new OrMongoDBIterator(instruction, mongoTemplate, stagingfile, orInstruction);
    }

    /**
     * Persist by moving the temporary collection into the main collection.
     * <p/>
     * When -DoverrideGlobal=true or there is no global instruction registered the data will be persisted in the
     * database
     * <p/>
     * Files in the stagingfile are always inserted.
     *
     * @param instruction
     * @throws Exception
     */
    @Override
    public void persist(OrIterator instruction) throws Exception {

        final String fileSet = instruction.getInstruction().getFileSet();
        final DBObject query = new BasicDBObject("fileSet", fileSet);
        final List<String> collections = new ArrayList<String>();
        collections.add(stagingfile);

        final DBObject one = mongoTemplate.getCollection(orInstruction).findOne(query);
        collections.add(orInstruction);
        for (String to : collections) {
            mongoTemplate.getCollection(to).remove(query);
            final String from = getCollectionName(to, instruction.getInstruction().getNa(), fileSet);
            final String cmd = "db." + from + ".find().forEach(function(x){db." + to + ".insert(x)})";
            mongoTemplate.getDb().doEval(cmd);
        }
        mongoTemplate.dropCollection(getCollectionName(orInstruction, instruction.getInstruction().getNa(), fileSet));
        mongoTemplate.dropCollection(getCollectionName(stagingfile, instruction.getInstruction().getNa(), fileSet));

        if (one != null) {
            Update update = Update.update("task", one.get("task")).set("workflow", one.get("workflow"));
            mongoTemplate.getCollection(orInstruction).update(query, update.getUpdateObject(), true, false);
        }
    }

    public void delete(String fileSet) {
        final DBObject query = new BasicDBObject("fileSet", fileSet);
        mongoTemplate.getCollection(orInstruction).remove(query);
        mongoTemplate.getCollection(stagingfile).remove(query);
    }

    @Override
    public void removetasks(InstructionType instruction) {
        final DBObject query = new BasicDBObject("fileSet", Normalizers.normalize(instruction.getFileSet()));
        final Update update = new Update().unset("task");
        mongoTemplate.getCollection(stagingfile).updateMulti(query, update.getUpdateObject());
    }

    private String getCollectionName(String na, String fileSet) {

        return (fileSet == null)
                ? "cp_" + na
                : "cp_" + na + "_" + Checksum.getMD5(fileSet);
    }

    private String getCollectionName(String collection, String na, String fileSet) {
        return collection + "_" + getCollectionName(na, fileSet);
    }

    private static Logger log = Logger.getLogger(InstructionMongoDBImpl.class);
}
