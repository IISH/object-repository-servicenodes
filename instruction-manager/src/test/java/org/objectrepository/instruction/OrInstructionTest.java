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

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.junit.*;
import org.junit.runner.RunWith;
import org.objectrepository.exceptions.InstructionException;
import org.objectrepository.instruction.dao.InstructionDao;
import org.objectrepository.instruction.dao.InstructionFilesystemImpl;
import org.objectrepository.instruction.dao.OrFsIterator;
import org.objectrepository.instruction.dao.OrIterator;
import org.objectrepository.tests.Utils;
import org.objectrepository.util.InstructionTypeHelper;
import org.objectrepository.util.Normalizers;
import org.objectrepository.util.PidGenerator;
import org.objectrepository.util.TestHelperMethods;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:META-INF/spring/or-manager.xml", "classpath:META-INF/spring/application-context.xml"})

/*
* Test
*
* Run with the datasource set to InstructionMongoDBImpl
*
* Example VM settings for fileSet and na:
* -DfileSet=\object-repository-servicenodes\instruction-manager\src\test\resources\home\12345\folder_of_cpuser\test-collection -Dna=12345
* 
* @author: Lucien van Wouw <lwo@iisg.nl>
*/
public class OrInstructionTest {

    @Autowired
    private InstructionManager instructionManager;

    @Autowired
    private Jaxb2Marshaller marshaller;

    @Autowired
    private InstructionDao dao;

    @Autowired
    ObjectFactory objectFactory;

    @Autowired
    @Qualifier("mongoTemplateSa")
    private MongoTemplate mongoTemplate;

    private Logger log = Logger.getLogger(getClass());

    // path to the test collection of files
    private static String fileSet;
    // path to templates that are copied over to fileSet for tests

    private static String na;

    @BeforeClass
    public static void setUp() throws Exception {

        System.setProperty("environment", "development");
        String tmp = TestHelperMethods.getFileSet();
        fileSet = Normalizers.normalize(tmp);
        na = System.getProperty("na");
        if (na == null) {
            na = "12345";
        }
        String or = System.getProperty("or.properties");
        if (or == null) {
            // See if we can find the property stagingfile...
            File file = new File("or.properties");
            if (!file.exists()) file = new File("./or.properties");
            if (!file.exists()) file = new File("../or.properties");
            if (!file.exists()) file = new File("../../or.properties");
            System.setProperty("or.properties", file.getAbsolutePath());
        }

        new File(fileSet, "instruction.xml").delete();

        removeSneak();
    }

    @AfterClass
    public static void tearDown() {
        removeSneak();
        new File(fileSet, "instruction.xml").delete();
    }

    /**
     * the following files should give errors in this test case: FileZeroSize
     *
     * @throws Exception very general
     */
    @Test
    public void testInstructionAutocreate() throws Exception {

        log.debug("testing the create");
        InstructionType instructionType = objectFactory.createInstructionType();
        instructionType.setFileSet(fileSet);
        instructionType.setNa(na);
        instructionType.setLabel("My alias for a folder");
        instructionType.setResolverBaseUrl("http://hdl.handle.net/");
        instructionType.setAction("add");
        instructionType.setAccess("open");
        instructionType.setContentType("image/jpg");
        instructionType.setAutoGeneratePIDs("uuid");   // Will have pids
        final TaskType taskType = new TaskType();
        taskType.setName("InstructionAutocreate");
        taskType.setStatusCode(500);
        taskType.setInfo("testCreate");
        InstructionTypeHelper.setSingleTask(instructionType, taskType);
        instructionManager.InstructionAutocreate(instructionType);

        // Now test filecount
        final OrIterator iterator = dao.load(instructionType);
        Assert.assertEquals(16, iterator.count());

        // We should have two errors, so two tasks
        Map statusCodes = Utils.statusCodes(iterator);
        Assert.assertEquals("Ought to have one tasks because one stagingfile was zero in length", 1, statusCodes.get(InstructionException.FileZeroSize));
    }

