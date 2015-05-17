/**
 * A sample command line tool application that
 * demonstrate the use of license3j
 * and also a nice tool to help development.
 *
 * @author Peter Verhas <peter@verhas.com>
 */
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.Writer;

import com.verhas.licensor.License;
import com.verhas.utils.CommandLineProcessor;

/**
 * Simple class containing public static main to encode and decode licenses from
 * the command line.
 * 
 * @author Peter Verhas
 */
public class License3j {

	private static final String commandLineString = "java -cp license3j.jar License3j";
	private static CommandLineProcessor commandLine;
	private static PrintStream errorOutput = System.err;

	private void encode() throws Exception {
		try {
			OutputStream os = new FileOutputStream(commandLine.option("output"));
			os.write((new License().setLicense(
					new File(commandLine.option("license-file"))).loadKey(
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
			final License license;
			if ((license = new License())
					.loadKeyRing(commandLine.option("keyring-file"), null)
					.setLicenseEncodedFromFile(
							commandLine.option("license-file")).isVerified()) {
				OutputStream os = System.out;
				if (commandLine.optionExists("output")) {
					os = new FileOutputStream(commandLine.option("output"));
				}
				Writer w = null;
				if (commandLine.optionExists("charset")) {
					w = new OutputStreamWriter(os,
							commandLine.option("charset"));
				} else {
					w = new OutputStreamWriter(os);
				}
				w.write("---LICENSE STRING PLAIN TEXT START\n");
				w.flush();
				license.dumpLicense(os);
				os.flush();
				w.write("---LICENSE STRING PLAIN TEXT END\n");
				w.write("Encoding license key id=" + license.getDecodeKeyId()
						+ "L\n");
				w.write("---KEY RING DIGEST START\n");
				w.write(license.dumpPublicKeyRingDigest());
				w.write("---KEY RING DIGEST END\n");
				w.close();
				os.close();
			} else {
				errorOutput.println("The license can not be verified.");
			}
		} catch (Exception e) {
			printUsage(null);
			e.printStackTrace(errorOutput);
			throw e;
		}
	}

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
			int i = 1;
			for (String arg : args) {
				errorOutput.println(i + ". " + arg);
				i++;
			}
			errorOutput.println("Command line options:");
			i = 1;
			for (String opt : commandLine.getOptions().keySet()) {
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
	 * <p>
	 * 
	 * <pre>
	 *  java -cp license3j.jar License3j command options
	 *   commands available:
	 *       encode
	 *       decode
	 * to get arguments to the different commands type the command w/o args
	 * </pre>
	 * </p>
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
		String command = commandLine.getFiles().get(0);

		if ("encode".equals(command)) {
			// encode a license file
			new License3j().encode();
		}
		if ("decode".equals(command)) {
			new License3j().decode();
		}

	}
}
