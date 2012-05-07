package org.objectrepository.instruction;

import org.objectrepository.instruction.dao.InstructionDao;
import org.objectrepository.instruction.dao.OrIterator;
import org.objectrepository.util.Normalizers;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import javax.xml.bind.JAXBElement;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stax.StAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;

/**
 * Import
 * <p/>
 * Utility for importing large OR instructions. The class implements a streamer and imports the data
 * stagingfile-by-stagingfile into a bag.
 * <p/>
 * We assume the XML has at least one -or element
 * And this -or element may have stagingfile elements.
 * <p/>
 * The global settings will persisted
 *
 * @author Lucien van Wouw <lwo@iisg.nl>
 */
final class InstructionUploadService extends ServiceBaseImp {

    @Autowired
    private InstructionDao dao;

    @Autowired
    private InstructionValidateService validateService;

    @Autowired
    private InstructionAutocreateService autocreateService;

    @Autowired
    private Jaxb2Marshaller marshaller;

    private void process(InstructionType mainInstructionType) throws Exception {


        OrIterator iterator = null;
        final XMLInputFactory xif = XMLInputFactory.newInstance();
        final File file = new File(mainInstructionType.getFileSet(), "instruction.xml");
        assert file.exists();
        final XMLStreamReader xsr = xif.createXMLStreamReader(new FileReader(file));

        while (xsr.hasNext()) {
            if (xsr.getEventType() == XMLStreamReader.START_ELEMENT) {
                String elementName = xsr.getLocalName();
                if ("instruction".equals(elementName) && iterator == null) {
                    final InstructionType fsInstructionType = addInstructionType(xsr);
                    overwrite(mainInstructionType, fsInstructionType);
                    iterator = dao.create(fsInstructionType);
                } else if (iterator != null) {
                    if ("stagingfile".equals(elementName)) {
                        StagingfileType stagingfileType = (StagingfileType) addElement(xsr);
                        stagingfileType.setLocation(Normalizers.normalize(stagingfileType.getLocation()));
                        stagingfileType.setVersion(0L);
                        autocreateService.addPid(iterator, stagingfileType);
                        validateService.isValid(iterator, stagingfileType);
                        iterator.add(stagingfileType);
                    } else {
                        hasNext(xsr);
                    }
                }
            } else {
                hasNext(xsr);
            }
        }
        build(iterator);
        dao.persist(iterator);
    }

    private void hasNext(XMLStreamReader xsr) throws XMLStreamException {
        if (xsr.hasNext()) xsr.next();
    }

    /**
     * objectFromFile
     * <p/>
     * Use to check for missing stagingfile declarations
     */
    @Override
    public void objectFromFile(File file, OrIterator instruction) {
        final String fileSet = Normalizers.toRelative(instruction.getInstruction().getFileSet(), file);
        StagingfileType stagingfileType = instruction.getFileByLocation(fileSet);
        if (stagingfileType == null) {
            log.debug("objectFromFile.getFileByLocation found nothing: " + fileSet);
            instruction.add(validateService.setMissingSection(instruction.getInstruction().getFileSet(), file));
        }
    }

    /**
     * overwrite
     * <p/>
     * Key system settings will overrule those set in the instruction
     *
     * @param fsInstructionType
     * @throws Exception
     */
    private void overwrite(InstructionType mainInstructionType, InstructionType fsInstructionType) throws Exception {
        fsInstructionType.setNa(mainInstructionType.getNa());
        fsInstructionType.setFileSet(Normalizers.normalize(mainInstructionType.getFileSet()));
        fsInstructionType.setTask(mainInstructionType.getTask());
        fsInstructionType.setVersion(0L);

    }

    private Object addElement(XMLStreamReader xsr) {

        final TransformerFactory tf = TransformerFactory.newInstance();
        final Transformer transformer;// Identity template.
        try {
            transformer = tf.newTransformer();
        } catch (TransformerConfigurationException e) {
            log.warn(e);
            return null;
        }
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            transformer.transform(new StAXSource(xsr), new StreamResult(baos));
        } catch (TransformerException e) {
            log.warn(e);
            return null;
        }
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        StreamSource source = new StreamSource(bais);
        JAXBElement o;
        try {
            o = (JAXBElement) marshaller.unmarshal(source);
        } catch (Exception e) {
            log.warn(e);
            return null;
        }
        return o.getValue();
    }

    private InstructionType addInstructionType(XMLStreamReader xsr) {

        StringBuilder sb = new StringBuilder("<instruction xmlns=\"http://objectrepository.org/instruction/1.0/\"");
        for (int i = 0; i < xsr.getAttributeCount(); i++) {
            sb.append(" ");
            sb.append(xsr.getAttributeLocalName(i));
            sb.append("=");
            sb.append("\"");
            sb.append(xsr.getAttributeValue(i));
            sb.append("\"");
        }
        sb.append("/>");
        ByteArrayInputStream bais = null;
        try {
            bais = new ByteArrayInputStream(sb.toString().getBytes("utf-8"));
        } catch (UnsupportedEncodingException e) {

            e.printStackTrace();
        }
        StreamSource source = new StreamSource(bais);
        JAXBElement<InstructionType> o = (JAXBElement<InstructionType>) marshaller.unmarshal(source);
        return o.getValue();
    }

    public void bulkImport(InstructionType mainInstructionType) throws Exception {
        process(mainInstructionType);
    }

    private static Logger log = Logger.getLogger(InstructionUploadService.class);


}
