/*
 * Copyright (c) 2010-2012 Social History Services
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

import org.objectrepository.util.InstructionTypeHelper;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.lang.reflect.InvocationTargetException;

/**
 * In case something calls the application from console... a main function.
 * json example
 */
public final class Console {
    /**
     * @param args args[0] - validate/create
     *             args[1] - json structure example: {"instruction":{"na":"00000","fileSet":"./instruction-manager/src/test/resources/test-collection/","label":"My alias for a folder","resolverBaseUrl":"http://hdl.handle.net","lidPrefix":"HU:OSA:380:","access":"open","contentType":"image/jpg"}}
     * @throws Exception
     */
    public static void main(String[] args) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {

        InstructionType instructionType = InstructionTypeHelper.instructionTypeFromJson(args[0]);
        if (instructionType == null) {
            System.out.println("Expected InstructionType argument");
            System.exit(1);
        }
        if (instructionType.getFileSet() == null) {
            System.out.println("Expected InstructionType.fileSet");
            System.exit(1);
        }
        if (instructionType.getTask() == null || instructionType.getTask().getName() == null) {
            System.out.println("Expected InstructionType.task with a name set");
            System.exit(1);
        }

        start(instructionType);
    }

    public static void start(InstructionType instructionType) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        ApplicationContext context = new ClassPathXmlApplicationContext("META-INF/spring/or-manager.xml");
        InstructionManager instructionManager = context.getBean(InstructionManager.class);
        instructionType.getTask().setStatusCode(400);
        InstructionManager.class.getMethod(instructionType.getTask().getName(), InstructionType.class).invoke(instructionManager, instructionType);
    }


}