    /**
     * testInstructionValidate
     * <p/>
     * Run a validation procedure with autoGenerated instruction
     *
     * @throws Exception very general
     */
    @Test
    public void testInstructionValidate() throws Exception {

        dao.delete(fileSet);
        InstructionType instructionType = objectFactory.createInstructionType();
        instructionType.setFileSet(fileSet);
        instructionType.setNa(na);
        instructionType.setAction("upsert");
        instructionType.setLabel("My alias for a folder");
        instructionType.setResolverBaseUrl("http://hdl.handle.net/");
        instructionType.setAccess("open");
        instructionType.setContentType("image/jpg");
        instructionType.setAutoGeneratePIDs("uuid");
        final TaskType taskType = new TaskType();
        taskType.setName("InstructionAutocreate");
        taskType.setStatusCode(500);
        taskType.setInfo("InstructionAutocreate");
        InstructionTypeHelper.setSingleTask(instructionType, taskType);

        instructionManager.InstructionAutocreate(instructionType);

        taskType.setName("InstructionValidate");
        instructionManager.InstructionValidate(instructionType);

        final OrIterator iterator = dao.load(instructionType);
        Map statusCodes = Utils.statusCodes(iterator);
        Assert.assertEquals("Ought to have one task because it is zero in length", 1, statusCodes.get(InstructionException.FileZeroSize));
        Assert.assertEquals("15 documents ought to be fine", 15, statusCodes.get(0));
    }

    /**
     * testInstructionLidValidate
     * <p/>
     * Run a validation with autoGenerated instruction.
     * Here we remove the PIDs and introduce LIDs elements.
     * The validation ought to create the PIDs
     *
     * @throws Exception
     */
    @Test
    @Ignore
    public void testInstructionLidValidate() throws Exception {

        dao.delete(fileSet);
        InstructionType instructionType = objectFactory.createInstructionType();
        instructionType.setFileSet(fileSet);
        instructionType.setNa(na);
        instructionType.setAction("upsert");
        instructionType.setLabel("My alias for a folder");
        instructionType.setResolverBaseUrl("http://hdl.handle.net/");
        instructionType.setAccess("open");
        instructionType.setContentType("image/jpg");
        instructionType.setAutoGeneratePIDs("none");
        final TaskType taskType = new TaskType();
        taskType.setName("InstructionAutocreate");
        taskType.setStatusCode(500);
        taskType.setInfo("InstructionValidate");
        InstructionTypeHelper.setSingleTask(instructionType, taskType);

        instructionManager.InstructionAutocreate(instructionType);

        // Re-write the instruction to the fs

        instructionType.setAutoGeneratePIDs("filename2lid");
        taskType.setName("InstructionValidate");
        instructionManager.InstructionValidate(instructionType);
        final OrIterator iterator = dao.load(instructionType);
        Map statusCodes = Utils.statusCodes(iterator);
        Assert.assertEquals("Ought to have one task because it is zero in length", 1, statusCodes.get(InstructionException.FileZeroSize));
        Assert.assertEquals("15 documents ought to be fine", 15, statusCodes.get(0));
    }

    /**
     * the following files should give errors in this test case: FileZeroSize
     *
     * @throws Exception
     */
    @Test
    public void testCreateNoPids() throws Exception {

        log.debug("testing the create");
        dao.delete(fileSet);
        File instr = new File(fileSet, "instruction.xml");
        instr.delete();
        InstructionType instructionType = objectFactory.createInstructionType();
        instructionType.setFileSet(fileSet);
        instructionType.setNa(na);
        instructionType.setLabel("My alias for a folder");
        instructionType.setResolverBaseUrl("http://hdl.handle.net/");
        instructionType.setAction("add");
        instructionType.setAccess("open");
        instructionType.setContentType("image/jpg");
        // instructionType.setAutoGeneratePIDs("uuid");
        final TaskType taskType = new TaskType();
        taskType.setName("InstructionAutocreate");
        taskType.setStatusCode(500);
        taskType.setInfo("testCreate");
        InstructionTypeHelper.setSingleTask(instructionType, taskType);
        instructionManager.InstructionAutocreate(instructionType);

        // Now test filecount
        final OrIterator iterator = dao.load(instructionType);

        Assert.assertEquals(16, iterator.count());
        Map statusCodes = Utils.statusCodes(iterator);
        Assert.assertNull(statusCodes.get(0));
        Assert.assertEquals(1, statusCodes.get(InstructionException.FileZeroSize));
        Assert.assertEquals(15, statusCodes.get(InstructionException.PidMissing));
    }

