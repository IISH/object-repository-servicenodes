package org.objectrepository.instruction.dao;

/**
 * OrDao
 *
 * Use to retrieve an files document
 */
public interface OrDao {

    /**
     * Checks for the existence of a document with the given PID
     * @param pid
     * @return
     */
    boolean hasStoredObject(String na, String pid);
}
