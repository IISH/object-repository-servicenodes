package org.objectrepository.io;

import org.objectrepository.exceptions.OrFilesException;
import org.objectrepository.utils.Invocations;
import org.apache.log4j.Logger;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

/**
 * Main class entry point for console app.
 *
 * @author: Lucien van Wouw <lwo@iisg.nl>
 */
public final class Console {

    public void run(String[] argv) throws OrFilesException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {

        Map<String, String> defaults = setDefaults();
        final Map<String, String> args = loadArgs(argv);

        checkArgs(defaults, args);

        final OrFiles OrFiles = OrFilesFactory.newInstance(args.get("-M"));
        defaults.remove("-M");
        for (String key : defaults.keySet()) {
            String value = args.get(key);
            Invocations.invoke(OrFiles, "set" + Invocations.camelCase(key.substring(1)), value, String.class);
        }

        final String[] hosts = args.get("-h").split("\\s|,|;|\t");
        OrFiles.setMongo(hosts);
        OrFiles.action();
        OrFiles.close();
    }

    private Map<String, String> setDefaults() {
        Map<String, String> defaults = new HashMap();
        defaults.put("-h", "localhost");// hosts, like localhost:27027,localhost:27028
        defaults.put("-d", "test");// database
        defaults.put("-b", "fs"); // Bucket... in fact... the bucket name. Default is "fs"
        defaults.put("-l", null); // filename
        defaults.put("-q", "{}"); // query like: {md5:"de8f392jd238992ej8923"}
        defaults.put("-m", null);// the md5
        defaults.put("-c", "files");// the collection. Default is "files"
        defaults.put("-a", null);// the alias
        defaults.put("-s", "0");// the shardKey prefix
        defaults.put("-t", "application/octet-stream");// the contentType
        defaults.put("-r", ""); // alternative replica set
        defaults.put("-M", "Get"); // method
        return defaults;
    }

    /**
     * @param argv
     * @return
     */
    private Map<String, String> loadArgs(String[] argv) {

        log.info("Arguments passed: ");
        for (int i = 0; i < argv.length; i++) {
            log.info("arg value=" + argv[i]);
        }
        log.info("Arguments length: " + argv.length);

        final Map<String, String> map = new HashMap(argv.length);
        for (int i = 0; i < argv.length; i += 2) {
            map.put(argv[i], argv[i + 1]);
        }
        return map;
    }

    private void checkArgs(Map<String, String> defaults, Map<String, String> args) throws OrFilesException {
        for (String key : defaults.keySet()) {
            String value = defaults.get(key);
            if (!args.containsKey(key)) {
                if (value == null) {
                    throw new OrFilesException("Parameter " + key + " is required.");
                }
                log.info("Using default for " + key + " = " + value + "\n");
                args.put(key, value);
            } else {
                value = args.get(key);
                if (value == null)
                    throw new OrFilesException("Parameter " + key + " is required; or it is optional and cannot be empty.");
                log.info("Use argument: " + key + "=" + value);
            }
        }
    }

    /**
     * @param argv -h=hosts ( urls  + port ), -d=database, -c = bucket collection ( prefix for files namespace ),
     *             -l = stagingfile; -q = query, m = update command, -M = method ( PUT or GET )
     */

    public static void main(String[] argv) {
        Console c = new Console();
        try {
            c.run(argv);
        } catch (Exception e) {
            log.fatal(e);
            System.exit(1);
        }
    }

    private static Logger log = Logger.getLogger(Console.class);
}
