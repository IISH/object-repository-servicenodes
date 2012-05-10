/*
 * Copyright (c) 2012 Social History Services
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

import org.apache.log4j.Logger;

import java.io.*;
import java.text.MessageFormat;
import java.util.Properties;

/**
 * Required parameters at startup are loaded into the properties list.
 * We will not start unless these have been declared in a properties stagingfile.
 * <p/>
 * Set the property stagingfile in a VM parameter: -Dor.properties=[/path/to/[filename].properties]
 */
public class PropertyPlaceHolder extends Properties {

    private static final long serialVersionUID = 1L;
    private static Logger log = Logger.getLogger(PropertyPlaceHolder.class);

    public PropertyPlaceHolder() {

        final String systemProperty = "or.properties";
        final String environmentProperty = "OR";
        String sorProperties = "";
        InputStream inputStream = null;
        try {
            // Should be added at startup
            sorProperties = System.getProperty(systemProperty);
            if (sorProperties != null) {
                log.info("Found system property '" + systemProperty + "', resolved to " + new File(sorProperties).getAbsolutePath());
            }
            inputStream = getInputFromFile(sorProperties);
            if (inputStream == null) {
                log.info("System property '" + systemProperty + "' not found, checking environment for '" + systemProperty + "'.");
                sorProperties = System.getenv(environmentProperty);
                if (sorProperties != null) {
                    log.info("Found env property '" + environmentProperty + "', resolved to " + new File(sorProperties).getAbsolutePath());
                }
                inputStream = getInputFromFile(sorProperties);
            }
        } catch (Exception e) {
            log.fatal("Error in resolving property stagingfile defined with " + sorProperties + " : " + e.getMessage());
            System.exit(1);
        }
        if (inputStream == null) {
            log.fatal(
                    "Configuration not available!\n" +
                            "Solutions:\n" +
                            "1) Start the JVM with parameter -D" + systemProperty + "=/path/to/[filename].properties\n" +
                            "2) Set the environment variable '" + environmentProperty + "' to /path/to/filename.properties"
            );
            System.exit(1);
        }
        try {
            load(inputStream);
        } catch (IOException e) {
            log.fatal("Unable to load '" + systemProperty + "'.properties' from input stream!");
            System.exit(1);
        }
        boolean complete = true;
        for (String expect : EXPECT) {
            String value = getProperty(expect);
            if (value == null) {
                log.warn(MessageFormat.format("Missing property ''{0}''", expect));
                complete = false;
            }
        }
        if (!complete) {
            log.fatal("Configuration properties incomplete. Check log of this class for warnings.");
            System.exit(1);
        }
    }

    private InputStream getInputFromFile(String filePath) {
        if (filePath != null) {
            try {
                log.info("Going to load properties from '" + filePath + "', resolved to " + new File(filePath).getCanonicalPath());
                return new FileInputStream(filePath);
            } catch (FileNotFoundException e) {
                throw new RuntimeException("No properties file found: " + filePath, e);
            } catch (IOException e) {
                throw new RuntimeException("IO exception on: " + filePath, e);
            }
        } else {
            return null;
        }
    }

    private static String[] EXPECT = {
            "log4j.xml",
    };
}