    /**
     * Run after testCreateNoPids. Should have the same validation result as the autocreate procedure.
     *
     * @throws Exception very general
     */
    @Test
    public void testValidate2() throws Exception {

        dao.delete(fileSet);
        InstructionType instructionType = objectFactory.createInstructionType();
        instructionType.setFileSet(fileSet);
        instructionType.setNa(na);
        instructionType.setAction("add");
        instructionType.setLabel("My alias for a folder");
        instructionType.setResolverBaseUrl("http://hdl.handle.net/");
        instructionType.setAccess("open");
        instructionType.setContentType("image/jpg");
        instructionType.setAutoGeneratePIDs("none");
        final TaskType taskType = new TaskType();
        taskType.setName("InstructionValidate");
        taskType.setStatusCode(500);
        taskType.setInfo("InstructionValidate");
        InstructionTypeHelper.setSingleTask(instructionType, taskType);

        instructionManager.InstructionAutocreate(instructionType);
        instructionManager.InstructionValidate(instructionType);
        final OrIterator iterator = dao.load(instructionType);

        // We should have PID errors
        Map statusCodes = Utils.statusCodes(iterator);
        Assert.assertNull(statusCodes.get(0));
        Assert.assertEquals(1, statusCodes.get(InstructionException.FileZeroSize));
        Assert.assertEquals(15, statusCodes.get(InstructionException.PidMissing));
    }

    /**
     * Rather a long test.
     * - autocreate
     * - add poison in database
     * - add extra undeclared stagingfile
     * - validate
     *
     * @throws Exception very general
     */
    @Test
    public void testInstructionUpload() throws Exception {


        // Create an instruction
        dao.delete(fileSet);
        InstructionType instructionType = objectFactory.createInstructionType();
        instructionType.setFileSet(fileSet);
        instructionType.setNa(na);
        instructionType.setLabel("My alias for a folder");
        instructionType.setResolverBaseUrl("http://hdl.handle.net/");
        instructionType.setAction("add");
        instructionType.setAccess("open");
        instructionType.setContentType("image/jpg");
        instructionType.setAutoGeneratePIDs("lid");   // Will have using lid
        final TaskType taskType = new TaskType();
        taskType.setName("InstructionAutocreate");
        taskType.setStatusCode(500);
        taskType.setInfo("testCreate");
        InstructionTypeHelper.setSingleTask(instructionType, taskType);
        instructionManager.InstructionAutocreate(instructionType);

        OrIterator iterator = dao.load(instructionType); // get documents from database

        // Write the instruction to the fs
        InstructionFilesystemImpl doaFilesystem = new InstructionFilesystemImpl();
        doaFilesystem.setMarshaller(marshaller);
        doaFilesystem.setObjectFactory(objectFactory);
        final OrFsIterator orFsIterator = doaFilesystem.create(iterator.getInstruction());
        orFsIterator.getInstruction().setAutoGeneratePIDs("lid");
        taskType.setName("InstructionUpload");
        InstructionTypeHelper.setSingleTask(orFsIterator.getInstruction(), taskType);
        int i = 0;
        while (iterator.hasNext()) {
            final StagingfileType stagingfileType = iterator.next();
            stagingfileType.setLid("lid" + ++i);   // add LID values in the process
            stagingfileType.getWorkflow().clear();
            orFsIterator.add(stagingfileType);
        }

        // Add some poison
        orFsIterator.getInstruction().getStagingfile().get(1).setLid(null);
        orFsIterator.getInstruction().getStagingfile().get(2).setMd5("00000000000000000000000000000000");
        orFsIterator.getInstruction().getStagingfile().get(3).setMd5(null);
        StagingfileType undeclared = new StagingfileType();
        undeclared.setLid(PidGenerator.getPid());
        undeclared.setLocation("/nowhere/");
        orFsIterator.add(undeclared);

        // Add custom PID
        final String pid = PidGenerator.getPidWithNa(na);
        final String fileWithPid = orFsIterator.getInstruction().getStagingfile().get(6).getLocation();
        orFsIterator.getInstruction().getStagingfile().get(6).setPid(pid);
        doaFilesystem.persist(orFsIterator);

        // Sneak in a new stagingfile to cause a missing stagingfile section
        addSneak();
        // Upload the custom instruction
        instructionManager.InstructionUpload(iterator.getInstruction());
        removeSneak();

        // Results
        iterator = dao.load(iterator.getInstruction());
        Map statusCodes = Utils.statusCodes(iterator);
        Assert.assertEquals(18, iterator.count());
        Assert.assertEquals(1, statusCodes.get(InstructionException.FileDoesNotExist));
        Assert.assertEquals(1, statusCodes.get(InstructionException.MD5Missing));
        Assert.assertEquals(1, statusCodes.get(InstructionException.MD5Mismatch));
        Assert.assertEquals(1, statusCodes.get(InstructionException.FileZeroSize));
        Assert.assertEquals(1, statusCodes.get(InstructionException.MissingFileSection));
        final StagingfileType stagingfileByLocation = iterator.getFileByLocation(fileWithPid);
        Assert.assertNotNull(stagingfileByLocation);
        Assert.assertEquals(pid, stagingfileByLocation.getPid());
    }

