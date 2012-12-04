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
import org.objectrepository.instruction.InstructionType;
import org.objectrepository.util.Checksum;
import org.objectrepository.util.Normalizers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Update;

import java.util.ArrayList;

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
        final String collectionName = getCollectionName(stagingfile, instruction.getNa(), fileSet);
        mongoTemplate.dropCollection(collectionName);
        mongoTemplate.getCollection(collectionName).ensureIndex("location");
        return new OrMongoDBIterator(instruction, mongoTemplate, collectionName, orInstruction);
    }

    @Override
    /**
     * load
     *
     * Initializes the OrInstruction Iterator wrapper
     *
     * The result can be null in unit tests.
     **/
    public OrIterator load(InstructionType instruction) throws Exception {

        return new OrMongoDBIterator(instruction, mongoTemplate, stagingfile, orInstruction);
    }

    @Override
    public OrIterator load(String na, String location) throws Exception {

        throw new Exception("Method not implemented for " + this.getClass().getName());
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
        mongoTemplate.getCollection(stagingfile).remove(query);
        final String from = getCollectionName(stagingfile, instruction.getInstruction().getNa(), fileSet);
        final String cmd = "db." + from + ".find().forEach(function(x){db." + stagingfile + ".insert(x)})";
        mongoTemplate.getDb().doEval(cmd);
        mongoTemplate.dropCollection(getCollectionName(stagingfile, instruction.getInstruction().getNa(), fileSet));
    }

    public void delete(String fileSet) {
        final DBObject query = new BasicDBObject("fileSet", fileSet);
        mongoTemplate.getCollection(orInstruction).remove(query);
        mongoTemplate.getCollection(stagingfile).remove(query);
    }

    @Override
    public void removetasks(InstructionType instruction) {
        final DBObject query = new BasicDBObject("fileSet", Normalizers.normalize(instruction.getFileSet()));
        final Update update = new Update().set("workflow", new ArrayList(0));
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

    //private static Logger log = Logger.getLogger(InstructionMongoDBImpl.class);
}
