package com.javax0.license3j.licensor;

import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPUtil;
import org.junit.jupiter.api.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Peter Verhas <peter@verhas.com>
 */

public class TestLicenseClass {

    private static final String digestReference = "byte [] digest = new byte[] {\n"
            + "(byte)0x69, \n"
            + "(byte)0xBB, (byte)0x8E, (byte)0x6F, (byte)0x99, (byte)0xF2, (byte)0x15, (byte)0x37, (byte)0x7C, \n"
            + "(byte)0x39, (byte)0x54, (byte)0x6F, (byte)0x1F, (byte)0x5D, (byte)0xBA, (byte)0xC9, (byte)0x7E, \n"
            + "(byte)0x35, (byte)0x7C, (byte)0xBF, (byte)0x7F, (byte)0xE6, (byte)0xA2, (byte)0x17, (byte)0x9B, \n"
            + "(byte)0x7E, (byte)0x3E, (byte)0x92, (byte)0x7F, (byte)0xB6, (byte)0x0C, (byte)0x6A, (byte)0xDA, \n"
            + "(byte)0x8D, (byte)0x46, (byte)0xBE, (byte)0xED, (byte)0x96, (byte)0x87, (byte)0x24, (byte)0x06, \n"
            + "(byte)0x98, (byte)0x7C, (byte)0x6B, (byte)0x80, (byte)0xB2, (byte)0x91, (byte)0x19, (byte)0x0D, \n"
            + "(byte)0x22, (byte)0x66, (byte)0x89, (byte)0x9E, (byte)0xF0, (byte)0xB1, (byte)0xDA, (byte)0xE9, \n"
            + "(byte)0x74, (byte)0x70, (byte)0x2F, (byte)0x80, (byte)0x6E, (byte)0x6F, (byte)0x67, \n"
            + "};\n";
    final private String testTmpFilesDirectory = "target/";
    final private String licenseOutputTextFileName = testTmpFilesDirectory
            + "license.txt";
    final private String licenseInputFile = testTmpFilesDirectory
            + "license-plain.txt";
    final private String dumpFile1 = testTmpFilesDirectory + "dumpfile1.txt";
    final private String dumpFile2 = testTmpFilesDirectory + "dumpfile2.txt";
    private final byte[] digest = new byte[]{(byte) 0x69, (byte) 0xBB,
            (byte) 0x8E, (byte) 0x6F, (byte) 0x99, (byte) 0xF2, (byte) 0x15,
            (byte) 0x37, (byte) 0x7C, (byte) 0x39, (byte) 0x54, (byte) 0x6F,
            (byte) 0x1F, (byte) 0x5D, (byte) 0xBA, (byte) 0xC9, (byte) 0x7E,
            (byte) 0x35, (byte) 0x7C, (byte) 0xBF, (byte) 0x7F, (byte) 0xE6,
            (byte) 0xA2, (byte) 0x17, (byte) 0x9B, (byte) 0x7E, (byte) 0x3E,
            (byte) 0x92, (byte) 0x7F, (byte) 0xB6, (byte) 0x0C, (byte) 0x6A,
            (byte) 0xDA, (byte) 0x8D, (byte) 0x46, (byte) 0xBE, (byte) 0xED,
            (byte) 0x96, (byte) 0x87, (byte) 0x24, (byte) 0x06, (byte) 0x98,
            (byte) 0x7C, (byte) 0x6B, (byte) 0x80, (byte) 0xB2, (byte) 0x91,
            (byte) 0x19, (byte) 0x0D, (byte) 0x22, (byte) 0x66, (byte) 0x89,
            (byte) 0x9E, (byte) 0xF0, (byte) 0xB1, (byte) 0xDA, (byte) 0xE9,
            (byte) 0x74, (byte) 0x70, (byte) 0x2F, (byte) 0x80, (byte) 0x6E,
            (byte) 0x6F, (byte) 0x67};

    private static String fromResource(String resourceName) {
        return License.class.getClassLoader().getResource(resourceName)
                .getFile();
    }

    private void createLicenseInputFile() throws IOException {
        try (final var os = new FileOutputStream(licenseInputFile)) {
            os.write("feature=value\n".getBytes());
        }
    }

    @BeforeEach
    public void setUp() throws IOException {
        createLicenseInputFile();
    }

    @AfterEach
    public void tearDown() throws IOException {
        new File(licenseOutputTextFileName).delete();
        new File(licenseInputFile).delete();
        new File(dumpFile1).delete();
        new File(dumpFile2).delete();
    }

