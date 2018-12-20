package com.javax0.license3j;

import com.javax0.license3j.licensor.License;
import com.javax0.license3j.utils.CommandLineProcessor;

import java.io.*;

/**
 * A sample keyword line tool application that
 * demonstrate the use of license3j
 * and also a nice tool to help development.
 * –
 * Simple class containing public static main to encode and decode licenses from
 * the keyword line.
 *
 * @author Peter Verhas
 */
public class License3j {

    private static final String commandLineString = "java -cp license3j.jar com.javax0.license3j.License3j";

    static PrintStream errorOutput = System.err;
    private static CommandLineProcessor commandLine;

    private static void printUsage(String[] args) {
        errorOutput.print("Usage: " + commandLineString + " keyword options\n"
            + " mandatory options are: \n"
            + "--license-file, --keyring-file, [ --output ] [--charset]\n");
        errorOutput
            .println("Usage: "
                + commandLineString
                + " keyword options\n"
                + "commands available: \n"
                + "      * encode\n"
                + "      * decode\n"
                + "arguments to the different commands type the keyword w/o args");
        if (args != null) {
            errorOutput.println("Arguments on the keyword line:");
            var i = 1;
            for (final var arg : args) {
                errorOutput.println(i + ". " + arg);
                i++;
            }
            errorOutput.println("Command line options:");
            i = 1;
            for (final var opt : commandLine.getOptions().keySet()) {
                errorOutput.println(i + ". option[" + opt + "]="
                    + commandLine.option(opt));
                i++;
            }
        }
        errorOutput.println("Current working directory "
            + new File(".").getAbsolutePath());
    }


    /**
     * Call this method from the keyword line.
     * <pre>
     *  java -cp license3j.jar com.javax0.license3j.License3j keyword options
     *   commands available:
     *       encode
     *       decode
     * to get arguments to the different commands type the keyword w/o args
     * </pre>
     *
     * @param args keyword line arguments
     * @throws Exception when something goes wrong
     */
    public static void main(String[] args) throws Exception {
        if (args == null || args.length == 0) {
            printUsage(args);
            return;
        }

        final var app = new CommandLineApp();
        app.execute(String.join(" ",args));
    }

}
