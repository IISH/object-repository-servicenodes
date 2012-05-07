package org.objectrepository.utils;

import org.objectrepository.io.OrFiles;
import org.apache.log4j.Logger;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Some basic reflection.
 */
public class Invocations {

/*
    public static void invoke(OrFiles _class, String methodName) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {

        log.debug("invoke method " + methodName);
        final Method method = _class.getClass().getMethod(methodName);
        invoke(_class, method, null);
    }
*/

    public static void invoke(OrFiles _class, String methodName, Object value, Class c) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {

        log.info("invoke method " + _class.getClass().getCanonicalName() + "." + methodName + "(" + value.toString() + ")");
        final Method method = _class.getClass().getMethod(methodName, c);
        invoke(_class, method, value);
    }

    public static void invoke(OrFiles _class, Method method, Object value) throws IllegalAccessException, InvocationTargetException {

        assert _class != null;
        if (value == null) {
            log.debug("invoke class " + _class.getClass().getCanonicalName());
            method.invoke(_class);
        } else {
            log.debug("invoke method " + _class.getClass().getCanonicalName() + "." + method.getName() + "(" + value + ")");
            method.invoke(_class, value);
        }
    }

    public static String camelCase(String key) {

        final String text = (key.length() == 1)
                ? key.toUpperCase()
                : key.substring(0, 1).toUpperCase() + key.substring(1).toLowerCase();
        return text;
    }

    private static Logger log = Logger.getLogger(Invocations.class);

}
