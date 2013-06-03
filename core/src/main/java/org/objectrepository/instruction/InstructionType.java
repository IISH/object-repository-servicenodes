
package org.objectrepository.instruction;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for InstructionType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="InstructionType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="workflow" type="{http://objectrepository.org/instruction/1.0/}taskType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element ref="{http://objectrepository.org/instruction/1.0/}stagingfile" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="plan" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="fileSet" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="label" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="autoGeneratePIDs" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="autoIngestValidInstruction" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *       &lt;attribute name="resolverBaseUrl" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="na" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="contentType" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="access" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="embargo" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="embargoAccess" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="action">
 *         &lt;simpleType>
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *             &lt;enumeration value="add"/>
 *             &lt;enumeration value="update"/>
 *             &lt;enumeration value="upsert"/>
 *             &lt;enumeration value="delete"/>
 *           &lt;/restriction>
 *         &lt;/simpleType>
 *       &lt;/attribute>
 *       &lt;attribute name="pidwebserviceEndpoint" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="pidwebserviceKey" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="version" type="{http://www.w3.org/2001/XMLSchema}long" default="0" />
 *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="objid" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="deleteCompletedInstruction" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="replaceExistingDerivatives" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="pdfLevel" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "InstructionType", namespace = "http://objectrepository.org/instruction/1.0/", propOrder = {
    "workflow",
    "stagingfile"
})
public class InstructionType {

    @XmlElement(namespace = "http://objectrepository.org/instruction/1.0/")
    protected List<TaskType> workflow;
    @XmlElement(namespace = "http://objectrepository.org/instruction/1.0/")
    protected List<StagingfileType> stagingfile;
    @XmlAttribute
    @XmlSchemaType(name = "anySimpleType")
    protected String plan;
    @XmlAttribute
    @XmlSchemaType(name = "anySimpleType")
    protected String fileSet;
    @XmlAttribute
    @XmlSchemaType(name = "anySimpleType")
    protected String label;
    @XmlAttribute
    @XmlSchemaType(name = "anySimpleType")
    protected String autoGeneratePIDs;
    @XmlAttribute
    protected Boolean autoIngestValidInstruction;
    @XmlAttribute
    @XmlSchemaType(name = "anySimpleType")
    protected String resolverBaseUrl;
    @XmlAttribute
    @XmlSchemaType(name = "anySimpleType")
    protected String na;
    @XmlAttribute
    @XmlSchemaType(name = "anySimpleType")
    protected String contentType;
    @XmlAttribute
    @XmlSchemaType(name = "anySimpleType")
    protected String access;
    @XmlAttribute
    protected String embargo;
    @XmlAttribute
    protected String embargoAccess;
    @XmlAttribute
    protected String action;
    @XmlAttribute
    protected String pidwebserviceEndpoint;
    @XmlAttribute
    protected String pidwebserviceKey;
    @XmlAttribute
    protected Long version;
    @XmlAttribute
    protected String id;
    @XmlAttribute
    protected String objid;
    @XmlAttribute
    protected String deleteCompletedInstruction;
    @XmlAttribute
    protected String replaceExistingDerivatives;
    @XmlAttribute
    protected String pdfLevel;

    /**
     * Gets the value of the workflow property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the workflow property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getWorkflow().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TaskType }
     * 
     * 
     */
    public List<TaskType> getWorkflow() {
        if (workflow == null) {
            workflow = new ArrayList<TaskType>();
        }
        return this.workflow;
    }

    /**
     * Gets the value of the stagingfile property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the stagingfile property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getStagingfile().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link StagingfileType }
     * 
     * 
     */
    public List<StagingfileType> getStagingfile() {
        if (stagingfile == null) {
            stagingfile = new ArrayList<StagingfileType>();
        }
        return this.stagingfile;
    }

