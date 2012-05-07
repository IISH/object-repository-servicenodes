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
        add(md5, bucket, url);

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

    private void add(String pid, String bucket, URL url) throws OrFilesException {

        if (url == null) url = getClass().getResource(file);
        final OrPut putFile = new OrPut();
        putFile.setMongo(hosts);
        putFile.setH("localhost");// hosts, like localhost:27027,localhost:27028
        putFile.setD(db);// database
        putFile.setB(bucket);
        putFile.setL(url.getFile());
        putFile.setA(pid);
        putFile.action();
    }
}
