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
package org.objectrepository.instruction;

import org.apache.log4j.Logger;
import org.objectrepository.exceptions.InstructionException;
import org.objectrepository.instruction.dao.InstructionDao;
import org.objectrepository.instruction.dao.InstructionFilesystemImpl;
import org.objectrepository.instruction.dao.OrIterator;
import org.springframework.beans.factory.annotation.Autowired;

/*
* ToDo: Put javadoc here
*
* Created by IntelliJ IDEA.
* Date: 12-2-11
* Time: 0:51
*
* @author: Lucien van Wouw <lwo@iisg.nl>
* @author Laszlo Marossy <marossyl@ceu.hu>
*/

public final class InstructionManager {

    @Autowired
    private InstructionDao dao;

    @Autowired
    private ObjectFactory objectFactory;

    private static Logger log = Logger.getLogger(InstructionManager.class);

    private InstructionAutocreateService instructionAutocreate;
    private InstructionValidateService instructionValidate;
    private InstructionUploadService instructionUpload;
    private InstructionIngestService instructionInstructionIngest;

    /**
     * InstructionManager
     * <p/>
     * Creates an or XML Instruction stagingfile in the directory given in the fileSet param - if such stagingfile is not
     * already present - using the passed-in parameters as global values for the stagingfile, and examining all files found
     * by traversing the folder structure that is included in the given folder.
     * <p/>
     * The method will instructionAutocreate an initial version of the XML Instruction File that will still need to be updated by the
     * CP with the LID value before the stagingfile can be processed further.
     *
     * @param instructionType The instruction global element
     * @throws Exception
     */
    public void InstructionAutocreate(InstructionType instructionType) throws Exception {

        //creating the instruction XML object with params passed in from an interface/API
        OrIterator instruction = dao.create(instructionType);

        //now build the stagingfile content
        try {
            instructionAutocreate.build(instruction);
            dao.persist(instruction);
        } catch (InstructionException se) {
            log.error(se.getMessage());
            throw se;
        }
    }

    public void InstructionUpload(InstructionType instructionType) throws Exception {
        instructionUpload.bulkImport(instructionType);
    }

    /**
     * InstructionValidate
     *
     * @throws Exception
     */
    public void InstructionValidate(InstructionType instructionType) throws Exception {
        dao.removetasks(instructionType);
        OrIterator instruction = dao.load(instructionType);
        instructionValidate.build(instruction);
        if (dao instanceof InstructionFilesystemImpl)  // Test case with the FileDao
            dao.persist(instruction);
    }

    public void InstructionIngest(InstructionType instructionType) {
        try {
            OrIterator instruction = dao.load(instructionType);
            instructionInstructionIngest.build(instruction);
        } catch (Exception e) {
            log.warn(e);
        }
    }

    public void setInstructionAutocreate(InstructionAutocreateService instructionAutocreate) {
        this.instructionAutocreate = instructionAutocreate;
    }

    public InstructionAutocreateService getInstructionAutocreate() {
        return instructionAutocreate;
    }

    public void setInstructionValidate(InstructionValidateService instructionValidate) {
        this.instructionValidate = instructionValidate;
    }

    public void setInstructionUpload(InstructionUploadService instructionUpload) {
        this.instructionUpload = instructionUpload;
    }

    public void setInstructionInstructionIngest(InstructionIngestService instructionInstructionIngest) {
        this.instructionInstructionIngest = instructionInstructionIngest;
    }

    public InstructionIngestService getInstructionInstructionIngest() {
        return instructionInstructionIngest;
    }

    public InstructionValidateService getInstructionValidate() {
        return instructionValidate;
    }
}
