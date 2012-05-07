package org.objectrepository.io;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.QueryBuilder;
import com.mongodb.gridfs.GridFSInputFile;
import org.apache.log4j.Logger;
import org.objectrepository.exceptions.OrFilesException;

import java.io.File;
import java.io.IOException;
import java.util.Date;

/**
 * OrPut
 * <p/>
 * Adds a file with a metadata.pid in the database
 */
public class OrPut extends OrFilesFactory {

    private static String longPadding = "%" + String.valueOf((int) Math.sqrt(Long.SIZE) * 2) + "s";

    /**
     * action
     * <p/>
     * Uploads a stagingfile into the database.
     * The model assumes: md5 plus length is the key for a stagingfile
     */
    @Override
    public void action() throws OrFilesException {
        final File localFile = new File(getL());
        final long fileLength = localFile.length();
        if (fileLength == 0) {
            throw new OrFilesException("Staging file not found; or the length of the file was zero bytes.");
        }

        final String md5 = getMd5();
        final DBObject query = QueryBuilder.start("md5").is(md5).and("length").is(fileLength).get();
        final DBObject document = getBucket().findOne(query);
        if (document == null) {
            System.out.println("Adding new file");
            final Date start = new Date();
            addFile(localFile, md5, fileLength);
            System.out.println("Time it took in seconds: " + (new Date().getTime() - start.getTime()) / 1000);
        } else {
            System.out.println("Skip put, as the file with md5 " + md5 + " and length " + fileLength + " already exists.");
        }
    }

    /**
     * addFile
     * <p/>
     * Adds a new stagingfile into the database
     *
     * @param localFile
     * @throws org.objectrepository.exceptions.OrFilesException
     *
     */
    private void addFile(File localFile, String md5, long length) throws OrFilesException {

        final GridFSInputFile gridFile;
        try {
            gridFile = getGridFS().createFile(localFile);
        } catch (IOException e) {
            throw new OrFilesException(e);
        }
        gridFile.setId(md5 + String.format(longPadding, Long.toHexString(length)).replace(' ', '0'));
        gridFile.setMetaData(new BasicDBObject("pid", getA()));
        gridFile.setContentType(getT());
        gridFile.save();
    }

    private static Logger log = Logger.getLogger(OrPut.class);
}
