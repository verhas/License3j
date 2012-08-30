package com.verhas.licensor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.util.Arrays;
import junit.framework.TestCase;
import org.bouncycastle.openpgp.PGPException;

/**
 *
 * @author Peter Verhas <peter@verhas.com>
 */
public class TestEncoding extends TestCase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        new File("license.txt").delete();
    }
    private byte[] digest = new byte[]{
        (byte) 0x86, (byte) 0x1C, (byte) 0x2C, (byte) 0x13, (byte) 0x30,
        (byte) 0x96, (byte) 0xD0, (byte) 0xBA, (byte) 0x90, (byte) 0x23,
        (byte) 0x30, (byte) 0x69, (byte) 0x12, (byte) 0x3F, (byte) 0xAF,
        (byte) 0x8F, (byte) 0xFD, (byte) 0xAE, (byte) 0x43, (byte) 0xBD,
        (byte) 0xE1, (byte) 0x82, (byte) 0x73, (byte) 0x33, (byte) 0x0E,
        (byte) 0x1A, (byte) 0x7A, (byte) 0x95, (byte) 0xF2, (byte) 0xE7,
        (byte) 0xCF, (byte) 0x78
    };

    public void testCalculatePublicKeyRingDigest() throws IOException,
            PGPException,
            Exception {
        License l = new License();
        l.loadKey(License.class.getClassLoader().
                getResourceAsStream("secring.gpg"),
                "Peter Verhas (licensor test key) <peter@verhas.com>");
        l.loadKeyRingFromResource("pubring.gpg", null);
        byte[] c = l.calculatePublicKeyRingDigest();
        assert Arrays.equals(c, digest);
    }

    public void testMain() throws IOException, PGPException,
            NoSuchAlgorithmException, NoSuchProviderException,
            SignatureException, Exception {

        License license = new License();
        license.setLicense("a=b");
        license.loadKey(License.class.getClassLoader().
                getResourceAsStream("secring.gpg"),
                "Peter Verhas (licensor test key) <peter@verhas.com>");
        String encoded = license.encodeLicense("alma");
        OutputStream os = new FileOutputStream("license.txt");
        os.write(encoded.getBytes("utf-8"));
        os.close();

        License lic = new License();
        lic.loadKeyRingFromResource("pubring.gpg", digest);
        lic.setLicenseEncodedFromFile("license.txt");
        String z = lic.getLicenseString();
        assert -3623885160523215197L == lic.getDecodeKeyId();
        assert lic.getFeature("a").equals("b");
    }
}
