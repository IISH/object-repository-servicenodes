package org.objectrepository.instruction.dao;

import org.objectrepository.instruction.InstructionType;
import org.objectrepository.instruction.StagingfileType;

import java.util.Iterator;

public interface OrIterator extends Iterator<StagingfileType> {

    public void add(StagingfileType stagingfile);
    public StagingfileType getFileByLocation(String absolute);
    public InstructionType getInstruction();
    int count();
}
