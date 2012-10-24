package org.objectrepository.io;

import com.mongodb.BasicDBObject;
import com.mongodb.gridfs.GridFSDBFile;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.objectrepository.exceptions.OrFilesException;
import org.objectrepository.util.Checksum;

import java.io.File;
import java.net.URL;
import java.util.UUID;

public class OrFilesTest {

    final static private String[] hosts = new String[]{"localhost"};
    final static private String file = "/xxxxxxxx-xxxx-Mxxx-Nxxx-xxxxxxxxxxxx";
    final static private String fileUpdate = "/xxxxxxxx-xxxx-Mxxx-Nxxx-xxxxxxxxxxxx.update";
    final static private String db = "test";
    final static private String bucket = "level12345";

    @BeforeClass
    public static void setUp() throws ClassNotFoundException {

        final OrPut putFile = new OrPut();
        putFile.setH("localhost");// hosts, like localhost:27027,localhost:27028
        putFile.setMongo(hosts);
        putFile.setD(db);// database
        putFile.getDatabase().dropDatabase();
    }

    @AfterClass
    public static void tearDown() throws ClassNotFoundException {

        setUp();
    }

    @Test
    public void get() throws OrFilesException {

        final URL url = getClass().getResource(file);
        final File f = new File(url.getFile());
        final File downLoadFile = new File(f.getParent(), "test.stagingfile");
        downLoadFile.delete();
        Assert.assertFalse("Unit test should really not contain a stagingfile until the action is called to download it from the database.", downLoadFile.exists());

        final String md5 = Checksum.getMD5(f);
        add(md5, bucket, url, null, null);

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

    private void add(String pid, String bucket, URL url, String md5, String shardKey) throws OrFilesException {

        if (url == null) url = getClass().getResource(file);
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
        putFile.action();
    }

    @Test
    public void removeMD5mismatch() throws Exception {
        final URL url = getClass().getResource(file);
        final File f = new File(url.getFile());

        final String pid = Checksum.getMD5(f);
        add(pid, bucket, url, null, null);

        final String md5Liar = "000000000000FF";
        try {
            add(pid, bucket, url, md5Liar, null);
        } catch (OrFilesException e) {
            Assert.assertTrue("Test threw the wrong exception: " + e.getMessage() + ". Should be complaining about the md5 mismatch.", e.getMessage().contains(md5Liar));
        }
    }

    @Test
    public void update() throws OrFilesException {

        final URL url = getClass().getResource(file);
        final URL urlUpdate = getClass().getResource(fileUpdate);
        final File f = new File(url.getFile());
        final String pid = Checksum.getMD5(f);

        add(pid, bucket, url, null, null);
        add(pid, bucket, urlUpdate, null, null);

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
        final File f = new File(url.getFile());

        final String pid1 = UUID.randomUUID().toString();
        final String pid2 = UUID.randomUUID().toString();

        add(pid1, bucket, url, null, "123");
        add(pid2, bucket, urlUpdate, null, "123");

        final OrPut putFile = new OrPut();
        putFile.setMongo(hosts);
        putFile.setH("localhost");// hosts, like localhost:27027,localhost:27028
        putFile.setD(db);// database
        putFile.setB(bucket);

        final GridFSDBFile document1 = putFile.getGridFS().findOne(new BasicDBObject("metadata.pid", pid1));
        Assert.assertTrue((Integer) document1.get("_id") == 123);

        final GridFSDBFile document2 = putFile.getGridFS().findOne(new BasicDBObject("metadata.pid", pid2));
        Assert.assertTrue((Integer) document2.get("_id") != 123);


    }

    @Test
    public void differentPIDs() throws OrFilesException {

        final URL url = getClass().getResource(file);
        final URL urlUpdate = getClass().getResource(fileUpdate);
        final File f = new File(url.getFile());
        final String pid = Checksum.getMD5(f);

        final OrPut putFile = new OrPut();
        putFile.setMongo(hosts);
        putFile.setH("localhost");// hosts, like localhost:27027,localhost:27028
        putFile.setD(db);// database
        putFile.setB(bucket);

        add(pid, bucket, url, null, null);
        for (int i = 0; i < 10; i++) {
            final String p = pid + "." + i;
            add(p, bucket, urlUpdate, null, null);
        }

        for (int i = 0; i < 10; i++) {
            final String p = pid + "." + i;
            final GridFSDBFile document = putFile.getGridFS().findOne(new BasicDBObject("metadata.pid", p));
            Assert.assertNotNull(document);
        }
    }
}