package org.objectrepository.instruction.dao;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;

public class OrDaoImp implements OrDao {

    @Value("#{clientProperties['or.collection.files']}")
    private String files;

    @Autowired
    @Qualifier("mongoTemplateOr")
    private MongoTemplate mongoTemplate;

    @Override
    public boolean hasFiles(String pid) {
        final DBObject query = new BasicDBObject("pid", pid);
        DBObject document = mongoTemplate.getCollection(files).findOne(query);
        return ( document != null );
    }
}
