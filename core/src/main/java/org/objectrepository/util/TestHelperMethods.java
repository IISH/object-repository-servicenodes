package org.objectrepository.util;

import java.io.File;

public class TestHelperMethods {

    public static String getFileSet() {

        String tmp = System.getProperty("fileSet");
        if (tmp == null) {
            tmp = "instruction-manager/src/test/resources/home/12345/folder_of_cpuser/test-collection/";
            File file = new File(tmp);
            if (!file.exists()) file = new File("./" + tmp);
            if (!file.exists()) file = new File("../" + tmp);
            if (!file.exists()) file = new File("../../" + tmp);
            tmp = file.getAbsolutePath();
        }
        return tmp;
    }
}
