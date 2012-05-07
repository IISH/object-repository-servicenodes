package org.objectrepository.instruction;

import org.objectrepository.util.InstructionTypeHelper;
import org.junit.Assert;
import org.junit.Test;

public class ConsoleTest {

    @Test
    public void parseConsoleCommandsFiles() {

        String text = "{instruction:{na:'12345',fileSet:'/mnt/sa/12345/folder_of_cpuser/test-collection/',label:'MyCollection',resolverBaseUrl:'http://hdl.handle.net/',autoGeneratePIDs:'uuid',contentType:'image/jpg',access:'open', stagingfile:{stagingfile:{location:'a stagingfile'}}   ,task:{name:'InstructionAutocreate'} }    }";
        InstructionType instructionType = InstructionTypeHelper.instructionTypeFromJson(text);
        Assert.assertNotNull(instructionType);
        Assert.assertEquals(1, instructionType.getStagingfile().size());
    }

    @Test
    public void parseConsoleCommands() {

        String text = "{instruction:{na:'12345',fileSet:'/mnt/sa/12345/folder_of_cpuser/test-collection/',label:'MyCollection',resolverBaseUrl:'http://hdl.handle.net/',autoGeneratePIDs:'uuid',contentType:'image/jpg',access:'open',task:{name:'InstructionAutocreate'} }    }";
        InstructionType instructionType = InstructionTypeHelper.instructionTypeFromJson(text);
        Assert.assertNotNull(instructionType);
        Assert.assertEquals(0, instructionType.getStagingfile().size());
    }

}
