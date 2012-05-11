/*
 * Copyright (c) 2010-2011 Social History Services
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

import org.apache.commons.io.FilenameUtils;
import org.objectrepository.exceptions.InstructionException;
import org.objectrepository.instruction.dao.OrIterator;
import org.objectrepository.pid.PidHttpClient;
import org.objectrepository.util.Checksum;
import org.objectrepository.util.Normalizers;
import org.objectrepository.util.PidGenerator;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;

/**
 * InstructionAutocreateService
 *
 * @author Laszlo Marossy <marossyl@ceu.hu>
 * @author Lucien van Wouw <lwo@iisg.nl>
 */
public final class InstructionAutocreateService extends ServiceBaseImp {

    @Autowired
    private InstructionValidateService validateService;

    @Autowired
    public PidHttpClient pidHttpClient;

    /**
     * This method is called for each stagingfile of a collection directory.  In the InstructionAutocreateService implementation a new
     * stagingfileType section is created and added to the InstructionType for each File passed in.  Validation is performed by the
     * ServiceBaseImp.fileTypeForFile(...) method.  Empty <pid> value is created; this will be filled
     * later by either the system or the CP.
     *
     * @param file        the build method calls this method for each stagingfile that is not a directory when traversing the stagingfile
     *                    structure, looking for all files within.
     * @param instruction - that contains the section on fileTypes
     * @see ServiceBaseImp
     */
    @Override
    public void objectFromFile(File file, OrIterator instruction) {

        // calculate checksum and other required items...
        // validate. Report errors where: stagingfile is zero bytes or unreadable or locked
        // Add a pid if it was requested...
        StagingfileType stagingfileType = objectFactory.createStagingfileType();
        //will  provide a PID
        final String location = Normalizers.toRelative(instruction.getInstruction().getFileSet(), file);
        stagingfileType.setLocation(location);
        stagingfileType.setMd5(Checksum.getMD5(file));
        stagingfileType.setLength(file.length());
        addPid(instruction, stagingfileType);
        validateService.isValid(instruction, stagingfileType);
        instruction.add(stagingfileType);
    }

    /**
     * addPid
     * <p/>
     * Will add a lid or pid based on the autoGeneratePIDs field value. If it is:
     * uuid : random pid
     * lid : the pid will be created at ingest time
     * empty\null : no pid will be generated
     * <p/>
     * The logic will only apply to empty pid fields
     *
     * @param iterator        The container of stagingfiles references
     * @param stagingfileType the stagingfile
     */
    public void addPid(OrIterator iterator, StagingfileType stagingfileType) {
        final String autoGeneratePIDs = iterator.getInstruction().getAutoGeneratePIDs();
        if (!Normalizers.isEmpty(autoGeneratePIDs) && Normalizers.isEmpty(stagingfileType.getPid())) {
            if (autoGeneratePIDs.equalsIgnoreCase("uuid")) {
                stagingfileType.setPid(PidGenerator.getPidWithNa(iterator.getInstruction().getNa()));
            } else if (autoGeneratePIDs.equalsIgnoreCase("filename2pid")) {
                stagingfileType.setPid(iterator.getInstruction().getNa()
                        + "/" + FilenameUtils.getBaseName(stagingfileType.getLocation()));
            } else if (autoGeneratePIDs.equalsIgnoreCase("filename2lid")
                    && Normalizers.isEmpty(stagingfileType.getLid())) {
                stagingfileType.setLid(FilenameUtils.getBaseName(stagingfileType.getLocation()));
                stagingfileType.setPid(getPid(iterator.getInstruction(), stagingfileType.getLid()));
            } else if (autoGeneratePIDs.equalsIgnoreCase("lid")) {
                if (Normalizers.isEmpty(stagingfileType.getLid())) {
                    customInfo(stagingfileType, new InstructionException("LidMissing"));
                } else {
                    stagingfileType.setPid(
                            getPid(iterator.getInstruction(), stagingfileType.getLid()));
                }
            }
        }
        if (Normalizers.isEmpty(stagingfileType.getPid())) {
            customInfo(stagingfileType, new InstructionException("PidMissing"));
        }
    }

    /**
     * addPid
     * <p/>
     * Also known as the reverse PID procedure. When we have no PID and a LID and the CP can call the PID webservice.
     * <p/>
     * See if we need to bind a lid with a pid.
     * We will create a PID here if there is none.
     *
     * @param instructionType The container of the stagingfiles
     * @param lid             Local identifier
     * @return pid Peristant identifier from webservice
     */
    private String getPid(InstructionType instructionType, String lid) {
        String cp_url = (instructionType.getPidwebserviceEndpoint() == null) ? null : instructionType.getPidwebserviceEndpoint();
        String cp_key = (instructionType.getPidwebserviceKey() == null) ? null : instructionType.getPidwebserviceKey();
        return pidHttpClient.getPid(cp_url, cp_key, instructionType.getNa(), lid, null);
    }
}
