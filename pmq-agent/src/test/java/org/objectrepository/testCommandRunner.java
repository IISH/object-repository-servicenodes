package org.objectrepository;

import org.apache.commons.exec.*;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.BufferedReader;
import java.io.InputStreamReader;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/META-INF/spring/application-context.xml", "/META-INF/spring/dispatcher-servlet.xml"})
public class testCommandRunner {
    private Logger log = Logger.getLogger(getClass());

    @Test
    public void testCommandRunner() {
        //Command to be executed
        CommandLine command = new CommandLine("cmd.exe \\C dir *.java ");

        //Adding its arguments
/*        for (int i = 1; i < commandparts.length; i += 1) {
            String commandpart = commandparts[i];
            log.debug("Adding argument" + commandpart);
            command.addArguments(commandpart);
        }*/

        log.debug("First part " + command.getExecutable());
        String[] arguments = command.getArguments();
        for (String argument : arguments) {
            log.debug("argument: " + argument);
        }

        //Infinite timeout
        ExecuteWatchdog watchDog = new ExecuteWatchdog(ExecuteWatchdog.INFINITE_TIMEOUT);

        //Result Handler for executing the process in a Asynch way
        DefaultExecuteResultHandler resultHandler = new DefaultExecuteResultHandler();

        //Using Std out for the output/error stream - to do it later...
        //http://stackoverflow.com/questions/621596/how-would-you-read-image-data-in-from-a-program-like-image-magick-in-java
        PumpStreamHandler streamHandler = new PumpStreamHandler();

        //This is used to end the process when the JVM exits
        ShutdownHookProcessDestroyer processDestroyer = new ShutdownHookProcessDestroyer();

        //Our main command executor
        DefaultExecutor executor = new DefaultExecutor();

        //Setting the properties
        executor.setStreamHandler(streamHandler);
        executor.setWatchdog(watchDog);
        executor.setProcessDestroyer(processDestroyer);

        //Executing the command
        try {
            executor.execute(command, resultHandler);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //Anything after this will be executed only when the command completes the execution
        try {
            resultHandler.waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        //Checking the exitValue, if 0, then task was successful, if not, error
        int exitValue = resultHandler.getExitValue();
        if (exitValue == 0) {
            log.debug("Success message!");
        } else {
            log.debug("Error message!");
        }
    }

    @Test
    public void testCommandRunner2() {
        try {
            Runtime rt = Runtime.getRuntime();
            Process pr = rt.exec("cmd /c lajos");

            BufferedReader input = new BufferedReader(new InputStreamReader(pr.getErrorStream()));

            String line = null;

            while ((line = input.readLine()) != null) {
                System.out.println(line);
            }

            int exitVal = pr.waitFor();
            System.out.println("Exited with error code " + exitVal);

        } catch (Exception e) {
            System.out.println(e.toString());
            e.printStackTrace();
        }
    }

}