    /**
     * Gets the value of the plan property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPlan() {
        return plan;
    }

    /**
     * Sets the value of the plan property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPlan(String value) {
        this.plan = value;
    }

    /**
     * Gets the value of the fileSet property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFileSet() {
        return fileSet;
    }

    /**
     * Sets the value of the fileSet property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFileSet(String value) {
        this.fileSet = value;
    }

    /**
     * Gets the value of the label property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLabel() {
        return label;
    }

    /**
     * Sets the value of the label property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLabel(String value) {
        this.label = value;
    }

    /**
     * Gets the value of the autoGeneratePIDs property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAutoGeneratePIDs() {
        return autoGeneratePIDs;
    }

    /**
     * Sets the value of the autoGeneratePIDs property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAutoGeneratePIDs(String value) {
        this.autoGeneratePIDs = value;
    }

    /**
     * Gets the value of the autoIngestValidInstruction property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isAutoIngestValidInstruction() {
        return autoIngestValidInstruction;
    }

    /**
     * Sets the value of the autoIngestValidInstruction property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setAutoIngestValidInstruction(Boolean value) {
        this.autoIngestValidInstruction = value;
    }

    /**
     * Gets the value of the resolverBaseUrl property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getResolverBaseUrl() {
        return resolverBaseUrl;
    }

    /**
     * Sets the value of the resolverBaseUrl property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setResolverBaseUrl(String value) {
        this.resolverBaseUrl = value;
    }

    /**
     * Gets the value of the na property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNa() {
        return na;
    }

    /**
     * Sets the value of the na property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNa(String value) {
        this.na = value;
    }

    /**
     * Gets the value of the contentType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * Sets the value of the contentType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setContentType(String value) {
        this.contentType = value;
    }

    /**
     * Gets the value of the access property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAccess() {
        return access;
    }

    /**
     * Sets the value of the access property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAccess(String value) {
        this.access = value;
    }

    /**
     * Gets the value of the embargo property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEmbargo() {
        return embargo;
    }

    /**
     * Sets the value of the embargo property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEmbargo(String value) {
        this.embargo = value;
    }

    /**
     * Gets the value of the embargoAccess property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEmbargoAccess() {
        return embargoAccess;
    }

    /**
     * Sets the value of the embargoAccess property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEmbargoAccess(String value) {
        this.embargoAccess = value;
    }

    /**
     * Gets the value of the action property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAction() {
        return action;
    }

    /**
     * Sets the value of the action property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAction(String value) {
        this.action = value;
    }

    /**
     * Gets the value of the pidwebserviceEndpoint property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPidwebserviceEndpoint() {
        return pidwebserviceEndpoint;
    }

    /**
     * Sets the value of the pidwebserviceEndpoint property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPidwebserviceEndpoint(String value) {
        this.pidwebserviceEndpoint = value;
    }

    /**
     * Gets the value of the pidwebserviceKey property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPidwebserviceKey() {
        return pidwebserviceKey;
    }

    /**
     * Sets the value of the pidwebserviceKey property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPidwebserviceKey(String value) {
        this.pidwebserviceKey = value;
    }

    /**
     * Gets the value of the version property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public long getVersion() {
        if (version == null) {
            return  0L;
        } else {
            return version;
        }
    }

    /**
     * Sets the value of the version property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setVersion(Long value) {
        this.version = value;
    }

    /**
     * Gets the value of the id property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setId(String value) {
        this.id = value;
    }

    /**
     * Gets the value of the objid property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getObjid() {
        return objid;
    }

    /**
     * Sets the value of the objid property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setObjid(String value) {
        this.objid = value;
    }

    /**
     * Gets the value of the deleteCompletedInstruction property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDeleteCompletedInstruction() {
        return deleteCompletedInstruction;
    }

    /**
     * Sets the value of the deleteCompletedInstruction property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDeleteCompletedInstruction(String value) {
        this.deleteCompletedInstruction = value;
    }

    /**
     * Gets the value of the replaceExistingDerivatives property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getReplaceExistingDerivatives() {
        return replaceExistingDerivatives;
    }

    /**
     * Sets the value of the replaceExistingDerivatives property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setReplaceExistingDerivatives(String value) {
        this.replaceExistingDerivatives = value;
    }

    /**
     * Gets the value of the pdfLevel property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPdfLevel() {
        return pdfLevel;
    }

    /**
     * Sets the value of the pdfLevel property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPdfLevel(String value) {
        this.pdfLevel = value;
    }

}
