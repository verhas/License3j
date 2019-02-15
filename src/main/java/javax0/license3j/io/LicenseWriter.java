package javax0.license3j.io;

import javax0.license3j.License;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Write the license into the output
 */
public class LicenseWriter implements Closeable {
    private final OutputStream os;

    public LicenseWriter(OutputStream os) {
        this.os = os;
    }

    public LicenseWriter(File file) throws FileNotFoundException {
        this(new FileOutputStream(file));
    }

    public LicenseWriter(String fileName) throws FileNotFoundException {
        this(new File(fileName));
    }

    /**
     * Write the license into the output.
     *
     * @param license the license itself
     * @param format  the desired format of the license, can be {@link IOFormat#STRING},
     *                {@link IOFormat#BASE64} or {@link IOFormat#BINARY}
     * @throws IOException if the output cannot be written
     */
    public void write(License license, IOFormat format) throws IOException {
        switch (format) {
            case BINARY:
                os.write(license.serialized());
                return;
            case BASE64:
                os.write(Base64.getEncoder().encode(license.serialized()));
                return;
            case STRING:
                os.write(license.toString().getBytes(StandardCharsets.UTF_8));
                return;
        }
        throw new IllegalArgumentException("License format " + format + " is unknown");
    }

    /**
     * Write the license to the output in binary format.
     * @param license to write to the file
     * @throws IOException if the output cannot be written
     */
    public void write(License license) throws IOException {
        write(license, IOFormat.BINARY);
    }

    @Override
    public void close() {

    }
}
