package org.objectrepository.util.md5;

import junit.framework.Assert;
import org.junit.Test;
import org.objectrepository.util.Checksum;
import org.objectrepository.util.TestHelperMethods;

import java.io.File;
import java.io.IOException;

public class ChecksumTest {

    @Test
    public void testCompare() throws IOException {

        String m1 = "a12356eb46723a43c70788b9867eb80";
        String m2 = "0a12356eb46723a43c70788b9867eb80";

        Assert.assertTrue(Checksum.compare(m1, m2));

        final String jk8ssl = Checksum.getMD5("jk8ssl");
        final String j2 = "18e6137ac2caab16074784a6";
        Assert.assertFalse(jk8ssl.length() == j2.length());
        Assert.assertTrue(Checksum.compare(jk8ssl, j2));

        Assert.assertFalse(Checksum.compare(m1, jk8ssl));
        Assert.assertFalse(Checksum.compare(m2, jk8ssl));
        Assert.assertFalse(Checksum.compare(m1, j2));
        Assert.assertFalse(Checksum.compare(m2, j2));
    }

    @Test
    public void testGetMD5as32Characters() {

        final String j2 = "18e6137ac2caab16074784a6";
        final String md5as32Characters = Checksum.getMD5as32Characters(j2);
        Assert.assertFalse(j2.length() == md5as32Characters.length());

        Assert.assertEquals(md5as32Characters.length(), 32);
    }

    @Test
    public void testNative() {
        String fileSet = TestHelperMethods.getFileSet();
        File folder = new File(fileSet);
        for (File file : folder.listFiles()) {
            if (file.isFile()) {
                final String md5 = Checksum.getMD5(file, true);
                Assert.assertNotNull(md5);
            }
        }
        for (File file : folder.listFiles()) {
            if ( file.getAbsolutePath().endsWith(".md5")) {
                file.delete();
            }
        }
    }
}
