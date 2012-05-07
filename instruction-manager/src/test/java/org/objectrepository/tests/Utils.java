package org.objectrepository.tests;

import org.objectrepository.instruction.StagingfileType;
import org.objectrepository.instruction.dao.OrIterator;

import java.util.HashMap;
import java.util.Map;

/**
 * Helper methods
 */
public class Utils {

    public static Map<Integer, Integer> statusCodes(OrIterator iterator) {
        final Map<Integer, Integer> statusCodes = new HashMap();
        while (iterator.hasNext()) {
            final StagingfileType stagingfileType = iterator.next();
            Integer statusCode = (stagingfileType.getTask() == null) ? 0 : stagingfileType.getTask().getStatusCode();
            Integer count = statusCodes.containsKey(statusCode) ? statusCodes.get(statusCode) : 0;
            statusCodes.put(statusCode, ++count);
        }
        return statusCodes;
    }
}