    @Test
    public void testInstructionAutoCreateFilename2pid() throws Exception {

        // Create an instruction
        dao.delete(fileSet);
        InstructionType instructionType = objectFactory.createInstructionType();
        instructionType.setFileSet(fileSet);
        instructionType.setNa(na);
        instructionType.setLabel("My alias for a folder");
        instructionType.setResolverBaseUrl("http://hdl.handle.net/");
        instructionType.setAction("add");
        instructionType.setAccess("open");
        instructionType.setContentType("image/jpg");
        instructionType.setAutoGeneratePIDs("filename2pid");   // Will have using lid
        final TaskType taskType = new TaskType();
        taskType.setName("InstructionAutocreate");
        taskType.setStatusCode(500);
        taskType.setInfo("testCreate");
        InstructionTypeHelper.setSingleTask(instructionType, taskType);
        instructionManager.InstructionAutocreate(instructionType);

        final OrIterator iterator = dao.load(instructionType); // get documents from database
        boolean ok = true;
        while (iterator.hasNext()) {
            final StagingfileType stagingfileType = iterator.next();
            final String pid = na + "/" + FilenameUtils.getBaseName(stagingfileType.getLocation());
            ok = ok && pid.equals(stagingfileType.getPid());
        }
        Assert.assertTrue(ok);
    }

    @Test
    @Ignore
    /**
     * To test we depend here on the testInstructionAutoCreateFilename2pid success of building a XML processing
     * instruction.
     */
    public void testInstructionUploadFilename2lid() throws Exception {

        // Create an instruction
        dao.delete(fileSet);
        InstructionType instructionType = objectFactory.createInstructionType();
        instructionType.setFileSet(fileSet);
        instructionType.setNa(na);
        instructionType.setLabel("My alias for a folder");
        instructionType.setResolverBaseUrl("http://hdl.handle.net/");
        instructionType.setAction("add");
        instructionType.setAccess("open");
        instructionType.setContentType("image/jpg");
        instructionType.setAutoGeneratePIDs("filename2lid");   // Will have using lid
        final TaskType taskType = new TaskType();
        taskType.setName("InstructionAutocreate");
        taskType.setStatusCode(500);
        taskType.setInfo("testCreate");
        InstructionTypeHelper.setSingleTask(instructionType, taskType);
        instructionManager.InstructionAutocreate(instructionType);

        final OrIterator iterator = dao.load(instructionType); // get documents from database
        boolean ok = true;
        while (iterator.hasNext() && ok) {
            final StagingfileType stagingfileType = iterator.next();
            final String filename = FilenameUtils.getBaseName(stagingfileType.getLocation());

            // expecting a filename equaling the lid; and a PID with a na/UUID ( a UUID length as a string is 36 )
            ok = ok && filename.equals(stagingfileType.getLid());
            ok = ok && stagingfileType.getPid().length() == 42;
        }
        Assert.assertTrue(ok);
    }

