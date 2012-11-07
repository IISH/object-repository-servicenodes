/*
 * Copyright (c) 2010-2011 Social History Services
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.objectrepository.util;

import org.objectrepository.util.md5.MD5;
import org.apache.log4j.Logger;

import java.io.*;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;


/*
* Checksum
*
* Calculate a hash for a string or stagingfile
*
* @author: Lucien van Wouw <lwo@iisg.nl>
*/
public class Checksum {

    private final static Logger log = Logger.getLogger(Checksum.class);

    public static String getMD5(String text) {
        String md5 = null;
        try {
            md5 = md5FromString(text);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return md5;
    }

    private static String md5FromString(String text) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(text.getBytes());
        byte[] mdbytes = md.digest();
        return getHex(mdbytes);
    }

    private static String getHex(byte[] mdbytes) {
        StringBuffer sb = new StringBuffer();
        for (byte mdbyte : mdbytes) {
            String hex = Integer.toHexString(0xff & mdbyte);
            if (hex.length() == 1) sb.append('0');
            sb.append(hex);
        }
        return sb.toString();
    }

    /**
     * getMD5
     * <p/>
     * Retrieves or calculates and MD5 hash
     * <p/>
     * To obtain a has we first append to the filename a ".md5" string and
     * see if such a file exists. If so, we use the content of that file as a hash.
     * <p/>
     * If this fails, we try to use the native system's way of calculating the hash.
     * This ought to be set in the md5sum system property. For example linux distributions offer
     * md5sum
     * <p/>
     * If this fails, we will calculate the hash ourselves.
     *
     * @param file
     * @return
     */
    public static String getMD5(File file) {

        final Date start = new Date();
        String md5 = null;
        try {
            md5 = metadataFile(file);
        } catch (IOException e) {
            log.warn(e);
        }

        if (md5 == null) {
            md5 = createMD5file(file, md5);
        }

        long time = (new Date().getTime() - start.getTime());
        log.debug(String.valueOf(time / 1000) + " " + md5);

        return md5;
    }

    public static String getMD5(byte[] writeBuffer) {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
        md.update(writeBuffer);
        byte[] mdbytes = md.digest();
        return getHex(mdbytes);
    }

    private static String createMD5file(File file, String md5) {
        try {
            final String md5sum = System.getProperty("md5sum");
            if (md5sum == null) {
                md5 = MD5.asHex(MD5.getHash(file));
            } else {
                md5 = nativeMd5(md5sum, file);
            }
        } catch (Exception e) {
            log.error(e);
        }

        final String md5File = file + ".md5";
        try {
            FileOutputStream fos = new FileOutputStream(md5File);
            final String line = md5 + "  " + file.getAbsoluteFile();
            fos.write(line.getBytes("utf-8"));
            fos.close();
        } catch (IOException e) {
            log.warn(e.getMessage());
        }
        return md5;
    }

    /**
     * metadataFile
     * <p/>
     * During an ftp upload there could be a stagingfile in the [stagingfile name].md5 containing a checksum.
     * We use that if it is there.
     *
     * @param file
     * @return
     */
    private static String metadataFile(File file) throws IOException {

        final File md5File = new File(file.getAbsoluteFile() + ".md5");
        return (md5File.exists() && md5File.length() != 0) ? parse(new FileInputStream(md5File)) : null;
    }

    /**
     * Calls an external native md5sum application to calculate a md5
     * This assumes there is such a program.
     *
     * @param file
     * @return
     * @throws IOException
     */
    private static String nativeMd5(String md5sum, File file) throws IOException {

        final String command = md5sum + " " + file.getAbsolutePath();
        final Process process = Runtime.getRuntime().exec(command);
        return parse(process.getInputStream());
    }

    private static String parse(InputStream is) throws IOException {

        final BufferedReader in = new BufferedReader(new InputStreamReader(is));
        final StringBuilder sb = new StringBuilder(32);

        String line;
        while ((line = in.readLine()) != null) {
            sb.append(line);
        }
        in.close();

        int end = sb.indexOf(" ");
        return (end == -1) ? sb.toString() : sb.substring(0, end);
    }

    /**
     * Compares two md5 hashes. We cannot rely only on string comparisons, as some
     * hash implementations do not render a zero's at the left side of the string.
     */
    public static Boolean compare(String md5_A, String md5_B) {

        if (md5_A == null || md5_B == null) return false;

        final BigInteger md5_alfa;
        final BigInteger md5_beta;

        try {
            md5_alfa = new BigInteger(md5_A, 16);
            md5_beta = new BigInteger(md5_B, 16);
        } catch (NumberFormatException e) {
            log.warn(e.getMessage());
            return false;
        }

        return md5_beta.compareTo(md5_alfa) == 0;
    }

    /**
     * getMD5as32Characters
     * <p/>
     * Adds left padded zeros
     *
     * @param md5
     * @return
     */
    public static String getMD5as32Characters(String md5) {
        return String.format("%32s", md5).replace(' ', '0');
    }

    /**
     * Return the checksum
     *
     * @param args
     */
    public static void main(String[] args) throws IOException {

        final File file = new File(args[0]);
        final String md5 = getMD5(file);
        if (args.length == 1) {
            System.out.print(md5 + "  " + file.getAbsolutePath());
        } else {
            System.out.print(md5);
        }
    }
}
