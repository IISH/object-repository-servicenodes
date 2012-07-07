package org.objectrepository.io;

import com.mongodb.*;
import com.mongodb.util.SimplePool;
import com.mongodb.util.Util;
import org.apache.log4j.Logger;
import org.bson.types.ObjectId;
import org.objectrepository.exceptions.OrFilesException;

import java.security.MessageDigest;

/**
 * OrPut
 * <p/>
 * Inserts md5 checksum in chunks.
 */
public class OrMdq5 extends OrFilesFactory {

    private DBCollection _chunkCollection;
    private int count = 0;
    private MessageDigest _messageDigesterChunk = null;

    /**
     * action
     * <p/>
     * Takes the first chunk in the chunks collection that has no md5
     * Then created a cursor for all the chunks under the same identifier.
     * Calculated a md5 per chunk and sets it.
     * <p/>
     */
    @Override
    public void action() throws OrFilesException {
        _chunkCollection = getDatabase().getCollection(getB() + ".chunks");
        final DBObject query = QueryBuilder.start("md5").exists(false).get();
        final DBObject chunk = _chunkCollection.findOne(query);
        if (chunk == null) {
            log.info("No chunks found without a MD5... that is good.");
        } else {
            _messageDigesterChunk = _md5Pool.get();
            final String files_id = (String) chunk.get("files_id");
            log.warn("No md5 found in chunk. Will set these for " + files_id);
            enrichChunks(files_id);
        }
    }

    private void enrichChunks(String files_id) {
        final DBObject query = QueryBuilder.start("files_id").is(files_id).and("md5").exists(false).get();
        final DBCursor cursor = _chunkCollection.find(query);
        while (cursor.iterator().hasNext()) {
            checksum(cursor.iterator().next());
        }
        cursor.close();
        log.info("Done adding " + count + "md5 checksums");
    }

    private void checksum(DBObject chunk) {
        final ObjectId _id = (ObjectId) chunk.get("_id");
        final int n = (Integer) chunk.get("n");
        final DBObject query = new BasicDBObject("_id", _id);
        final String md5 = writeMD5((byte[]) chunk.get("data"));
        final DBObject update = new BasicDBObject("$set", new BasicDBObject("md5", md5));
        log.warn("No md5 in chunk {_id:" + _id.toString() + ",n:" + n + "}. Will update {md5:" + md5 + "}");
        _chunkCollection.update(query, update, true, false, WriteConcern.FSYNC_SAFE);
        log.info("Wrote to " + _id.toString() + " " + md5);
        count++;
    }

    private String writeMD5(byte[] writeBuffer) {
        _messageDigesterChunk.reset();
        _messageDigesterChunk.update(writeBuffer);
        return Util.toHex(_messageDigesterChunk.digest());
    }

    static SimplePool<MessageDigest> _md5Pool
            = new SimplePool<MessageDigest>("md5", 10, -1, false, false) {
        /**
         * {@inheritDoc}
         *
         * @see com.mongodb.util.SimplePool#createNew()
         */
        protected MessageDigest createNew() {
            try {
                return MessageDigest.getInstance("MD5");
            } catch (java.security.NoSuchAlgorithmException e) {
                throw new RuntimeException("your system doesn't have md5!");
            }
        }
    };


    private static Logger log = Logger.getLogger(OrMdq5.class);
}
