package org.objectrepository.io;

import org.objectrepository.exceptions.OrFilesException;

/**
 * OrFiles
 */
public interface OrFiles {

    public void setMongo(String hosts[]);

    public void action() throws OrFilesException;
    public void close();
}
