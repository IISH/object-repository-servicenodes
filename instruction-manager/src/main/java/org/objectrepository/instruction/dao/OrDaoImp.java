package org.objectrepository.instruction.dao;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;

public class OrDaoImp implements OrDao {

    @Value("#{clientProperties['or.collection.files']}")
    private String collection;

    @Autowired
    @Qualifier("mongoTemplateOr")
    private MongoTemplate mongoTemplate;

    @Override
    public boolean hasFiles(String pid) {
        final DBObject query = new BasicDBObject("metadata.pid", pid);
        DBObject document = mongoTemplate.getCollection(collection).findOne(query);
        return ( document != null );
    }
}
