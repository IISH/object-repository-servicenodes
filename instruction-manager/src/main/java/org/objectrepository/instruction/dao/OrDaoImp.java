package org.objectrepository.instruction.dao;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

public class OrDaoImp implements OrDao {

    @Value("#{clientProperties['or.collection.files']}")
    private String collection;

    @Autowired
    private Mongo mongo;

    @Override
    public boolean hasFiles(String na, String pid) {
        final DBObject query = new BasicDBObject("metadata.pid", pid);
        DBObject document = mongo.getDB("or_" + na).getCollection(collection).findOne(query);
        return ( document != null );
    }
}
