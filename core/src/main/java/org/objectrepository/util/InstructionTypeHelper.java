package org.objectrepository.util;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.json.JettisonMappedXmlDriver;
import org.apache.log4j.Logger;
import org.objectrepository.instruction.InstructionType;
import org.objectrepository.instruction.StagingfileType;
import org.objectrepository.instruction.ObjectFactory;
import org.objectrepository.instruction.TaskType;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import javax.xml.bind.JAXBElement;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;

/**
 * InstructionTypeHelper
 * <p/>
 * Helpers for streaming to and from the InstructionType model
 */
public class InstructionTypeHelper {

    private static Jaxb2Marshaller marshaller = new Jaxb2Marshaller();

    static {
        marshaller.setContextPath(InstructionType.class.getPackage().getName());
    }

    public static InstructionType instructionTypeFromFile(File file) throws FileNotFoundException {

        FileInputStream fis = new FileInputStream(file);
        Source source = new StreamSource(fis);
        JAXBElement<InstructionType> instruction = (JAXBElement<InstructionType>) marshaller.unmarshal(source);
        return instruction.getValue();
    }

    public static InstructionType instructionTypeFromJson(String text) {

        XStream xstream = new XStream(new JettisonMappedXmlDriver());
        xstream.setMode(XStream.NO_REFERENCES);
        xstream.alias("instruction", InstructionType.class);
        xstream.alias("stagingfile", StagingfileType.class);
        xstream.alias("task", TaskType.class);
        return (InstructionType) xstream.fromXML(text);
    }

    public static String instructionTypeToJson(InstructionType instructionType) {

        XStream xstream = new XStream(new JettisonMappedXmlDriver());
        xstream.setMode(XStream.NO_REFERENCES);
        xstream.alias("instruction", InstructionType.class);
        xstream.alias("stagingfile", StagingfileType.class);
        xstream.alias("task", TaskType.class);
        return xstream.toXML(instructionType);
    }

    public static InstructionType stringToInstructionType(String text) throws Exception {

        ByteArrayInputStream is = new ByteArrayInputStream(text.getBytes("UTF8"));
        StreamSource streamsource = new StreamSource(is);
        JAXBElement<InstructionType> instruction = (JAXBElement<InstructionType>) marshaller.unmarshal(streamsource);
        return instruction.getValue();
    }

    public static String instructionTypeToString(InstructionType message) throws Exception {

        ObjectFactory objectFactory = new ObjectFactory();
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        Result result = new StreamResult(os);
        JAXBElement<InstructionType> InstructionTypeJAXBElement = objectFactory.createInstruction(message);
        marshaller.marshal(InstructionTypeJAXBElement, result);
        return new String(os.toByteArray());
    }

    /**
     * getValue
     * <p/>
     * Retrieves a value from an attribute.
     * First the child element StagingfileType is used to get the value.
     * If this is empty, it will fallback on the parent InstructionType global element
     *
     * @param instructionType
     * @param stagingfileType
     * @param key
     * @return
     */
    public static Object getValue(InstructionType instructionType, StagingfileType stagingfileType, String key) {

        String getter = "get" + key.substring(0, 1).toUpperCase() + key.substring(1);
        Object value = null;
        try {
            value = StagingfileType.class.getMethod(getter, null).invoke(stagingfileType, null);
        } catch (Exception e) {
        }
        if (value == null) {
            try {
                value = InstructionType.class.getMethod(getter, null).invoke(instructionType, null);
            } catch (Exception e) {
            }
        }
        return value;
    }

    private final static Logger log = Logger.getLogger(InstructionTypeHelper.class);
}
