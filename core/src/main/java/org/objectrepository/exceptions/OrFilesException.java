package org.objectrepository.exceptions;

public class OrFilesException extends Exception {

    public OrFilesException(String message) {
        super(message);
    }

    public OrFilesException(Exception e){
        super(e);
    }
}