    @Test
    @DisplayName("can calculate the public key ring digest properly")
    public void calculatesPublicKeyRingDigest() throws IOException,
            PGPException {
        final var license = new License();
        license.loadKey(fromResource("secring.gpg"),
                "Peter Verhas (licensor test key) <peter@verhas.com>");
        license.loadKeyRingFromResource("pubring.gpg", null);
        final byte[] calculatedDigest = license.calculatePublicKeyRingDigest();
        assert Arrays.equals(calculatedDigest, digest);
    }

    @Test
    @Deprecated
    @DisplayName("can load the license from a file")
    public void licenseLoadedFromFileContainsFeatureAndValue()
            throws IOException {
        final var license = new License();
        license.setLicense(new File(licenseInputFile));
        assertEquals("value", license.getFeature("feature"));
    }

    @Test
    @Deprecated
    @DisplayName("can dump a license to a file and then read it back")
    public void dumpsLicenseToFilesAndReadsBack() throws IOException {
        final var license = new License();
        final var sb = new StringBuilder();
        final var featureSetSize = 10;
        for (int i = 1; i < featureSetSize; i++) {
            sb.append("key").append(i).append("=value").append(i).append(
                    "\n");
        }
        license.setLicense(sb.toString());
        license.dumpLicense(new File(dumpFile1));
        license.dumpLicense(dumpFile2);

        License lic = new License();
        lic.setLicense(new File(dumpFile1));
        for (int i = 1; i < featureSetSize; i++) {
            assertEquals("value" + i, lic.getFeature("key" + i));
        }
        lic = new License();
        lic.setHashAlgorithm(PGPUtil.SHA512);
        lic.setLicense(new File(dumpFile2));
        for (int i = 1; i < featureSetSize; i++) {
            assertEquals("value" + i, lic.getFeature("key" + i));
        }
    }

    @Test
    @DisplayName("when the digest does not match it throws exception")
    public void wrongDigestCausesException() throws IOException {
        final var lic = new License();
        final byte[] myDigest = new byte[digest.length];
        System.arraycopy(digest, 0, myDigest, 0, digest.length);
        myDigest[myDigest.length - 1] = (byte) (myDigest[myDigest.length - 1] ^ 8);
        Assertions.assertThrows(IllegalArgumentException.class, () -> lic.loadKeyRingFromResource("pubring.gpg", myDigest));
    }

    @Test
    @DisplayName("can load a license from an encoded string")
    public void loadsEncodedLicenseString() throws IOException, PGPException,
            NoSuchAlgorithmException, NoSuchProviderException,
            SignatureException {
        final var lic = new License();
        lic.loadKeyRingFromResource("pubring.gpg", digest);
        lic.setLicenseEncoded("-----BEGIN PGP MESSAGE-----\n"
                + "Version: BCPG v1.46\n"
                + "\n"
                + "owJ4nJvAy8zAJXh2a/zSHhO/xYynA5PEczKTU/OKU90yc1L9EnNTdT3T8/KLUlMC\n"
                + "HH9wKuvqKkClFdKA8lzKwYklCsGpBQoGhgqGxlamZlam5grOrsEhCkYGhkZcibZJ\n"
                + "XB3VLAyCXAzWrEwgE0xkAlJLUosUwlKLMhKLFTQgpuUXKZSkFpcoZKdWairYFIBU\n"
                + "OJSBVegl5+faMXBxCsCc+N+TYa7EU76VglmzPzo96VMMV32xTejNRE2GeRofVA4u\n"
                + "ChTbYWL6YrrSob8nNqtzXAMAt5JOhw==\n" + "=xVq4\n"
                + "-----END PGP MESSAGE-----\n");
        assertTrue(lic.isVerified());
        String z = lic.getLicenseString().replaceAll("\r\n", "\n");
        z = z.substring(z.length() - 4);
        assertEquals("a=b\n", z);
        assertEquals((Long) (-3623885160523215197L), lic.getDecodeKeyId());
        assertEquals("b", lic.getFeature("a"));
    }

    @Test
    @DisplayName("can load a license from an encoded resource")
    public void loadsEncodedLicenseFile() throws IOException, PGPException,
            NoSuchAlgorithmException, NoSuchProviderException,
            SignatureException {
        final var lic = new License();
        lic.loadKeyRingFromResource("pubring.gpg", digest);
        lic.setLicenseEncodedFromResource("license-encoded.txt", "utf-8");
        assertTrue(lic.isVerified());
        String z = lic.getLicenseString().replaceAll("\r\n", "\n");
        z = z.substring(z.length() - 4);
        assertEquals("a=b\n", z);
        assertEquals((Long) (-3623885160523215197L), lic.getDecodeKeyId());
        assertEquals("b", lic.getFeature("a"));
    }

