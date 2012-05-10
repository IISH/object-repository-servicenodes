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

/**
 * Store and retrieve the or instruction.
 */
public interface InstructionDao {

    /**
     * Returns an empty instruction.
     * The instruction may contain default global settings.
     * If an instruction already exists in the supplied location, an error will be thrown.
     *
     * @return
     */
    OrIterator create(InstructionType instruction) throws Exception;

    /**
     * Retrieves the or instruction from the datasource.
     *
     * @param location
     * @return A list of records
     * @throws Exception
     */
    OrIterator load(String na, String location) throws Exception;

    OrIterator load(InstructionType instruction) throws Exception;

    /**
     * Inserts the entire instruction into the datasource.
     */
    void persist(OrIterator instructions) throws Exception;

    public void syncProfile(InstructionType instruction) throws Exception;

    void delete(String fileSet);

    /**
     * removetasks
     * <p/>
     * Removes the transient stagingfile element that was inserted to indicate the
     * absence of a file.
     *
     * @param instruction
     */
    void removetasks(InstructionType instruction);
}