package org.objectrepository.services;

import org.apache.commons.exec.CommandLine;
import org.apache.log4j.Logger;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
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

        ArrayList<String> keys = new ArrayList<String>();
        String[] xpaths = new String[]{"//text()", "//@*"};
        for (String xpath : xpaths) {
            try {
                parseNodes(GetNode(dom, xpath), command, keys);
            } catch (XPathExpressionException e) {
                e.printStackTrace();
                return;
            }
        }
    }

    private static void parseNodes(NodeList nodelist, CommandLine command, ArrayList<String> keys) {
        for (int i = 0; i < nodelist.getLength(); i++) {
            Node node = nodelist.item(i);
            final String value = escaping(node.getNodeValue());
            String name = (node.getNodeType() == Node.ATTRIBUTE_NODE) ? node.getNodeName() : node.getParentNode().getNodeName();
            final String key = "-" + name;
            if (value != null && !keys.contains(key)) {
                keys.add(key);
                command.addArgument(key);
                command.addArgument(value, false);
                log.debug("Added to command " + key + " = " + value);
            }
        }
    }

    private static NodeList GetNode(Node node, String xquery) throws XPathExpressionException {
        XPathExpression expr = getXPathExpression(xquery);
        return (NodeList) expr.evaluate(node, XPathConstants.NODESET);
    }

    private static XPathExpression getXPathExpression(String xquery) throws XPathExpressionException {
        XPathFactory factory = XPathFactory.newInstance();
        XPath xpath = factory.newXPath();

        // http://www.ibm.com/developerworks/library/x-javaxpathapi.html
        NamespaceContext ns = new NamespaceContext() {

            @Override
            public String getPrefix(String namespaceURI) {
                throw new UnsupportedOperationException();
            }

            @Override
            public Iterator getPrefixes(String namespaceURI) {
                throw new UnsupportedOperationException();
            }

            @Override
            public String getNamespaceURI(String prefix) {
                final String or = "http://objectrepository.org/instruction/1.0/";
                if (prefix == null)
                    throw new NullPointerException(or);

                if (prefix.equalsIgnoreCase("or"))
                    return or;

                if (prefix.equalsIgnoreCase("xml"))
                    return XMLConstants.XML_NS_URI;

                return XMLConstants.NULL_NS_URI;
            }
        };

        xpath.setNamespaceContext(ns);
        XPathExpression expr = xpath.compile(xquery);

        return expr;
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
