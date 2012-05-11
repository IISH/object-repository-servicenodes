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
package org.objectrepository.instruction.dao;

import org.objectrepository.instruction.InstructionType;
import org.objectrepository.instruction.ObjectFactory;
import org.objectrepository.util.InstructionTypeHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import javax.xml.bind.JAXBElement;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileOutputStream;

/**
 * Datasource is the stagingfile system.
 *
 * @author Lucien van Wouw <lwo@iisg.nl>
 */

public class InstructionFilesystemImpl implements InstructionDao {

    @Autowired
    Jaxb2Marshaller marshaller;

    @Autowired
    private ObjectFactory objectFactory;

    @Override
    public OrFsIterator create(InstructionType instruction) throws Exception {
        return new OrFsIterator(instruction);
    }

    @Override
    public OrIterator load(InstructionType instruction) throws Exception {
        return load(instruction.getNa(), instruction.getFileSet());
    }

    @Override
    public OrFsIterator load(String na, String rootFolder) throws Exception {

        File location = getInstructionFile(rootFolder);
        InstructionType instructionType = InstructionTypeHelper.instructionTypeFromFile(location);
        return new OrFsIterator(instructionType);
    }

    @Override
    public void persist(OrIterator instruction) throws Exception {

        FileOutputStream fileOutputStream = new FileOutputStream(getInstructionFile(instruction.getInstruction().getFileSet()));
        Result result = new StreamResult(fileOutputStream);
        JAXBElement<InstructionType> InstructionTypeJAXBElement = objectFactory.createInstruction(instruction.getInstruction());
        marshaller.marshal(InstructionTypeJAXBElement, result);
        fileOutputStream.close();
    }

    /*@Override
    public void syncProfile(InstructionType instruction) throws Exception {
        throw new Exception("Method not implemented.");
    }*/

    @Override
    public void delete(String fileSet) {
        File location = getInstructionFile(fileSet);
        location.delete();
    }

    @Override
    public void removetasks(InstructionType instructionType) {
        // Method not implemented.
    }

    public File getInstructionFile(String rootFolder) {
        return new File(rootFolder, "instruction.xml");
    }

    public void setMarshaller(Jaxb2Marshaller marshaller) {
        this.marshaller = marshaller;
    }

    public void setObjectFactory(ObjectFactory objectFactory) {
        this.objectFactory = objectFactory;
    }
}
