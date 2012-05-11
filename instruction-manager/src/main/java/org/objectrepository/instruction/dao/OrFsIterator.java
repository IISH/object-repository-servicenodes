package org.objectrepository.instruction.dao;

import org.objectrepository.instruction.InstructionType;
import org.objectrepository.instruction.StagingfileType;

public class OrFsIterator implements OrIterator {

    private InstructionType instruction;
    private int index = 0;

    public OrFsIterator(InstructionType instruction) {
        this.instruction = instruction;
    }

    @Override
    public boolean hasNext() {
        return (index < instruction.getStagingfile().size());
    }

    @Override
    public StagingfileType next() {
        return instruction.getStagingfile().get(index++);
    }

    @Override
    public void remove() {
        instruction.getStagingfile().remove(index);
    }

    public void add(StagingfileType stagingfile) {
        instruction.getStagingfile().add(stagingfile);
    }

    /**
     * method loops through all fileTypes of an instruction stagingfile and returns the one where its location is matching the
     * passed-in File's.
     *
     * @param absolute File to be searched for
     * @return FileType of matching section; or null if no match found
     */
    @Override
    public StagingfileType getFileByLocation(String absolute) {
        for (StagingfileType stagingfileType : instruction.getStagingfile()) {
            String loc = stagingfileType.getLocation();
            if (loc.equals(absolute))
                return stagingfileType;
        }
        return null;
    }

    public InstructionType getInstruction() {
        return instruction;
    }

    @Override
    public int count() {
        return instruction.getStagingfile().size();
    }

    @Override
    public int countByKey(String key, String value) {
        return -1;
    }
}
