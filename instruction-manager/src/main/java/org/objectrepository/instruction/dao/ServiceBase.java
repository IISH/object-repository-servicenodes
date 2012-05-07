package org.objectrepository.instruction.dao;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

public interface ServiceBase {
    public void build(OrIterator iterator) throws IOException, NoSuchAlgorithmException;
}
