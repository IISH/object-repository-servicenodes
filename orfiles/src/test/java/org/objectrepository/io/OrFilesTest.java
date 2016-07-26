package org.objectrepository.io;

import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.WriteConcern;
import com.mongodb.gridfs.GridFSDBFile;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.objectrepository.exceptions.OrFilesException;
import org.objectrepository.util.Checksum;

import java.io.File;
import java.net.URL;
import java.util.Random;
import java.util.UUID;

public class OrFilesTest {

    final static private String[] hosts = new String[]{"localhost"};
    final static private String file = "/xxxxxxxx-xxxx-Mxxx-Nxxx-xxxxxxxxxxxx";
    final static private String fileUpdate = "/xxxxxxxx-xxxx-Mxxx-Nxxx-xxxxxxxxxxxx.update";
    final static private String db = "test";
    final static private String bucket = "level12345";

    @BeforeClass
    public static void setUp() throws ClassNotFoundException, OrFilesException {

        System.setProperty("WriteConcern", "SAFE"); // In case we do not test in a replicaset context
        final OrPut putFile = new OrPut();
        putFile.setH("localhost");// hosts, like localhost:27027,localhost:27028
        putFile.setMongo(hosts);
        putFile.setD(db);// database
        putFile.getDatabase().getCollection(bucket + ".chunks").drop();
        putFile.getDatabase().getCollection(bucket + ".files").drop();
    }

    @AfterClass
    public static void tearDown() throws ClassNotFoundException, OrFilesException {

        setUp();
    }

    @Test
    public void getReplica() throws OrFilesException {

        final URL url = getClass().getResource(file);
        final File f = new File(url.getFile());
        final File downLoadFile = new File(f.getParent(), "test.replicafile");
        downLoadFile.delete();
        Assert.assertFalse("Unit test should really not contain a stagingfile until the action is called to download it from the database.", downLoadFile.exists());

        final String md5 = Checksum.getMD5(f, false);
        add(md5, bucket, url, null, null, true);

        OrReplica getFile = new OrReplica();
        getFile.setMongo(hosts);
        getFile.setR(hosts[0]);
        getFile.setH("localhost");// hosts, like localhost:27027,localhost:27028
        getFile.setD(db);// database
        getFile.setM(md5);
        getFile.setB(bucket);
        getFile.setA(md5);
        getFile.setL(downLoadFile.getAbsolutePath());

        getFile.action();
        Assert.assertTrue("Failed to download the stagingfile...", downLoadFile.exists());
        downLoadFile.delete();
    }

    @Test
    public void getByPid() throws OrFilesException {

        final URL url = getClass().getResource(file);
        final File f = new File(url.getFile());
        final File downLoadFile = new File(f.getParent(), "test.stagingfile");
        downLoadFile.delete();
        Assert.assertFalse("Unit test should really not contain a stagingfile until the action is called to download it from the database.", downLoadFile.exists());

        final String md5 = Checksum.getMD5(f, false);
        add(md5, bucket, url, null, null, true);

        OrGet getFile = new OrGet();
        getFile.setMongo(hosts);
        getFile.setH("localhost");// hosts, like localhost:27027,localhost:27028
        getFile.setD(db);// database
        getFile.setM(md5);
        getFile.setB(bucket);
        getFile.setA(md5);
        getFile.setL(downLoadFile.getAbsolutePath());

        getFile.action();
        Assert.assertTrue("Failed to download the stagingfile...", downLoadFile.exists());
        downLoadFile.delete();
    }

    @Test
    public void getByShardkey() throws OrFilesException {

        final URL url = getClass().getResource(file);
        final File f = new File(url.getFile());
        final File downLoadFile = new File(f.getParent(), "test.stagingfile.shardkey");
        downLoadFile.delete();
        Assert.assertFalse("Unit test should really not contain a stagingfile until the action is called to download it from the database.", downLoadFile.exists());

        final String md5 = Checksum.getMD5(f, false);
        final String shardKey = Integer.toString(new Random().nextInt());
        add(md5, bucket, url, null, shardKey, true);

        OrGet getFile = new OrGet();
        getFile.setMongo(hosts);
        getFile.setH("localhost");// hosts, like localhost:27027,localhost:27028
        getFile.setD(db);// database
        getFile.setM(md5);
        getFile.setB(bucket);
        getFile.setA("rubish");
        getFile.setS(shardKey);
        getFile.setL(downLoadFile.getAbsolutePath());

        getFile.action();
        Assert.assertTrue("Failed to download the stagingfile...", downLoadFile.exists());
        downLoadFile.delete();
    }

    private void add(String pid, String bucket, URL url, String md5, String shardKey, Boolean add_files) throws OrFilesException {

        if (url == null) url = getClass().getResource(file);
        if (shardKey == null) shardKey = Integer.toString(new Random().nextInt());
        final OrPut putFile = new OrPut();


        putFile.setMongo(hosts);
        putFile.setH("localhost");// hosts, like localhost:27027,localhost:27028
        putFile.setD(db);// database
        putFile.setB(bucket);
        putFile.setM(md5);
        putFile.setL(url.getFile());
        putFile.setA(pid);
        putFile.setS(shardKey);
        putFile.setEnvironment("test");

        if (add_files) {
            final double _id = Double.parseDouble(shardKey);
            putFile.getGridFS().getDB().getCollection(bucket + ".files").save(BasicDBObjectBuilder.start("_id", _id)
                    .append("metadata", BasicDBObjectBuilder.start("pid", _id).get())
                    .get());
        }

        putFile.action();
    }