    @Test
    public void testInstructionUploadFilename2pid() throws Exception {

        dao.delete(fileSet);

        InstructionType instructionType = objectFactory.createInstructionType();
        instructionType.setFileSet(fileSet);
        instructionType.setNa(na);
        instructionType.setLabel("My alias for a folder");
        instructionType.setResolverBaseUrl("http://hdl.handle.net/");
        instructionType.setAction("add");
        instructionType.setAccess("open");
        instructionType.setContentType("image/jpg");
        instructionType.setAutoGeneratePIDs("none");
        final TaskType taskType = new TaskType();
        taskType.setName("InstructionAutocreate");
        taskType.setStatusCode(500);
        taskType.setInfo("testCreate");
        InstructionTypeHelper.setSingleTask(instructionType, taskType);
        instructionManager.InstructionAutocreate(instructionType);

        OrIterator iterator = dao.load(instructionType);

        InstructionFilesystemImpl doaFilesystem = new InstructionFilesystemImpl();
        doaFilesystem.setMarshaller(marshaller);
        doaFilesystem.setObjectFactory(objectFactory);
        final OrFsIterator orFsIterator = doaFilesystem.create(iterator.getInstruction());
        taskType.setName("InstructionUpload");
        InstructionTypeHelper.setSingleTask(orFsIterator.getInstruction(), taskType);
        iterator.getInstruction().setAutoGeneratePIDs("filename2pid");
        while (iterator.hasNext()) {
            orFsIterator.add(iterator.next());
        }
        doaFilesystem.persist(orFsIterator);

        instructionManager.InstructionUpload(iterator.getInstruction());
        iterator = dao.load(iterator.getInstruction()); // get documents from database
        boolean ok = true;
        while (iterator.hasNext()) {
            final StagingfileType stagingfileType = iterator.next();
            final String pid = na + "/" + FilenameUtils.getBaseName(stagingfileType.getLocation());
            ok = ok && pid.equals(stagingfileType.getPid());
        }
        Assert.assertTrue(ok);
    }

    /**
     * Integration test to check the lid and pid generation
     */
    @Test
    @Ignore
    public void pidServiceCall() throws Exception {

        InstructionType instructionType = objectFactory.createInstructionType();
        instructionType.setFileSet(fileSet);
        instructionType.setNa(na);
        instructionType.setLabel("My alias for a folder");
        instructionType.setResolverBaseUrl("http://hdl.handle.net/");
        instructionType.setAction("add");
        instructionType.setAccess("open");
        instructionType.setContentType("image/jpg");
        final TaskType taskType = new TaskType();
        taskType.setName("InstructionAutocreate");
        taskType.setStatusCode(500);
        taskType.setInfo("testCreate");
        InstructionTypeHelper.setSingleTask(instructionType, taskType);
        instructionManager.InstructionAutocreate(instructionType);

        OrIterator iterator = dao.load(instructionType); // get documents from database

        // Write the instruction to the fs
        InstructionFilesystemImpl doaFilesystem = new InstructionFilesystemImpl();
        doaFilesystem.setMarshaller(marshaller);
        doaFilesystem.setObjectFactory(objectFactory);
        final OrFsIterator orFsIterator = doaFilesystem.create(iterator.getInstruction());
        orFsIterator.getInstruction().setAutoGeneratePIDs("lid");
        InstructionTypeHelper.firstTask(orFsIterator.getInstruction()).setName("InstructionUpload");
        int i = 0;
        while (iterator.hasNext()) {
            final StagingfileType stagingfileType = iterator.next();
            stagingfileType.setLid("lid" + ++i);   // add LID values in the process
            stagingfileType.getWorkflow().clear();
            orFsIterator.add(stagingfileType);
        }

        doaFilesystem.persist(orFsIterator);
        instructionType.setAutoGeneratePIDs("lid");
        instructionManager.InstructionUpload(instructionType);
        instructionManager.InstructionIngest(instructionType);

        // Results
        iterator = dao.load(instructionType);
        int count = 0;
        while (iterator.hasNext()) {
            final StagingfileType stagingfileType = iterator.next();
            String pid = stagingfileType.getPid();
            if (pid != null) {
                count++;
                Assert.assertTrue(pid.startsWith(na));
            }
        }
        Assert.assertTrue(count > 5);
    }

