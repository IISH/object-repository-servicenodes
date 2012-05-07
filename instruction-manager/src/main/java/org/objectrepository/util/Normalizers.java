package org.objectrepository.util;

import org.apache.commons.io.FilenameUtils;

import java.io.File;

public class Normalizers {

    public static String normalize(File file) {
        return normalize(file.getAbsolutePath());
    }

    /**
     * Ensures all fileformats are absolute and with forward slash and without dot or driveletter
     *
     * @param absolutePath
     * @return
     */
    public static String normalize(String absolutePath) {
        if (absolutePath == null) return null;
        String s = absolutePath.trim().replaceAll("\\n|\\r|\\t", "");
        return FilenameUtils.separatorsToUnix(s.replaceFirst("[A-Za-z][:]", ""));
    }

    /**
     * toRelative
     * <p/>
     * Expresses the location of the stagingfile as a relative path to the fileset and starting from the first folder.
     * <p/>
     * The relative path of a stagingfile is then: first folder of fileSet + location
     * We do not write an absolute location to preserve the stagingfile view of the staging area.
     * <p/>
     * That is: the sftp client's perspective of the staging area is identical to that of the stagingfile location.
     *
     * @param fileSet
     * @param location
     * @return
     */
    public static String toRelative(String fileSet, String location) {
        String _fileSet = normalize(fileSet);
        String _location = normalize(location);
        File folder = new File(fileSet);
        return "/" + folder.getName() + _location.substring(_fileSet.length());
    }

    public static String toRelative(String fileSet, File file) {
        return toRelative(fileSet, file.getPath());
    }

    /**
     * Converts a relative stagingfile specification into an absolute fs path.
     *
     * @return
     */
    public static File toAbsolute(String fileSet, String location) {
        return new File(FilenameUtils.getFullPath(fileSet) + location);
    }

    public static boolean isEmpty(String text) {
        return (text == null) ? true : text.isEmpty();
    }

    public static boolean isTrue(String key, boolean Default) {
        String value = System.getProperty(key);
        if (value == null) return Default;
        return Boolean.parseBoolean(value);
    }

    public static boolean isTrue(String key) {
        return isTrue(key, false);
    }

}
