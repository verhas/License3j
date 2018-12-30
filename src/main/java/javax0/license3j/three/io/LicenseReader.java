package javax0.license3j.three.io;

import javax0.license3j.three.License;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Reads a license from some input.
 */
public class LicenseReader implements Closeable {

    private final InputStream is;

    public LicenseReader(InputStream is) {
        this.is = is;
    }

    public LicenseReader(File file) throws FileNotFoundException {
        this(new FileInputStream(file));
    }

    public LicenseReader(String fileName) throws FileNotFoundException {
        this(new File(fileName));
    }


    public License read() throws IOException {
        return read(IOFormat.BINARY);
    }

    public License read(IOFormat format) throws IOException {
        switch (format) {
            case BINARY:
                return License.Create.from(ByteArrayReader.readInput(is));
            case BASE64:
                return License.Create.from(Base64.getDecoder().decode(ByteArrayReader.readInput(is)));
            case STRING:
                return License.Create.from(new String(ByteArrayReader.readInput(is), StandardCharsets.UTF_8));
        }
        throw new IllegalArgumentException("License format "+format+" is unknown.");
    }

    @Override
    public void close() throws IOException {
        if (is != null) {
            is.close();
        }
    }
}
