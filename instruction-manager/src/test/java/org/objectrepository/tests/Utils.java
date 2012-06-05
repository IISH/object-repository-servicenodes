package org.objectrepository.tests;

import org.objectrepository.instruction.StagingfileType;
import org.objectrepository.instruction.TaskType;
import org.objectrepository.instruction.dao.OrIterator;
import org.objectrepository.util.InstructionTypeHelper;

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
            TaskType task = InstructionTypeHelper.firstTask(stagingfileType) ;
            Integer statusCode = (task == null) ? 0 : task.getStatusCode();
            Integer count = statusCodes.containsKey(statusCode) ? statusCodes.get(statusCode) : 0;
            statusCodes.put(statusCode, ++count);
        }
        return statusCodes;
    }
}
