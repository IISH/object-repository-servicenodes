package org.objectrepository;

import java.util.Properties;

/**
 * Starts the daemon application.
 *
 * @author Lucien van Wouw <lwo@iisg.nl>
 */
public class Startup {

    public static void main(String[] args) {

        if (args.length == 0)
            args = new String[]{"-messageQueue", "activemq:controller"};

        final String or = "or.properties";
        if (!System.getProperties().contains(or))
            System.setProperty(or, "or.properties"); // Set this as a VM parameter -Dor.properties
        String[] params = new String[]{};
        //MessageConsumerDaemon.main(params);

        Properties p = new Properties();
        p.put("-maxTasks", "3");
        p.put("-messageQueue", "InstructionAutocreate");
        p.put("-shellScript", "notepad.exe");

        MessageConsumerDaemon services = MessageConsumerDaemon.getInstance(p);
        MessageConsumerDaemon.main(params);
        services.run(); // Don't use start... we need the IDE thread to hang here.
    }
}
