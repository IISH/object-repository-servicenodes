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
import org.objectrepository.instruction.dao.OrIterator;
import org.objectrepository.instruction.dao.ServiceBase;
import org.objectrepository.util.Counting;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;


/*
* Holds a list of or instructions
*
* @author: Lucien van Wouw <lwo@iisg.nl>
* @author Laszlo Marossy <marossyl@ceu.hu>
*/
public abstract class ServiceBaseImp implements ServiceBase {

    private static Logger log = Logger.getLogger(ServiceBaseImp.class);
    @Autowired
    public ObjectFactory objectFactory;
    @Value("#{clientProperties['server.or.proxy']}")
    protected String proxy;

    /**
     * this method loops through all folders within the folder Folder.  Each (non-directory) stagingfile is then passed to the
     * objectFromFile method implementation of the child classes.
     *
     * @param instruction
     */
    public void build(OrIterator instruction) {
        getFolders(new File(instruction.getInstruction().getFileSet()), instruction);
    }

    /**
     * @param folder
     * @param instruction
     */
    private void getFolders(File folder, OrIterator instruction) {
        for (File file : folder.listFiles()) {
            if (Counting.skip(file.getName())) {  // Ignore the self and hidden folders.
                continue;
            }
            if (file.isDirectory()) {
                log.debug("folder " + folder.getName());
                this.getFolders(file, instruction);
            } else {
                log.info("stagingfile " + file.getAbsolutePath());
                objectFromFile(file, instruction);
            }
        }
    }

    /**
     * this method should be overwritten by either the ValidateService or the Insert Service.
     *
     * @param file        the build method calls this method for each stagingfile that is not a directory when traversing the stagingfile
     *                    structure, looking for all files within.
     *                    <p/>
     *                    the InstructionAutocreateService implementation should create a new fileType section for each stagingfile found
     *                    the ValidateService implementation should see if a section is present for each stagingfile found.
     * @param instruction - that contains the section on fileTypes
     * @throws IOException
     * @throws NoSuchAlgorithmException
     */
    public abstract void objectFromFile(File file, OrIterator instruction);

    /**
     * adds a SORInfoType and sets the workflowStep of the passed-in StagingfileType
     *
     * @param ft
     * @param se
     */
    public void customInfo(StagingfileType ft, InstructionException se) {
        final TaskType task = se.getInstructionInfo(); // a clone
        customInfo(ft, task);
    }
    public void customInfo(StagingfileType ft, TaskType taskType) {
        final String info = (ft.getTask() == null) ? taskType.getInfo() : ft.getTask().getInfo() + "; " + taskType.getInfo();
        taskType.setInfo(info);
        ft.setTask(taskType);
    }
}

