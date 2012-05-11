package org.objectrepository.exceptions;

import org.objectrepository.instruction.TaskType;

import java.util.HashMap;


/**
 * InstructionException
 * <p/>
 * Meant to be a basic Or exception for Or-type errors.
 * The error codes would correspond to specific errors
 * for which the errors are collected in an internal map.
 * <p/>
 * author: lma
 * author: Lucien van Wouw <lwo@iisg.nl>
 */
public final class InstructionException extends Exception {

    private TaskType instructionInfo;
    private static HashMap<String, TaskType> errorMessages;

    public static int FileAlreadyExists = 701;
    public static int FileDoesNotExist = 702;
    public static int FileZeroSize = 703;
    public static int CantReadFile = 704;
    public static int MissingMimeType = 705;
    public static int MissingFileSection = 706;
    public static int MissingFileForSection = 707;
    public static int MissingNAValue = 708;
    public static int MissingLidPrefix = 709;
    public static int LidNotInProperFormat = 710;
    public static int MD5Mismatch = 711;
    public static int MD5Missing = 712;
    public static int AccessMissing = 713;
    public static int ActionMissing = 714;
    public static int LidMissing = 715;
    public static int PidMissing = 716;
    public static int ExpectUpdate = 717;
    public static int ExpectAdd = 718;
    public static int ExpectFileAdd = 719;
    public static int ExpectFileUpsert = 720;
    public static int LidMultiplication = 721;
    public static int PidMultiplication = 722;


    static {
        errorMessages = new HashMap();
        errorMessages.put("FileAlreadyExists", addTask("InstructionValidate", FileAlreadyExists, "File already exists! Either delete it first or use the Update functionality."));
        errorMessages.put("FileDoesNotExist", addTask("InstructionValidate", FileDoesNotExist, "The File does not exist."));
        errorMessages.put("FileZeroSize", addTask("InstructionValidate", FileZeroSize, "File size is zero; please re-upload the stagingfile."));
        errorMessages.put("CantReadFile", addTask("InstructionValidate", CantReadFile, "File can not be read; please re-upload the stagingfile."));
        errorMessages.put("MissingMimeType", addTask("InstructionValidate", MissingMimeType, "There are files in the instruction that do not have a contentType qualifier. Set these... or set a global contentType status that can be applied to all files."));
        errorMessages.put("MissingFileSection", addTask("InstructionValidate", MissingFileSection, "A stagingfile is found; but no stagingfile section was found."));
        errorMessages.put("MissingFileForSection", addTask("InstructionValidate", MissingFileForSection, "A stagingfile is found; but no stagingfile section was found."));
        errorMessages.put("MissingNAValue", addTask("InstructionValidate", MissingNAValue, "No pid provided and no NA attribute present for the system to create random value."));
        errorMessages.put("MissingLidPrefix", addTask("InstructionValidate", MissingLidPrefix, "No pid  provided and lidPrefix attribute present for the system to create random value."));
        errorMessages.put("LidNotInProperFormat", addTask("InstructionValidate", LidNotInProperFormat, "No <pid>, no lidPrefix attribute & <lid> not in proper format () for "));
        errorMessages.put("MD5Mismatch", addTask("InstructionValidate", MD5Mismatch, "The CP-provided a checksum that does not match the one generated by the system for stagingfile."));
        errorMessages.put("MD5Missing", addTask("InstructionValidate", MD5Missing, "There is no md5 checksum."));
        errorMessages.put("AccessMissing", addTask("InstructionValidate", AccessMissing, "There are files in the instruction that do not have an access qualifier. Set these... or set a global access status that can be applied to all files."));
        errorMessages.put("ActionMissing", addTask("InstructionValidate", ActionMissing, "There are files in the instruction that do not have an action qualifier. Set these... or set a global action status that can be applied to all files."));
        errorMessages.put("LidMissing", addTask("InstructionValidate", LidMissing, "The stagingfile has no persistent identifier or local substitute"));
        errorMessages.put("PidMissing", addTask("InstructionValidate", PidMissing, "The stagingfile has no PID. It should be added."));
        errorMessages.put("ExpectUpdate", addTask("InstructionValidate", ExpectUpdate, "The action=add, but an object with the PID already exists in the repository. Use action='upsert' or 'update' in stead."));
        errorMessages.put("ExpectAdd", addTask("InstructionValidate", ExpectAdd, "The action=update, but there is no such object in the repository. Use action='add' or 'upsert' in stead."));
        errorMessages.put("ExpectFileAdd", addTask("InstructionValidate", ExpectFileAdd, "The action=add, but there is no location for the stagingfile."));
        errorMessages.put("ExpectFileUpsert", addTask("InstructionValidate", ExpectFileUpsert, "The action=upsert, but there is no location for the stagingfile or object in the repository."));
        errorMessages.put("LidMultiplication", addTask("InstructionValidate", LidMultiplication, "The lid value is used elsewhere in the instruction."));
        errorMessages.put("PidMultiplication", addTask("InstructionValidate", PidMultiplication, "The pid value is used elsewhere in the instruction."));
    }

    public static TaskType getTaskByStatus(String fault) {
        return clone(errorMessages.get(fault));
    }

    private static TaskType addTask(String name, int statusCode, String info) {
        TaskType task = new TaskType();
        task.setName(name);
        task.setStatusCode(statusCode);
        task.setInfo(info);
        task.setTotal(0);
        task.setProcessed(0);
        task.setAttempts(0);
        task.setLimit(0);
        task.setExitValue(Integer.MAX_VALUE);
        return task;
    }

    private static TaskType clone(TaskType info) {
        return addTask(info.getName(), info.getStatusCode(), info.getInfo());
    }

    /**
     * Use if no additional info needs to be passed in
     *
     * @param errorCode - has to correspond to one of the integer
     *                  values in errorMessages hashmap
     */
    public InstructionException(String errorCode) {
        super(errorMessages.get(errorCode).getInfo());
        instructionInfo = errorMessages.get(errorCode);
    }

    /**
     * Use if additional info needs to be passed in
     *
     * @param errorCode
     * @param customInfo Some event data... by the system rather than the pre-defined status codes
     */
    public InstructionException(String errorCode, String customInfo) {
        super(customInfo);
        instructionInfo = clone(errorMessages.get(errorCode));
    }

    /**
     * Returns the error message stored for the given param
     *
     * @param name - the Or error code key
     * @return - the Or error message
     */
    public static String lookupOrError(String name) {
        return errorMessages.get(name).getInfo();
    }

    /**
     * returns a OrInfo ready to be inserted into a InstructionType or StagingfileType
     *
     * @return - the OrInfo
     */
    public TaskType getInstructionInfo() {
        return clone(instructionInfo);
    }
}
