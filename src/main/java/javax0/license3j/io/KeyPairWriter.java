package javax0.license3j.io;

import javax0.license3j.crypto.LicenseKeyPair;

import java.io.*;
import java.util.Base64;

/**
 * Class to write the key pair into two files. Key are read individually but they are written in pairs right
 * after they are generated. This class can be used by applications that generate keys and it is used by the
 * repl application.
 *
 * Create an instance of the class using one of the constructors specifying the output files and then invoke the
 * {@link #write(LicenseKeyPair, IOFormat)} method to save the keys into the files.
 */
public class KeyPairWriter implements Closeable {
    private final OutputStream osPrivate;
    private final OutputStream osPublic;

    public KeyPairWriter(OutputStream osPrivate, OutputStream osPublic) {
        this.osPrivate = osPrivate;
        this.osPublic = osPublic;
    }

    public KeyPairWriter(File priv, File publ) throws FileNotFoundException {
        this(new FileOutputStream(priv), new FileOutputStream(publ));
    }

    public KeyPairWriter(String priv, String publ) throws FileNotFoundException {
        this(new File(priv), new File(publ));
    }

    /**
     * Write the key pair into the output files.
     *
     * @param pair the key pair to write.
     * @param format that can be {@link IOFormat#BINARY} or {@link IOFormat#BASE64}. Using {@link IOFormat#STRING}
     *               will throw exception as keys, as opposed to licenses, cannot be saved in string format.
     * @throws IOException when the underlying media cannot be written
     */
    public void write(LicenseKeyPair pair, IOFormat format) throws IOException {
        switch (format) {
            case BINARY:
                osPrivate.write(pair.getPrivate());
                osPublic.write(pair.getPublic());
                return;
            case BASE64:
                osPrivate.write(Base64.getEncoder().encode(pair.getPrivate()));
                osPublic.write(Base64.getEncoder().encode(pair.getPublic()));
                return;
        }
        throw new IllegalArgumentException("Key format " + format + " is unknown.");
    }

    @Override
    public void close() throws IOException {
        if (osPrivate != null) {
            osPrivate.close();
        }
        if (osPublic != null) {
            osPublic.close();
        }
    }
}
