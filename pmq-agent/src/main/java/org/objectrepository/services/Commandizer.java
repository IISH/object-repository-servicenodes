package org.objectrepository.services;

import org.apache.commons.exec.CommandLine;
import org.apache.log4j.Logger;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * Commandizer
 * <p/>
 * Parses an Instruction type into "flat" command line parameters:
 * key value
 *
 * @author: Jozsef Gabor Bone <bonej@ceu.hu>
 */

public class Commandizer {

    public static CommandLine makeCommand(String commandToRun, String message) {

        final CommandLine command = new CommandLine(commandToRun);
        final Document dom = parseXml(makeInputStream(message));
        parseDocument(command, dom);
        if (log.isDebugEnabled()) {
            log.debug("Message was: " + message);
            log.debug("Turned into command arguments: ");
            for (String argument : command.getArguments()) {
                log.debug(argument);
            }
        }
        return command;
    }

    private static ByteArrayInputStream makeInputStream(String message) {

        ByteArrayInputStream is = null;
        try {
            is = new ByteArrayInputStream(message.getBytes("UTF8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return is;
    }

    private static Document parseXml(InputStream is) {
        //get the factory
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        Document dom = null;
        try {

            //Using factory get an instance of document builder
            DocumentBuilder db = dbf.newDocumentBuilder();

            //parse using builder to get DOM representation of the XML stagingfile
            dom = db.parse(is);

        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
        } catch (SAXException se) {
            se.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return dom;
    }

    private static void parseDocument(CommandLine command, Document dom) {

        final List<String> keys = new ArrayList<String>();

        try {
            NodeList nl = dom.getDocumentElement().getChildNodes();
            //get a nodelist of elements like task, stagingfile
            for (int i = 0; i < nl.getLength(); i++) {

                //get the child node
                org.w3c.dom.Node node = nl.item(i);

                NodeList child_nl = node.getChildNodes();

                for (int j = 0; j < child_nl.getLength(); j++) {  // access, contentType, etc
                    //get the next child node
                    org.w3c.dom.Node child_node = child_nl.item(j);

                    //If node is Element
                    if (child_node.getNodeType() == Node.ELEMENT_NODE) {
                        String key = "-" + child_node.getNodeName();
                        String value = escaping(child_node.getTextContent());
                        if (value != null && !keys.contains(key)) {
                            keys.add(key);
                            //command.addArgument("-request.instruction." + key);
                            command.addArgument(key);
                            command.addArgument(value, false);
                            log.debug("Added to command " + key + " = " + value);
                        }
                    }
                }
            }

            Element or = dom.getDocumentElement();
            NamedNodeMap attr = or.getAttributes();
            for (int i = 0; i < attr.getLength(); i++) {
                Node node = attr.item(i);
                final String value = escaping(node.getNodeValue());
                final String key = "-" + node.getNodeName();
                if (value != null && !keys.contains(key)) {
                    keys.add(key);
                    //command.addArgument("-request.instruction." + key);
                    command.addArgument(key);
                    command.addArgument(value, false);
                    log.debug("Added to command " + key + " = " + value);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * escaping
     * <p/>
     * Surrounds the argument with quotes where we have spaces in the argument
     * Escapes key tokens of Linux ( we will target this OS )
     *
     * @param text
     * @return
     */
    private static String escaping(String text) {

        if (text == null || text.trim().isEmpty())
            return null;

        final List<Character> escapeChars = new ArrayList<Character>(2);
        escapeChars.add('$');
        escapeChars.add('\\');

        final StringBuilder sb = new StringBuilder(text.trim());
        if (sb.indexOf(" ") != -1) {
            sb.insert(0, "\"");
            sb.append("\"");
        }

        for (int i = sb.length() - 1; i != -1; i--) {
            char c = sb.charAt(i);
            if (escapeChars.contains(c)) {
                sb.insert(i, "\\");
            }
        }
        return sb.toString();
    }

    private static final Logger log = Logger.getLogger(Commandizer.class);

}