    @Test
    @DisplayName("can encode license")
    public void encodesLicense1() throws IOException, PGPException {
        final var license = new License();
        license.setLicense("");
        license.setFeature("a", "b");
        license.loadKey(new File(fromResource("secring.gpg")),
                "Peter Verhas (licensor test key) <peter@verhas.com>");
        final var encoded = license.encodeLicense("alma");
        try (final var os = new FileOutputStream(licenseOutputTextFileName)) {
            os.write(encoded.getBytes(StandardCharsets.UTF_8));
        }
    }

    @Test
    @DisplayName("can set a feature in a license and then retrieve it")
    public void canSetFeatureAndRetrieveOnFreshLicenseObject() {
        final var license = new License();
        license.setFeature("a", "b");
        assertEquals("b", license.getFeature("a"));
    }

    @Test
    @DisplayName("gets null for any feature in an empty (no feature) license")
    public void getsNullForNonExistingFeatureOnFreshLicenseObject() {
        final var license = new License();
        assertNull(license.getFeature("xxx"));
    }

    @Test
    @DisplayName("returns null if the feature does not exist in a license")
    public void getsNullForNonExistingFeature() {
        final var license = new License();
        license.setFeature("a", "b");
        assertNull(license.getFeature("xxx"));
    }

    @Test
    @DisplayName("empty license dump is zero length")
    public void dumpsNothingFromFreshLicenseObject() throws IOException {
        final var license = new License();
        var baos = new ByteArrayOutputStream();
        license.dumpLicense(baos);
        assertEquals(0, baos.size());
    }

    @Test
    @DisplayName("loading a key to a license with user name that is not in the gpg ring throws exception")
    public void loadingKeyWithBadNameThrowsException() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            final var license = new License();
            license.setLicense("");
            license.setFeature("a", "b");
            license.loadKey(new File(fromResource("secring.gpg")),
                    "Name that does not exist <peter@verhas.com>");
            final var encoded = license.encodeLicense("alma");
            try (final var os = new FileOutputStream(licenseOutputTextFileName)) {
                os.write(encoded.getBytes(StandardCharsets.UTF_8));
            }
        });
    }

    @Test
    @DisplayName("loading a key to a license with wrong password throws exception")
    public void loadingKeyWithBadPasswordThrowsException() {
        Assertions.assertThrows(PGPException.class, () -> {
            final var license = new License();
            license.setLicense("");
            license.setFeature("a", "b");
            license.loadKey(new File(fromResource("secring.gpg")),
                    "Peter Verhas (licensor test key) <peter@verhas.com>");
            final var encoded = license.encodeLicense("bad password");
            try (final var os = new FileOutputStream(licenseOutputTextFileName)) {
                os.write(encoded.getBytes(StandardCharsets.UTF_8));
            }
        });
    }

    @Test
    @DisplayName("license loaded from a file has the expected digest")
    public void licenseLoadedFromFileHasAppropriateDigest()
            throws IOException {
        final var license = new License();
        license.loadKeyRing(new File(fromResource("pubring.gpg")), digest);
        final var s = license.dumpPublicKeyRingDigest();
        assertEquals(digestReference, s);
    }

    @Test
    @DisplayName("license loaded from a file given by the name has the expected digest")
    public void licenseLoadedFromFileNameHasAppropriateDigest()
            throws IOException {
        final var license = new License();
        license.loadKeyRing(fromResource("pubring.gpg"), digest);
        final var s = license.dumpPublicKeyRingDigest();
        assertEquals(digestReference, s);
    }

    @Test
    @Deprecated
    public void testMain()
            throws IOException, PGPException {
        final var license = new License();
        license.setLicense("");
        license.setFeature("a", "b");
        license.loadKey(fromResource("secring.gpg"),
                "Peter Verhas (licensor test key) <peter@verhas.com>");
        final var encoded = license.encodeLicense("alma");
        try (final var os = new FileOutputStream(licenseOutputTextFileName)) {
            os.write(encoded.getBytes(StandardCharsets.UTF_8));
        }

        final var lic = new License();
        lic.loadKeyRingFromResource("pubring.gpg", digest);
        lic.setLicenseEncodedFromFile(licenseOutputTextFileName);
        assertTrue(lic.isVerified());
        String z = lic.getLicenseString();
        assertTrue(z.contains("a=b"));
        assertEquals((Long) (-3623885160523215197L), lic.getDecodeKeyId());
        assertEquals("b", lic.getFeature("a"));
        assertNull(lic.getFeature("abraka-dabra"));
    }
}
