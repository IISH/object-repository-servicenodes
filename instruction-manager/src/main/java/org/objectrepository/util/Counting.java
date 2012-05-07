package org.objectrepository.util;

import java.io.File;

public class Counting {

    private static int limit = 10000;

    /**
     * Counts the total number of files in the fileSet.
     * Stops counting after the limit of 10000 has been reached.
     * <p/>
     * Files that begin with a dot can be ignored.
     *
     * @param fileSet
     * @return
     */
    public static int  countFiles(String fileSet) {
        return countFiles(new File(fileSet));
    }

    private static int countFiles(File folder) {

        int count = 0;

        File[] files = folder.listFiles();
        for (File file : files) {
            final String name = file.getName();
            if (skip(name)) continue;
            if (file.isDirectory()) {
                count += countFiles(file);
                if (count > limit)
                    break;
            } else {
                count++;
            }
        }
        return count;
    }

    public static boolean skip(String name) {
        return name.startsWith(".") || name.equals("instruction.xml") || name.endsWith(".md5");
    }
}
