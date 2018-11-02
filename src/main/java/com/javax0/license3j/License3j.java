package com.javax0.license3j;

import com.javax0.license3j.licensor.License;
import com.javax0.license3j.utils.CommandLineProcessor;

import java.io.*;

/**
 * A sample command line tool application that
 * demonstrate the use of license3j
 * and also a nice tool to help development.
 * –
 * Simple class containing public static main to encode and decode licenses from
 * the command line.
 *
 * @author Peter Verhas
 */
public class License3j {

    private static final String commandLineString = "java -cp license3j.jar com.javax0.license3j.License3j";

    static PrintStream errorOutput = System.err;
    private static CommandLineProcessor commandLine;

    private static void printUsage(String[] args) {
        errorOutput.print("Usage: " + commandLineString + " decode options\n"
                + " mandatory options are: \n"
                + "--license-file, --keyring-file, [ --output ] [--charset]\n");
        errorOutput
                .println("Usage: "
                        + commandLineString
                        + " command options\n"
                        + "commands available: \n"
                        + "      * encode\n"
                        + "      * decode\n"
                        + "arguments to the different commands type the command w/o args");
        if (args != null) {
            errorOutput.println("Arguments on the command line:");
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
                + System.getProperties().get("user.dir"));
    }


    /**
     * Call this method from the command line.
     * <pre>
     *  java -cp license3j.jar com.javax0.license3j.License3j command options
     *   commands available:
     *       encode
     *       decode
     * to get arguments to the different commands type the command w/o args
     * </pre>
     *
     * @param args command line arguments
     * @throws Exception when something goes wrong
     */
    public static void main(String[] args) throws Exception {
        commandLine = new CommandLineProcessor();
        if (args == null || args.length == 0) {
            printUsage(args);
            return;
        }

        commandLine.process(args);
        if (commandLine.getFiles().size() < 1) {
            printUsage(args);
            return;
        }
        var command = commandLine.getFiles().get(0);

        if ("encode".equals(command)) {
            // encode a license file
            new License3j().encode();
        }
        if ("decode".equals(command)) {
            new License3j().decode();
        }

    }

    private void encode() throws Exception {
        try {
            final var os = new FileOutputStream(commandLine.option("output"));
            os.write((new License().setLicense(
                    new File(commandLine.option("license-file")), "utf-8").loadKey(
                    commandLine.option("keyring-file"),
                    commandLine.option("key")).encodeLicense(commandLine
                    .option("password"))).getBytes("utf-8"));
            os.close();
        } catch (Exception e) {
            errorOutput
                    .print("Usage: "
                            + commandLineString
                            + " encode options\n"
                            + " mandatory options are: \n"
                            + "--license-file, --keyring-file, --key, --password, --output\n");
            throw e;
        }
    }

    private void decode() throws Exception {
        try {
            final var license = new License();
            if (license.loadKeyRing(commandLine.option("keyring-file"), null)
                    .setLicenseEncodedFromFile(
                            commandLine.option("license-file"), "utf-8").isVerified()) {
                try (final var os = getOutput(); final Writer w = getOutputWriter(os);) {
                    w.write("---LICENSE STRING PLAIN TEXT START\n");
                    w.flush();
                    license.dumpLicense(os);
                    w.write("---LICENSE STRING PLAIN TEXT END\n");
                    w.write("Encoding license key id=" + license.getDecodeKeyId() + "L\n");
                    w.write("---KEY RING DIGEST START\n");
                    w.write(license.dumpPublicKeyRingDigest());
                    w.write("---KEY RING DIGEST END\n");
                }
            } else {
                errorOutput.println("The license can not be verified.");
            }
        } catch (Exception e) {
            printUsage(null);
            e.printStackTrace(errorOutput);
            throw e;
        }
    }

    /**
     * Get the output writer from the previously identified output stream.
     * The writer simply wraps the output stream using the default charset or
     * the charset specified on the command line using the option {@code charset}.
     *
     * @param os the output stream
     * @return the created writer object
     * @throws UnsupportedEncodingException if the specified encoding
     *                                      is not supported by the environment
     */
    private Writer getOutputWriter(OutputStream os) throws UnsupportedEncodingException {
        if (commandLine.optionExists("charset")) {
            return new OutputStreamWriter(os, commandLine.option("charset"));
        } else {
            // default charset and not UTF-8 because the aim is not system independence but
            // rather readability when the characters are sent to the system console
            return new OutputStreamWriter(os);
        }
    }

    /**
     * Get the output for the program. If nothing is configured then this is
     * {@code System.out}. If the command line contains the option {@code output}
     * then the file named in the option will be the output.
     *
     * @return the output to be used by the program to write text to
     * @throws FileNotFoundException if the comnmand line specified file is not found
     */
    private OutputStream getOutput() throws FileNotFoundException {
        if (commandLine.optionExists("output")) {
            return new FileOutputStream(commandLine.option("output"));
        } else {
            return System.out;
        }
    }
}