    /**
     * add, update, upsert and delete actions need testing.
     * Not sure what is the best way to proceed.
     */
    @Test
    public void actions
    () throws Exception {

        dao.delete(fileSet);

        log.debug("testing the create");
        InstructionType instructionType = objectFactory.createInstructionType();
        instructionType.setFileSet(fileSet);
        instructionType.setNa(na);
        instructionType.setLabel("My alias for a folder");
        instructionType.setResolverBaseUrl("http://hdl.handle.net/");
        instructionType.setAction("add");
        instructionType.setAccess("open");
        instructionType.setContentType("image/jpg");
        instructionType.setAutoGeneratePIDs("uuid");   // Will have pids
        final TaskType taskType = new TaskType();
        taskType.setName("InstructionAutocreate");
        taskType.setStatusCode(500);
        taskType.setInfo("testCreate");
        InstructionTypeHelper.setSingleTask(instructionType, taskType);
        instructionManager.InstructionAutocreate(instructionType);

        OrIterator iterator = dao.load(instructionType);

        InstructionFilesystemImpl doaFilesystem = new InstructionFilesystemImpl();
        doaFilesystem.setMarshaller(marshaller);
        doaFilesystem.setObjectFactory(objectFactory);
        final OrFsIterator orFsIterator = doaFilesystem.create(iterator.getInstruction());
        taskType.setName("InstructionUpload");
        InstructionTypeHelper.setSingleTask(orFsIterator.getInstruction(), taskType);

        while (iterator.hasNext()) {
            final StagingfileType stagingfileType = iterator.next();
            stagingfileType.setAction("update");
            stagingfileType.setLocation(null);
            stagingfileType.getWorkflow().clear();
            orFsIterator.add(stagingfileType);
        }

        doaFilesystem.persist(orFsIterator);
        instructionManager.InstructionUpload(iterator.getInstruction());
        iterator = dao.load(iterator.getInstruction());
        Map statusCodes = Utils.statusCodes(iterator);
        Assert.assertEquals(16, statusCodes.get(InstructionException.ExpectFileUpdate));
    }

    @Test
    public void PidLidMultiplication() throws Exception {

        // Create an instruction
        dao.delete(fileSet);
        InstructionType instructionType = objectFactory.createInstructionType();
        instructionType.setFileSet(fileSet);
        instructionType.setNa(na);
        instructionType.setLabel("My alias for a folder");
        instructionType.setResolverBaseUrl("http://hdl.handle.net/");
        instructionType.setAction("add");
        instructionType.setAccess("open");
        instructionType.setContentType("image/jpg");
        instructionType.setAutoGeneratePIDs("uuid");   // Will have using lid
        final TaskType taskType = new TaskType();
        taskType.setName("InstructionAutocreate");
        taskType.setStatusCode(500);
        taskType.setInfo("testCreate");
        InstructionTypeHelper.setSingleTask(instructionType, taskType);
        instructionManager.InstructionAutocreate(instructionType);

        OrIterator iterator = dao.load(instructionType); // get documents from database

        // Write the instruction to the fs
        InstructionFilesystemImpl doaFilesystem = new InstructionFilesystemImpl();
        doaFilesystem.setMarshaller(marshaller);
        doaFilesystem.setObjectFactory(objectFactory);
        final OrFsIterator orFsIterator = doaFilesystem.create(iterator.getInstruction());
        orFsIterator.getInstruction().setAutoGeneratePIDs("lid");
        taskType.setName("InstructionUpload");
        InstructionTypeHelper.setSingleTask(orFsIterator.getInstruction(), taskType);
        int i = 0;
        while (iterator.hasNext()) {
            final StagingfileType stagingfileType = iterator.next();
            stagingfileType.setLid("lid" + ++i);   // add LID values in the process
            stagingfileType.getWorkflow().clear();
            orFsIterator.add(stagingfileType);
        }

        // Add some poison
        orFsIterator.getInstruction().getStagingfile().get(0).setLid("lidIdentical");
        orFsIterator.getInstruction().getStagingfile().get(2).setLid("lidIdentical");
        orFsIterator.getInstruction().getStagingfile().get(4).setPid(na + "/pidIdentical");
        orFsIterator.getInstruction().getStagingfile().get(6).setPid(na + "/pidIdentical");
        doaFilesystem.persist(orFsIterator);

        InstructionTypeHelper.firstTask(iterator.getInstruction()).setName("InstructionUpload");
        instructionManager.InstructionUpload(iterator.getInstruction());

        // Results
        iterator = dao.load(iterator.getInstruction());
        Map statusCodes = Utils.statusCodes(iterator);
        Assert.assertEquals(1, statusCodes.get(InstructionException.PidMultiplication));
        Assert.assertEquals(1, statusCodes.get(InstructionException.LidMultiplication));

    }

    private static void addSneak() throws IOException {
        File file = new File(fileSet, "manuscripts.txt");
        FileOutputStream fos = new FileOutputStream(file);
        for (int i = 0; i < 1000; i++) {
            fos.write("All work and no play makes Jack a dull boy. ".getBytes());
            fos.write("All play and no work makes Jack a mere toy. ".getBytes());
        }
        fos.close();
    }

    private static void removeSneak() {
        File file = new File(fileSet, "manuscripts.txt");
        file.delete();
    }
}