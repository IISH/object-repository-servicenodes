package org.objectrepository.io;

import org.junit.*;
import org.objectrepository.exceptions.OrFilesException;
import org.objectrepository.util.Checksum;

import java.io.File;
import java.net.URL;

public class OrFilesTest {

    final static private String[] hosts = new String[]{"localhost"};
    final static private String file = "/xxxxxxxx-xxxx-Mxxx-Nxxx-xxxxxxxxxxxx";
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
        add(md5, bucket, url, null);

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

    private void add(String pid, String bucket, URL url, String md5) throws OrFilesException {

        if (url == null) url = getClass().getResource(file);
        final OrPut putFile = new OrPut();
        putFile.setMongo(hosts);
        putFile.setH("localhost");// hosts, like localhost:27027,localhost:27028
        putFile.setD(db);// database
        putFile.setB(bucket);
        putFile.setM(md5);
        putFile.setL(url.getFile());
        putFile.setA(pid);
        putFile.setEnvironment("test");
        putFile.action();
    }

    @Test
    public void removeMD5mismatch() throws Exception {
        final URL url = getClass().getResource(file);
        final File f = new File(url.getFile());
        final File downLoadFile = new File(f.getParent(), "test.stagingfile");
        downLoadFile.delete();
        Assert.assertFalse("Unit test should really not contain a stagingfile until the action is called to download it from the database.", downLoadFile.exists());

        final String pid = Checksum.getMD5(f);
        add(pid, bucket, url, null);

        final String md5Liar = "000000000000FF";
        try {
            add(pid, bucket, url, md5Liar);
        } catch (OrFilesException e) {
            Assert.assertTrue("Test threw the wrong exception: " + e.getMessage() + ". Should be complaining about the md5 mismatch.", e.getMessage().contains(md5Liar));
        }
    }
}
