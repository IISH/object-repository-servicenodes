package org.objectrepository;

import org.apache.commons.exec.CommandLine;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.objectrepository.services.Commandizer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Arrays;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/META-INF/spring/application-context.xml", "/META-INF/spring/dispatcher-servlet.xml"})
public class commandizerTest {

    @BeforeClass
    public static void setUp() throws Exception {

        String or = System.getProperty("or.properties");
        if (or == null) {
            System.setProperty("or.properties", ".././or.properties");
        }
    }

    @Test
    public void commandizer01Test() {
        String message = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                "<instruction xmlns=\"http://objectrepository.org/instruction/1.0/\" access=\"restricted\" contentType=\"image/jpg\" na=\"12345\"\n" +
                "    lidPrefix=\"HU:OSA:380:\" resolverBaseUrl=\"http://hdl.handle.net/\" label=\"My alias for a folder\"\n" +
                "    fileSet=\"./instruction-manager/src/test/resources/test-collection/\">\n" +
                "    <stagingfile>\n" +
                "        <contentType>image/jpeg</contentType>\n" +
                "        <pid>10891/HU:OSA:380:12345</pid>\n" +
                "        <lid>1234503</lid>\n" +
                "        <location>File on the $staging \\ area...</location>\n" +
                "        <md5>dc2227bf78e0ee19f8929530cdedc5e7</md5>\n" +
                "        <access>closed</access>\n" +
                "        <dummy1></dummy1>\n" +
                "        <dummy2/>\n" +
                "    </stagingfile>\n" +
                "    <task>\n" +
                "        <name>InstructionAutoCreate</name>\n" +
                "        <statusCode>30</statusCode>\n" +
                "    </task>\n" +
                "</instruction>";

        CommandLine cmd = Commandizer.makeCommand("/opt/bash.sh", message);
        System.out.println("And the command is:");
        System.out.println(cmd);
        List<String> arguments = Arrays.asList(cmd.getArguments());
        Assert.assertEquals(arguments.size(), 28);
        /*Assert.assertTrue(arguments.contains("-request.instruction.stagingfile.md5"));
        Assert.assertTrue(arguments.contains("-request.instruction.stagingfile.pid"));
        Assert.assertTrue(arguments.contains("-request.instruction.task.status"));
        Assert.assertTrue(arguments.contains("-request.instruction.access"));
        Assert.assertTrue(arguments.contains("-request.instruction.stagingfile.access"));*/
        Assert.assertTrue(arguments.contains("-name"));
        Assert.assertTrue(arguments.contains("InstructionAutoCreate"));
        Assert.assertTrue(arguments.contains("-md5"));
        Assert.assertTrue(arguments.contains("dc2227bf78e0ee19f8929530cdedc5e7"));
        Assert.assertTrue(arguments.contains("-pid"));
        Assert.assertTrue(arguments.contains("-statusCode"));
        Assert.assertTrue(arguments.contains("-access"));
        Assert.assertTrue(arguments.contains("closed"));
        Assert.assertTrue(arguments.contains("30"));
        Assert.assertFalse(arguments.contains("open"));
        Assert.assertFalse(arguments.contains(""));
        Assert.assertFalse(arguments.contains(null));
        Assert.assertFalse(arguments.contains("-dummy1"));
        Assert.assertFalse(arguments.contains("-dummy2"));
    }

    @Test
    public void commandizer02test() {
        String message = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<junk>\n" +
                "<pid>12345/the pid here</pid>\n" +
                "<stagingfile>\n" +
                "<pid>12345/the pid here</pid>\n" +
                "<contentType>image/jgp</contentType>\n" +
                "<location>stagingfile=\\staging-area-storage\\home\\cpuser\\fileset\\apples3.jpeg</location>\n" +
                "<cp></cp>\n" +
                "</stagingfile>\n" +
                "</junk>";

        CommandLine cmd = Commandizer.makeCommand("/opt/bash.sh", message);
        System.out.println("And the command is:");
        System.out.println(cmd);
        Assert.assertNotNull(cmd);

    }
}
