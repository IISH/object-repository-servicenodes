package org.objectrepository.io;

import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBCollection;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSInputFile;
import org.apache.log4j.Logger;
import org.objectrepository.exceptions.OrFilesException;
import org.objectrepository.util.Checksum;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.UUID;

/**
 * OrPut
 * <p/>
 * Adds a file with a metadata.pid in the database
 */
public class OrPut extends OrFilesFactory {

    private String environment;

    /**
     * action
     * <p/>
     * Uploads a stagingfile into the database.
     * The model assumes: md5 plus length is the key for a stagingfile
     * <p/>
     * It is possible that the action will receive an MD5 that is different from the one supplied.
     * In that case we delete file file.
     */
    @Override
    public void action() throws OrFilesException {
        final File localFile = new File(getL());
        final long fileLength = localFile.length();
        if (fileLength == 0) {
            throw new OrFilesException("Staging file not found; or the length of the file was zero bytes.");
        }
        final double shardKey = shardkey();

        final BasicDBObject query = new BasicDBObject("metadata.pid", getPid());
        final GridFSDBFile document = getGridFS().findOne(query);
        if (document != null && document.getLength() == fileLength && Checksum.compare(document.getMD5(), getMd5()) && expectedChunkCountOk(document)) {
            // Verify the chunks
            System.out.println("Skip put, as the file with md5 " + getMd5() + " and length " + fileLength + " already exists.");
        } else {
            System.out.println("Adding file with shardkey " + shardKey);
            final Date start = new Date();
            addFile(localFile, shardKey);
            System.out.println("Time it took in seconds: " + (new Date().getTime() - start.getTime()) / 1000);
        }
    }

    /**
     * Check chunk count
     *
     * @param document
     * @return
     */
    private boolean expectedChunkCountOk(GridFSDBFile document) {
        final DBCollection chunkCollection = getChunkCollection();
        final BasicDBObject field = new BasicDBObject("n", 1);
        final int numChunks = document.numChunks();
        for (int nc = 0; nc < numChunks; nc++) {
            final BasicDBObjectBuilder query = BasicDBObjectBuilder.start("files_id", document.getId()).add("n", nc);
            if (chunkCollection.findOne(query.get(), field) == null) return false;
        }
        return true;
    }

    /**
     * shardkey
     * <p/>
     * Returns the shardKey that was delivered via console.
     * Should the key already exist we create another.
     *
     * @return
     */
    private double shardkey() {

        final Double _id = getS();
        if (getGridFS().findOne(new BasicDBObject("_id", _id)) == null) return _id;
        setS(null);
        return shardkey();
    }

    /**
     * addFile
     * <p/>
     * Adds a new stagingfile into the database.
     * <p/>
     * We will add a semiPid. Should the upload fail, we thus still possess an original master document.
     *
     * @param localFile
     * @throws org.objectrepository.exceptions.OrFilesException
     *
     */
    private void addFile(File localFile, Object shardKey) throws OrFilesException {

        final GridFSInputFile gridFile;
        try {
            gridFile = getGridFS().createFile(localFile);
        } catch (IOException e) {
            throw new OrFilesException(e);
        }

        gridFile.setId(shardKey);
        final String semiPid = UUID.randomUUID().toString();
        gridFile.setMetaData(new BasicDBObject("pid", semiPid));
        gridFile.setContentType(getT());
        gridFile.save();

        boolean isMatch = Checksum.compare(getMd5(), gridFile.getMD5());
        if (isMatch) {
            getGridFS().remove(new BasicDBObject("metadata.pid", getA()));
            final BasicDBObject update = new BasicDBObject().append("$set", new BasicDBObject().append("metadata.pid", getA()));
            getFilesCollection().update(new BasicDBObject("metadata.pid", semiPid), update, false, true);
        } else {
            final String message = "The md5 that was offered (" + getMd5() + ") and the md5 ingested (" + gridFile.getMD5() + ") do not match ! The file will ne removed from the database.";
            removeFile(shardKey);
            log.fatal(message);
            if (getEnvironment().equalsIgnoreCase("production")) {
                System.exit(230);
            } else throw new OrFilesException(message);
        }
    }

    private void removeFile(Object shardKey) {
        getGridFS().remove(new BasicDBObject("_id", shardKey));
    }

    public String getEnvironment() {
        if (environment == null) environment = "production";
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    private static Logger log = Logger.getLogger(OrPut.class);
}
