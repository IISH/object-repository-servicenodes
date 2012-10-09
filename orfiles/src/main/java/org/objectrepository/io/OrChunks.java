package org.objectrepository.io;

import com.mongodb.*;
import com.mongodb.gridfs.GridFSDBFile;
import org.apache.log4j.Logger;
import org.objectrepository.exceptions.OrFilesException;

import java.io.FileOutputStream;
import java.io.IOException;

/**
 * OrChunks
 * <p/>
 * Retrieves the first two, five middle and last two chunks. Nine in total.
 * If the total is larger than the length of the file, we will skip this and return the entire file.
 */
public class OrChunks extends OrFilesFactory {

    @Override
    public void action() throws OrFilesException {

        assert getL() != null;
        final DBObject query = new BasicDBObject("metadata.pid", getA());
        final GridFSDBFile gridFSDBFile = getGridFS().findOne(query);
        if (gridFSDBFile == null) {
            log.warn("No such file in database: " + query.toString());
            return;
        }

        if (gridFSDBFile.numChunks() < 10) {
            log.info("Writing to stagingfile: " + getL());
            try {
                gridFSDBFile.writeTo(getL());
            } catch (IOException e) {
                throw new OrFilesException(e);
            }
        } else {
            try {
                writeChunks(gridFSDBFile);
            } catch (IOException e) {
                throw new OrFilesException(e);
            }
        }
    }

    private void writeChunks(GridFSDBFile gridFSDBFile) throws IOException {

        final FileOutputStream fos = new FileOutputStream(getL());
        final DBCollection _chunkCollection = getDatabase().getCollection(getB() + ".chunks");
        for (int n = 0; n < 10; n++) {
            fos.write(getChunk(_chunkCollection, gridFSDBFile.getId(), n));
        }
        fos.write(getChunk(_chunkCollection, gridFSDBFile.getId(), gridFSDBFile.numChunks()-2));
        fos.write(getChunk(_chunkCollection, gridFSDBFile.getId(), gridFSDBFile.numChunks()-1));
        fos.close();
    }

    byte[] getChunk(DBCollection _chunkCollection, Object _id, int i) {

        DBObject chunk = _chunkCollection.findOne(BasicDBObjectBuilder.start("files_id", _id)
                .add("n", i).get());
        if (chunk == null)
            throw new MongoException("can't find a chunk!  file id: " + _id + " chunk: " + i);

        return (byte[]) chunk.get("data");
    }


    private static Logger log = Logger.getLogger(OrChunks.class);
}