    @Test
    public void removeMD5mismatch() throws Exception {
        final URL url = getClass().getResource(file);
        final File f = new File(url.getFile());

        final String pid = Checksum.getMD5(f, false);
        add(pid, bucket, url, null, null, true);

        final String md5Liar = "000000000000FF";
        try {
            add(pid, bucket, url, md5Liar, null, true);
        } catch (OrFilesException e) {
            Assert.assertTrue("Test threw the wrong exception: " + e.getMessage() + ". Should be complaining about the md5 mismatch.", e.getMessage().contains(md5Liar));
        }


    }

    @Test
    public void update() throws OrFilesException {

        final URL url = getClass().getResource(file);
        final URL urlUpdate = getClass().getResource(fileUpdate);
        final File f = new File(url.getFile());
        final String pid = Checksum.getMD5(f, false);

        add(pid, bucket, url, null, null, true);
        add(pid, bucket, urlUpdate, null, null, true);

        final OrPut putFile = new OrPut();
        putFile.setMongo(hosts);
        putFile.setH("localhost");// hosts, like localhost:27027,localhost:27028
        putFile.setD(db);// database
        putFile.setB(bucket);
        final GridFSDBFile document = putFile.getGridFS().findOne(new BasicDBObject("metadata.pid", pid));
        Assert.assertNotNull(document);
        Assert.assertEquals(document.getMetaData().get("pid"), pid);
    }

    @Test
    public void customShardkey() throws OrFilesException {

        final URL url = getClass().getResource(file);
        final URL urlUpdate = getClass().getResource(fileUpdate);

        final String pid1 = UUID.randomUUID().toString();
        final String pid2 = UUID.randomUUID().toString();

        final String shardKey = "9007199254740992"; // maximum floating number 2^53
        final double _shardKey = Double.parseDouble(shardKey);

        add(pid1, bucket, url, null, shardKey, true);
        final OrPut putFile = new OrPut();
        putFile.setMongo(hosts);
        putFile.setH("localhost");// hosts, like localhost:27027,localhost:27028
        putFile.setD(db);// database
        putFile.setB(bucket);
        final GridFSDBFile document1 = putFile.getGridFS().findOne(new BasicDBObject("metadata.pid", pid1));
        Assert.assertTrue((Double) document1.get("_id") == _shardKey);

        boolean failure = false;
        try {
            add(pid2, bucket, urlUpdate, null, shardKey, false);
        } catch (OrFilesException e) {
            failure = true;
        }
        Assert.assertTrue("Cannot add an identical key. This ought to have been caught.", failure);

        failure = false;
        try {
            add(pid2, bucket, urlUpdate, null, "0", true);
        } catch (OrFilesException e) {
            failure = true;
        }
        Assert.assertTrue("Cannot add a shardkey with value 0.", failure);

        failure = false;
        try {
            add(pid2, bucket, urlUpdate, null, "-0", true);
        } catch (OrFilesException e) {
            failure = true;
        }
        Assert.assertTrue("Cannot add a shardkey with value 0.", failure);
    }

    @Test
    public void differentPIDs() throws OrFilesException {

        final URL url = getClass().getResource(file);
        final URL urlUpdate = getClass().getResource(fileUpdate);
        final File f = new File(url.getFile());
        final String pid = Checksum.getMD5(f, false);

        final OrPut putFile = new OrPut();
        putFile.setMongo(hosts);
        putFile.setH("localhost");// hosts, like localhost:27027,localhost:27028
        putFile.setD(db);// database
        putFile.setB(bucket);

        add(pid, bucket, url, null, null, true);
        for (int i = 0; i < 10; i++) {
            final String p = pid + "." + i;
            add(p, bucket, urlUpdate, null, null, true);
        }

        for (int i = 0; i < 10; i++) {
            final String p = pid + "." + i;
            final GridFSDBFile document = putFile.getGridFS().findOne(new BasicDBObject("metadata.pid", p));
            Assert.assertNotNull(document);
        }
    }

    @Test
    public void writeConcern() throws OrFilesException {
        final OrPut putFileSAFE = new OrPut();
        putFileSAFE.setMongo(hosts);
        Assert.assertEquals(putFileSAFE.getGridFS().getDB().getMongo().getWriteConcern().getW(), WriteConcern.SAFE.getW());

        System.setProperty("WriteConcern", "REPLICAS_SAFE");
        final OrPut putFileREPLICAS_SAFE = new OrPut();
        putFileREPLICAS_SAFE.setMongo(hosts);
        Assert.assertEquals(putFileREPLICAS_SAFE.getGridFS().getDB().getMongo().getWriteConcern().getW(), WriteConcern.REPLICAS_SAFE.getW());

        System.setProperty("WriteConcern", "I DO NOT EXIST");
        final OrPut putFileFSYNC_SAFE = new OrPut();

        boolean mustHaveError = false;
        try {
            putFileFSYNC_SAFE.setMongo(hosts);
        } catch (OrFilesException e) {
            mustHaveError = true;
        }
        Assert.assertTrue("An invalid writeconcern setting should throw an exception.", mustHaveError);

        System.setProperty("WriteConcern", "FSYNC_SAFE");
    }
}