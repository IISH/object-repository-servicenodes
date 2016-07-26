package org.objectrepository.io;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.ReplicaGridFSDBFile;
import org.apache.log4j.Logger;
import org.objectrepository.exceptions.OrFilesException;

import java.io.IOException;

/**
 * OrGet
 * <p/>
 * Retrieval of a file using a query. However, here the metadata collection is stored on a different
 * host than the chunks. Normally a mongos router is used to retrieve the content, but this class allows
 * us to bypass the use of the router.
 */
public class OrReplica extends OrFilesFactory {

    @Override
    public void action() throws OrFilesException {

        assert getL() != null;
        final DBObject query =  (getS() == 0) ? new BasicDBObject("metadata.pid", getA()) : new BasicDBObject("_id", getS());
        DBCollection collection = getMongo().getDB(getD()).getCollection(getB() + ".files");
        collection.setObjectClass(ReplicaGridFSDBFile.class);
        final DBObject metadata = collection.findOne(query);
        this.getMongo().close();
        if (metadata == null) {
            log.warn("No such file in database: " + query.toString());
            return;
        }

        final OrReplica replica = new OrReplica();
        replica.setB(getB());
        final String[] hosts = getR().split("\\s|,|;|\t");
        replica.setMongo(hosts);
        final ReplicaGridFSDBFile f = (ReplicaGridFSDBFile)metadata;
        f.setGridFS(replica.getGridFS());

        log.info("Writing to stagingfile: " + getL());
        try {
            f.writeTo(getL());
        } catch (IOException e) {
            throw new OrFilesException(e);
        }
    }

    private static Logger log = Logger.getLogger(OrReplica.class);
}
