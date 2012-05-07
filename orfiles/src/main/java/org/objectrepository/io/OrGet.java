package org.objectrepository.io;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.gridfs.GridFSDBFile;
import org.apache.log4j.Logger;
import org.objectrepository.exceptions.OrFilesException;

import java.io.IOException;

/**
 * OrGet
 * <p/>
 * Retrieval of a stagingfile using a query. If the query matches multiple documents, then only the first is returned.
 */
public class OrGet extends OrFilesFactory {

    @Override
    public void action() throws OrFilesException {

        assert getL() != null;

        final DBObject query = new BasicDBObject("metadata.pid", getA());
        final GridFSDBFile gridFSDBFile = getGridFS().findOne(query);
        if (gridFSDBFile == null) {
            log.warn("No such stagingfile in database: " + query.toString());
            return;
        }

        log.info("Writing to stagingfile: " + getL());
        try {
            gridFSDBFile.writeTo(getL());
        } catch (IOException e) {
            throw new OrFilesException(e);
        }
    }

    private static Logger log = Logger.getLogger(OrGet.class);
}
