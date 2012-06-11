
package org.objectrepository.instruction;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.objectrepository.instruction package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _Instruction_QNAME = new QName("http://objectrepository.org/instruction/1.0/", "instruction");
    private final static QName _Stagingfile_QNAME = new QName("http://objectrepository.org/instruction/1.0/", "stagingfile");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.objectrepository.instruction
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link StagingfileType }
     * 
     */
    public StagingfileType createStagingfileType() {
        return new StagingfileType();
    }

    /**
     * Create an instance of {@link TaskType }
     * 
     */
    public TaskType createTaskType() {
        return new TaskType();
    }

    /**
     * Create an instance of {@link InstructionType }
     * 
     */
    public InstructionType createInstructionType() {
        return new InstructionType();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link InstructionType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://objectrepository.org/instruction/1.0/", name = "instruction")
    public JAXBElement<InstructionType> createInstruction(InstructionType value) {
        return new JAXBElement<InstructionType>(_Instruction_QNAME, InstructionType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StagingfileType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://objectrepository.org/instruction/1.0/", name = "stagingfile")
    public JAXBElement<StagingfileType> createStagingfile(StagingfileType value) {
        return new JAXBElement<StagingfileType>(_Stagingfile_QNAME, StagingfileType.class, null, value);
    }

}
