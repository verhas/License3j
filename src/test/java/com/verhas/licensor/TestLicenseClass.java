package com.verhas.licensor;

import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.util.Arrays;

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
        final OutputStream os = new FileOutputStream(licenseInputFile);
        os.write("feature=value\n".getBytes());
        os.close();
    }

    @Before
    public void setUp() throws IOException {
        createLicenseInputFile();
    }

    @After
    public void tearDown() throws IOException {
        new File(licenseOutputTextFileName).delete();
        new File(licenseInputFile).delete();
        new File(dumpFile1).delete();
        new File(dumpFile2).delete();
    }

    @Test
    public void calculatesPublicKeyRingDigest() throws IOException,
            PGPException, Exception {
        final License license = new License();
        license.loadKey(fromResource("secring.gpg"),
                "Peter Verhas (licensor test key) <peter@verhas.com>");
        license.loadKeyRingFromResource("pubring.gpg", null);
        final byte[] calculatedDigest = license.calculatePublicKeyRingDigest();
        assert Arrays.equals(calculatedDigest, digest);
    }

    @Test
    public void licenseLoadedFromFileContainsFeatureAndValue()
            throws IOException {
        final License license = new License();
        license.setLicense(new File(licenseInputFile));
        Assert.assertEquals("value", license.getFeature("feature"));
    }

    @Test
    public void dumpsLicenseToFilesAndReadsBack() throws IOException {
        final License license = new License();
        final StringBuilder sb = new StringBuilder();
        final int featureSetSize = 10;
        for (int i = 1; i < featureSetSize; i++) {
            sb.append("key" + i + "=value" + i + "\n");
        }
        license.setLicense(sb.toString());
        license.dumpLicense(new File(dumpFile1));
        license.dumpLicense(dumpFile2);

        License lic = new License();
        lic.setLicense(new File(dumpFile1));
        for (int i = 1; i < featureSetSize; i++) {
            Assert.assertEquals("value" + i, lic.getFeature("key" + i));
        }
        lic = new License();
        lic.setHashAlgorithm(PGPUtil.SHA512);
        lic.setLicense(new File(dumpFile2));
        for (int i = 1; i < featureSetSize; i++) {
            Assert.assertEquals("value" + i, lic.getFeature("key" + i));
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void wrongDigestCausesException() throws IOException {
        final License lic = new License();
        final byte[] myDigest = new byte[digest.length];
        System.arraycopy(digest, 0, myDigest, 0, digest.length);
        myDigest[myDigest.length - 1] = (byte) (myDigest[myDigest.length - 1] ^ 8);
        lic.loadKeyRingFromResource("pubring.gpg", myDigest);
    }

    @Test
    public void loadsEncodedLicenseString() throws IOException, PGPException,
            NoSuchAlgorithmException, NoSuchProviderException,
            SignatureException {
        final License lic = new License();
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
        Assert.assertTrue(lic.isVerified());
        String z = lic.getLicenseString();
        z = z.substring(z.length() - 4);
        Assert.assertEquals("a=b\n", z);
        Assert.assertEquals((Long) (-3623885160523215197L),
                (Long) lic.getDecodeKeyId());
        Assert.assertEquals("b", lic.getFeature("a"));
    }

    @Test
    public void loadsEncodedLicenseFile() throws IOException, PGPException,
            NoSuchAlgorithmException, NoSuchProviderException,
            SignatureException {
        final License lic = new License();
        lic.loadKeyRingFromResource("pubring.gpg", digest);
        lic.setLicenseEncodedFromResource("license-encoded.txt", "utf-8");
        Assert.assertTrue(lic.isVerified());
        String z = lic.getLicenseString();
        z = z.substring(z.length() - 4);
        Assert.assertEquals("a=b\n", z);
        Assert.assertEquals((Long) (-3623885160523215197L),
                (Long) lic.getDecodeKeyId());
        Assert.assertEquals("b", lic.getFeature("a"));
    }

    @Test
    public void encodesLicense1() throws IOException, PGPException,
            NoSuchAlgorithmException, NoSuchProviderException,
            SignatureException {
        final License license = new License();
        license.setLicense("");
        license.setFeature("a", "b");
        license.loadKey(new File(fromResource("secring.gpg")),
                "Peter Verhas (licensor test key) <peter@verhas.com>");
        final String encoded = license.encodeLicense("alma");
        final OutputStream os = new FileOutputStream(licenseOutputTextFileName);
        os.write(encoded.getBytes("utf-8"));
        os.close();
    }

    @Test
    public void testEncodeLicense2() throws IOException, PGPException,
            NoSuchAlgorithmException, NoSuchProviderException,
            SignatureException {
        final License license = new License();
        license.setLicense("");
        license.setFeature("a", "b");
        license.loadKey(fromResource("secring.gpg"),
                "Peter Verhas (licensor test key) <peter@verhas.com>");
        final String encoded = license.encodeLicense("alma");
        final OutputStream os = new FileOutputStream(licenseOutputTextFileName);
        os.write(encoded.getBytes("utf-8"));
        os.close();
    }

    @Test
    public void canSetFeatureAndRetrieveOnFreshLicenseObject() {
        final License license = new License();
        license.setFeature("a", "b");
        Assert.assertEquals("b", license.getFeature("a"));
    }

    @Test
    public void getsNullForNonExistingFeatureOnFreshLicenseObject() {
        final License license = new License();
        Assert.assertNull(license.getFeature("xxx"));
    }

    @Test
    public void getsNullForNonExistingFeature() {
        final License license = new License();
        license.setFeature("a", "b");
        Assert.assertNull(license.getFeature("xxx"));
    }

    @Test
    public void dumpsNothingFromFreshLicenseObject() throws IOException {
        final License license = new License();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        license.dumpLicense(baos);
        Assert.assertEquals(0, baos.size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void loadingKeyWithBadNameThrowsException() throws IOException,
            PGPException, NoSuchAlgorithmException, NoSuchProviderException,
            SignatureException {
        final License license = new License();
        license.setLicense("");
        license.setFeature("a", "b");
        license.loadKey(new File(fromResource("secring.gpg")),
                "Peter Verhas (licensor test kez) <peter@verhas.com>");
        final String encoded = license.encodeLicense("alma");
        final OutputStream os = new FileOutputStream(licenseOutputTextFileName);
        os.write(encoded.getBytes("utf-8"));
        os.close();
    }

    @Test(expected = PGPException.class)
    public void loadingKeyWithBadPasswordThrowsException() throws IOException,
            PGPException, NoSuchAlgorithmException, NoSuchProviderException,
            SignatureException {
        final License license = new License();
        license.setLicense("");
        license.setFeature("a", "b");
        license.loadKey(new File(fromResource("secring.gpg")),
                "Peter Verhas (licensor test key) <peter@verhas.com>");
        final String encoded = license.encodeLicense("bad password");
        final OutputStream os = new FileOutputStream(licenseOutputTextFileName);
        os.write(encoded.getBytes("utf-8"));
        os.close();
    }

    @Test
    public void licenseLoadedFromFileHasAppropriateDigest()
            throws FileNotFoundException, IOException {
        final License license = new License();
        license.loadKeyRing(new File(fromResource("pubring.gpg")), digest);
        final String s = license.dumpPublicKeyRingDigest();
        Assert.assertEquals(digestReference, s);
    }

    @Test
    public void licenseLoadedFromFileNameHasAppropriateDigest()
            throws FileNotFoundException, IOException {
        final License license = new License();
        license.loadKeyRing(fromResource("pubring.gpg"), digest);
        final String s = license.dumpPublicKeyRingDigest();
        Assert.assertEquals(digestReference, s);
    }

    @Test
    public void testMain() throws IOException, PGPException,
            NoSuchAlgorithmException, NoSuchProviderException,
            SignatureException, Exception {

        final License license = new License();
        license.setLicense("");
        license.setFeature("a", "b");
        license.loadKey(fromResource("secring.gpg"),
                "Peter Verhas (licensor test key) <peter@verhas.com>");
        final String encoded = license.encodeLicense("alma");
        final OutputStream os = new FileOutputStream(licenseOutputTextFileName);
        os.write(encoded.getBytes("utf-8"));
        os.close();

        final License lic = new License();
        lic.loadKeyRingFromResource("pubring.gpg", digest);
        lic.setLicenseEncodedFromFile(licenseOutputTextFileName);
        Assert.assertTrue(lic.isVerified());
        String z = lic.getLicenseString();
        Assert.assertEquals(false, -1 == z.indexOf("a=b"));
        Assert.assertEquals((Long) (-3623885160523215197L), lic.getDecodeKeyId());
        Assert.assertEquals("b", lic.getFeature("a"));
        Assert.assertNull(lic.getFeature("abraka-dabra"));
    }
}
