
/**
 * A sample command line tool application that
 * demonstrate the use of license3j
 * and also a nice tool to help development.
 *
 * @author Peter Verhas <peter@verhas.com>
 */
import com.verhas.licensor.License;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Vector;

/**
 * Simple class containing public static main to encode and decode licenses
 * from the command line.
 *
 * @author Peter Verhas
 */
public class License3j {

    private static final String commandLineString = "java -cp license3j.jar License3j";
    private HashMap<String, String> argMap = new HashMap<String, String>();
    private Vector<String> argVec = new Vector<String>();

    /**
     * Fill the {@code argMap} map and the {@code argVec} Vector
     * with the command line arguments.
     * When an argument starts with -- then this is a
     * <pre>
     * --arg=value
     * </pre>
     * type argument. When the argument starts with - (but not --)
     * then it is a
     * <pre>
     * -arg value
     * </pre>
     * type argument.
     * <p>
     * When an argument is neither then this is a 'file' parameter
     * and then it gets into the {@code argVec} vector.
     * <p>
     * The order of the parameters and the way they are specified
     * ( -- or - ) is not preserved. The order of 'file' arguments
     * is reserved in the array.
     *
     * @param args the arguments passed to the main function
     */
    private void fillArg(String[] args) {
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.startsWith("--")) {
                arg = arg.substring(2);
                int indexOfEqualSign = arg.indexOf("=");
                if (indexOfEqualSign == -1) {
                    argMap.put(arg, null);
                } else {
                    String val = arg.substring(indexOfEqualSign + 1);
                    arg = arg.substring(0, indexOfEqualSign);
                    argMap.put(arg, val);
                }
            } else if (arg.startsWith("-")) {
                arg = arg.substring(1);
                if (i + 1 < args.length) {
                    argMap.put(arg, args[i + 1]);
                } else {
                    argMap.put(arg, null);
                }
                i++;
            } else {
                argVec.add(arg);
            }
        }
    }

    private void encode() throws Exception {
        try {
            License license = new License();
            license.setLicense(new File(argMap.get("license-file")));
            license.loadKey(argMap.get("keyring-file"), argMap.get("key"));
            String encoded = license.encodeLicense(argMap.get("password"));
            OutputStream os = new FileOutputStream(argMap.get("output"));
            os.write(encoded.getBytes("utf-8"));
            os.close();
        } catch (Exception e) {
            System.err.print(
                    "Usage: " + commandLineString + " encode options\n" +
                    " mandatory options are: \n" +
                    "--license-file, --keyring-file, --key, --password, --output\n");
            throw e;
        }
    }

    private void decode() throws Exception {
        try {
            License license = new License();
            license.loadKeyRing(argMap.get("keyring-file"), null);
            license.setLicenseEncodedFromFile(argMap.get("license-file"));
            if (license.isVerified()) {
                OutputStream os = System.out;
                if (argMap.containsKey("output")) {
                    os = new FileOutputStream(argMap.get("output"));
                }
                Writer w = null;
                if (argMap.containsKey("--charset")) {
                    w = new OutputStreamWriter(os, argMap.get("--charset"));
                } else {
                    w = new OutputStreamWriter(os);
                }
                w.write("---LICENSE STRING PLAIN TEXT START\n");
                w.flush();
                license.dumpLicense(os);
                os.flush();
                w.write("---LICENSE STRING PLAIN TEXT END\n");
                w.write("Encoding license key id=" + license.getDecodeKeyId() + "L\n");
                w.write("---KEY RING DIGEST START\n");
                w.write(license.dumpPublicKeyRingDigest());
                w.write("---KEY RING DIGEST END\n");
                w.close();
                os.close();
            } else {
                System.err.println("The license can not be verified.");
            }
        } catch (Exception e) {
            System.err.print(
                    "Usage: " + commandLineString + " decode options\n" +
                    " mandatory options are: \n" +
                    "--license-file, --keyring-file, [ --output ] [--charset]\n");
            throw e;
        }
    }

    private static void printUsage() {
        System.err.print(
                "Usage: " + commandLineString + " command options\n" +
                "commands available: \n" +
                "      * encode\n" +
                "      * decode\n" +
                "arguments to the different commands type the command w/o args");
    }

    /**
     * Call this method from the command line.
     * <p>
     * <pre>
     *  java -cp license3j.jar License3j command options
     *   commands available:
     *       encode
     *       decode
     * to get arguments to the different commands type the command w/o args
     * </pre>
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {

        if (args.length == 0) {
            printUsage();
            return;
        }
        License3j license3j = new License3j();
        license3j.fillArg(args);
        String command = license3j.argVec.get(0);
        if ("encode".equals(command)) {
            // encode a license file
            license3j.encode();
        }
        if ("decode".equals(command)) {
            license3j.decode();
        }

    }
}
