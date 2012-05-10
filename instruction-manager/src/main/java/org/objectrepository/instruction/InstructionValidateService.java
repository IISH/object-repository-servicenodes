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

import org.objectrepository.exceptions.InstructionException;
import org.objectrepository.instruction.dao.OrDaoImp;
import org.objectrepository.instruction.dao.OrIterator;
import org.objectrepository.util.Checksum;
import org.objectrepository.util.Normalizers;
import org.objectrepository.util.InstructionTypeHelper;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

/**
 * Report warnings and errors when updating an instruction:
 * All files in the filesystem MUST be in the or instruction
 * All files in the or instruction MUST be on the filesystem.
 * Files may not have a length of zero bytes
 * Checksum (if provided) must match
 * Must have a PID
 * Global level must have access status set
 * Global level must have a contentType
 * The PID of an object must not exists with other objects elsewhere in the instruction.
 *
 * @author Laszlo Marossy <marossyl@ceu.hu>
 */
public final class InstructionValidateService extends ServiceBaseImp {

    @Autowired
    OrDaoImp orDaoImp;

    @Autowired
    private InstructionAutocreateService autocreateService;

    @Override
    public void build(OrIterator instruction) {
        super.build(instruction);
        findMissingFiles(instruction);
    }

    /**
     * loops through all stagingfileType's within and creates sections with errors where files are missing based on the
     * locations
     *
     * @param instruction to be searched
     */
    public void findMissingFiles(OrIterator instruction) {

        while (instruction.hasNext()) {
            StagingfileType stagingfileType = instruction.next();
            if (missing(instruction.getInstruction(), stagingfileType)) {
                instruction.add(stagingfileType);
            }
        }
    }

    /**
     * missing
     * <p/>
     * Checks if a stagingfile is available on fs.
     * When action=delete, we can skip the stagingfile check
     * When action=add, we always expect a location
     * When action=upsert, it can be an add: location must be available. If not, it is an update, where we expect a
     * repository object
     *
     * @param stagingfileType
     * @return
     */
    public boolean missing(InstructionType instruction, StagingfileType stagingfileType) {

        final String action = (String) InstructionTypeHelper.getValue(instruction, stagingfileType, "action");
        if (action.equalsIgnoreCase("delete")) return false;

        try {
            if (stagingfileType.getLocation() == null) {
                if (action.equalsIgnoreCase("add")) {
                    throw new InstructionException("ExpectFileAdd");
                }
                if (action.equalsIgnoreCase("upsert") && !orDaoImp.hasFiles(stagingfileType.getPid())) {
                    throw new InstructionException("ExpectFileUpsert");
                }
                return false;
            }
        } catch (InstructionException se) {
            customInfo(stagingfileType, se);
            return true;
        }

        try {
            File file = Normalizers.toAbsolute(instruction.getFileSet(), stagingfileType.getLocation());
            if (!file.exists())
                throw new InstructionException("FileDoesNotExist");
        } catch (InstructionException se) {
            customInfo(stagingfileType, se);
            return true;
        }
        return false;
    }

    /**
     * @param file        the build method calls this method for each stagingfile that is not a directory when traversing the stagingfile
     *                    structure, looking for all files within.
     *                    <p/>
     *                    the ValidateService implementation should see if a section is present for each stagingfile found.  This
     *                    implementation checks for the following error conditions:
     *                    <li>no stagingfile section for stagingfile found (error code MissingFileSection)
     *                    <li>no global AND local contentType found (error code MissingMimeType)
     *                    <li>errors produced by fileTypeForFile() in ServiceBaseImp
     *                    <li>if lidPrefix is not provided in the header and lid value provided is not in proper format (contains at least
     *                    two ":" characters at different places) (errorCode LidNotInProperFormat)
     *                    <li>if both the lidPrefix and lid values are missing (errorCode MissingLidPrefix)
     *                    <li>if no NA value is provided in the header, AND no pid values provided for the stagingfile (errorCode MissingNAValue)
     * @param instruction - that contains the section on fileTypes
     * @throws org.objectrepository.exceptions.InstructionException
     *
     * @throws IOException
     * @throws NoSuchAlgorithmException
     * @see ServiceBaseImp
     */
    @Override
    public void objectFromFile(File file, OrIterator instruction) {

        StagingfileType stagingfileType = instruction.getFileByLocation(Normalizers.toRelative(instruction.getInstruction().getFileSet(), file));
        if (wrongFileContent(file, instruction, stagingfileType)) {
            instruction.add(stagingfileType);
        }

        if (stagingfileType.getPid() == null) {
            autocreateService.addPid(instruction, stagingfileType);
            instruction.add(stagingfileType);
        }
    }

