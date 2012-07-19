package org.objectrepository;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Starts the daemon application.
 *
 * @author Lucien van Wouw <lwo@iisg.nl>
 */
public class Startup {

    public static void main(String[] args) throws IOException {

        File folder = new File("./pmq-agent/target");
        folder.mkdirs();
        String[] testFiles = new String[]{"TestTask1", "TestTask2.2", "TestTask3.3"};
        for (int i = 0; i < testFiles.length; i++) {
            File file = new File(folder, testFiles[i]);
            file.mkdir();
            FileOutputStream fos = new FileOutputStream(file.getAbsolutePath() + "/startup.sh", false);
            fos.write(0);
            fos.close();
        }

        if (args.length == 0) {
            args = new String[]{"-messageQueues", folder.getAbsolutePath(), "-identifier", "12345"};
        }

        final String or = "or.properties";
        if (!System.getProperties().contains(or))
            System.setProperty(or, "or.properties"); // Set this as a VM parameter -Dor.properties
        MessageConsumerDaemon.main(args);
    }
}
