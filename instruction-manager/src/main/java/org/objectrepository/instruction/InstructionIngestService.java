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

import org.apache.camel.ProducerTemplate;
import org.apache.log4j.Logger;
import org.objectrepository.instruction.dao.OrIterator;
import org.objectrepository.instruction.dao.ServiceBase;
import org.objectrepository.util.InstructionTypeHelper;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

/**
 * InstructionIngestService
 * <p/>
 * Sets the ingest status for each stagingfile to ingest. This will effectively start the workflow.
 */
public final class InstructionIngestService implements ServiceBase {

    @Autowired
    InstructionValidateService instructionValidate;

    @Autowired
    ObjectFactory objectFactory;

    @Autowired
    ProducerTemplate producer;

    /**
     * build
     * <p/>
     * Gives all known valid stagingfile elements a Start status.
     * This will trigger a workflow for each stagingfile element.
     *
     * @throws IOException
     * @throws NoSuchAlgorithmException
     */
    @Override
    public void build(OrIterator instruction) throws IOException, NoSuchAlgorithmException {
        while (instruction.hasNext()) {
            final StagingfileType stagingfileType = instruction.next();
            if (instructionValidate.isMarkedAsValid(stagingfileType)) {
                TaskType task = objectFactory.createTaskType();
                task.setN(0);
                task.setInfo("Starting InstructionIngest");
                task.setName("Start");
                task.setStatusCode(300);
                task.setTotal(0);
                task.setProcessed(0);
                task.setAttempts(1);
                task.setLimit(3);
                task.setExitValue(Integer.MAX_VALUE);
                task.setIdentifier(UUID.randomUUID().toString());
                InstructionTypeHelper.setSingleTask(stagingfileType, task);
                instruction.add(stagingfileType);
                producer.sendBody(task.getIdentifier());
            } else {
                log.warn("Invalid document " + stagingfileType.getLocation());
                return;
            }
        }

        try {
            producer.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Logger log = Logger.getLogger(InstructionIngestService.class);
}