    /**
     * Various validations as to the stagingfile itself and it's content
     * Action will determine:
     * add = the stagingfile with the PID must not exist.
     * update = the stagingfile with the PID
     *
     * @param file
     * @param instruction
     * @param stagingfileType
     * @return true if invalid
     * @throws NoSuchAlgorithmException
     * @throws IOException
     */
    private boolean wrongFileContent(File file, OrIterator instruction, StagingfileType stagingfileType) {

        if (stagingfileType == null) {
            setMissingSection(instruction.getInstruction().getFileSet(), file);
            return true;
        }

        try {
            final InstructionType global = instruction.getInstruction();
            final String action = (String) InstructionTypeHelper.getValue(global, stagingfileType, "action");
            if (action == null) {
                throw new InstructionException("ActionMissing");
            }

            if (action.equalsIgnoreCase("delete")) return false;
            final boolean hasFiles = orDaoImp.hasFiles(stagingfileType.getPid());
            if (action.equalsIgnoreCase("add") && hasFiles) {
                throw new InstructionException("ExpectUpdate");
            }
            if (action.equalsIgnoreCase("update") && !hasFiles) {
                throw new InstructionException("ExpectAdd");
            }

            // Add like operations, need more metadata
            if (action.equalsIgnoreCase("add") || action.equalsIgnoreCase("upsert") && !hasFiles) {

                //check for access
                if (Normalizers.isEmpty(global.getAccess()) && Normalizers.isEmpty(stagingfileType.getAccess())) {
                    throw new InstructionException("AccessMissing");
                }

                //check for contentType
                if (Normalizers.isEmpty(global.getContentType()) && Normalizers.isEmpty(stagingfileType.getContentType())) {
                    throw new InstructionException("MissingMimeType");
                }

                fileTypeForFile(stagingfileType, file);
            }
        } catch (InstructionException se) {
            customInfo(stagingfileType, se);
            return true;
        }
        return false;
    }

    public StagingfileType setMissingSection(String fileSet, File file) {
        StagingfileType stagingfileType = new StagingfileType();
        stagingfileType.setLocation(Normalizers.toRelative(fileSet, file.getPath()));
        stagingfileType.setTask(InstructionException.getTaskByStatus("MissingFileSection"));
        return stagingfileType;
    }

    private void fileTypeForFile(StagingfileType stagingfileType, File file) throws InstructionException {

        if (!file.canRead())
            throw new InstructionException("CantReadFile");

        if (file.length() == 0)
            throw new InstructionException("FileZeroSize");

        final String md5_from_instruction = stagingfileType.getMd5();
        if (md5_from_instruction == null)
            throw new InstructionException("MD5Missing");

        final String md5_from_file = Checksum.getMD5(file);
        final boolean identical = Checksum.compare(md5_from_file, md5_from_instruction);
        if (!identical) {
            throw new InstructionException("MD5Mismatch");
        }
    }

    public boolean isValid(OrIterator instruction, StagingfileType stagingfileType) {
        final String fileSet = instruction.getInstruction().getFileSet();
        return !(missing(instruction.getInstruction(), stagingfileType) || wrongFileContent(Normalizers.toAbsolute(fileSet, stagingfileType.getLocation()), instruction, stagingfileType));
    }

    /**
     * isMarkedAsValid
     * <p/>
     * Verified if this stagingfile element has not got a record of a validation error
     *
     * @param stagingfileType
     * @return
     */
    public boolean isMarkedAsValid(StagingfileType stagingfileType) {
        TaskType task = stagingfileType.getTask();
        if (task == null) return true;
        return (!task.getName().equalsIgnoreCase("InstructionValidated") &&
                (task.getStatusCode() < 700 || task.getStatusCode() > 799));
    }
}
