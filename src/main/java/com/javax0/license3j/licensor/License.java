package com.javax0.license3j.licensor;

import com.javax0.license3j.licensor.encrypt.PGPHelper;
import org.bouncycastle.crypto.digests.SHA512Digest;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openpgp.*;
import org.bouncycastle.openpgp.bc.BcPGPPublicKeyRingCollection;
import org.bouncycastle.openpgp.jcajce.JcaPGPObjectFactory;
import org.bouncycastle.openpgp.jcajce.JcaPGPSecretKeyRingCollection;
import org.bouncycastle.openpgp.operator.jcajce.JcaPGPContentVerifierBuilderProvider;

import java.io.*;
import java.security.Security;
import java.util.Iterator;
import java.util.Properties;
import java.util.stream.Stream;

/**
 * A License object is (key,value) pair set that can be interpreted arbitrary.
 * There is no special meaning how to handle these. The License object however
 * can be electronically signed and saved into a file and loaded and verified.
 * <p>
 * You can use a License object for two purposes:
 *
 * <ul>
 * <li>Create a new license from pure text properties file, from a string or
 * build it up programmatically and then sign and save it to a file.
 * <li>Load an encoded license, verify it and then query features.
 * </ul>
 * <p>
 * License files in clear text are simple properties text files. Electronic
 * signature and coding is applied using the format PGP. The library used to
 * handle encryption is BouncyCastle.
 *
 * @author Peter Verhas
 */
public class License {
    private static final String DEFAULT_CHARSET = "utf-8";

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    private final PGPHelper cryptor = new PGPHelper();
    private Properties licenseProperties = null;
    private boolean verified = false;
    private byte[] publicKeyRing = null;
    private Long decodeKeyId = null;

    private static <T> Iterable<T> in(final Iterator<T> iterator) {
        return () -> iterator;
    }

    @SuppressWarnings("unchecked")
    private static Iterable<String> inS(final Iterator iterator) {
        return () -> (Iterator<String>) iterator;
    }

    private static void notNull(Object o) throws PGPException {
        if (o == null) {
            throw new PGPException("can not decode");
        }
    }

    /**
     * Loads a resource and returns it as a string
     *
     * @param resourceName the name of the resource file
     * @return the resource file content as a string. May throw NullPointerException if the resource does not exist.
     * @deprecated the functionality of this method is genertal and has nothing to do
     * with license management. It will be removed from the next version of the library.
     */
    @Deprecated
    public static String fromResource(String resourceName) {
        return License.class.getClassLoader().getResource(resourceName)
            .getFile();
    }

    /**
     * @return a stream of String objects that contain the names of the features
     */
    public Stream<String> features() {
        return licenseProperties
            .keySet()
            .stream()
            .map(s -> (String) s);
    }

    /**
     * Set a license feature.
     *
     * @param key   the name of the feature
     * @param value the string value of the feature
     * @return the license object so method calls can be chained
     */
    public License setFeature(final String key, final String value) {
        if (licenseProperties == null) {
            licenseProperties = new Properties();
        }
        licenseProperties.put(key, value);
        return this;
    }

    /**
     * Get a license feature.
     *
     * @param key the name of the feature.
     * @return the value of the feature as String
     */
    public String getFeature(final String key) {
        final String feature;
        if (licenseProperties != null && licenseProperties.containsKey(key)) {
            feature = licenseProperties.getProperty(key);
        } else {
            feature = null;
        }
        return feature;
    }

    /**
     * Set the license values from an input stream. The stream has to contain
     * properties.
     *
     * @param is the input stream to read the properties values from
     * @throws IOException when the input stream can not be read
     * @deprecated use the version of the method that specified the character set as argument
     */
    @Deprecated
    public void setLicense(final InputStream is) throws IOException {
        verified = false;
        licenseProperties = new Properties();
        licenseProperties.load(is);
    }

    /**
     * Set the license values from an input stream. The stream has to contain
     * properties.
     *
     * @param is      the input stream to read the properties values from
     * @param charset the character set of the input stream, for example "utf-8".
     * @throws IOException when the input stream can not be read
     */
    public void setLicense(final InputStream is, String charset) throws IOException {
        verified = false;
        licenseProperties = new Properties();
        licenseProperties.load(new InputStreamReader(is, charset));
    }

    /**
     * Set the license values from a clear text file. The file has to be a
     * properties file.
     *
     * @param file the file to read the license data.
     * @return {@code this}
     * @throws IOException when the file can not be read
     * @deprecated use the version of the method that also specifies the character set
     */
    @SuppressWarnings("DeprecatedIsStillUsed")
    @Deprecated
    public License setLicense(final File file) throws IOException {
        setLicense(new FileInputStream(file));
        return this;
    }

    /**
     * The same as the method {@link #setLicense(File)} but also the encoding of the
     * file can be specified.
     *
     * @param file    the file to read the license data from.
     * @param charset the character set of the input stream, for example "utf-8".
     * @return the license so that the method can be chained
     * @throws IOException if the file can not be read
     */
    public License setLicense(final File file, String charset) throws IOException {
        setLicense(new FileInputStream(file), charset);
        return this;
    }

    /**
     * Set the license from clear text format. This text should be a properties
     * format file.
     *
     * @param licenseString the properties content.
     * @return the license object so method calls can be chained
     * @throws IOException when the string is badly formatter and therefore the license
     *                     can not be loaded from the string
     */
    public License setLicense(final String licenseString) throws IOException {
        setLicense(new ByteArrayInputStream(licenseString.getBytes(DEFAULT_CHARSET)), DEFAULT_CHARSET);
        return this;
    }

    /**
     * <p>
     * Get the license as clear text. The license has the format that is usual
     * for properties files.
     * </p>
     * <p>
     * Note that this method replaces the "\r\n" line feed sequence with "\n" even on
     * Windows where the properties may be loaded using the Windows separator.
     * </p>
     *
     * @return the license as clear text
     */
    public String getLicenseString() {
        final var baos = new ByteArrayOutputStream();
        try {
            if (licenseProperties != null) {
                licenseProperties.store(baos, "-- license file");
            }
            baos.close();
            final var loadedLicense = new String(baos.toByteArray());
            return loadedLicense.replaceAll("\r\n", "\n");
        } catch (final IOException ex) {
            return "";
        }
    }

    /**
     * Dump the license as clear text into a file.
     *
     * @param fileName the name of the file to dump the license into.
     * @throws IOException when there is an error writing the file
     */
    public void dumpLicense(final String fileName) throws IOException {
        dumpLicense(new File(fileName));
    }

    /**
     * Dump the license as clear text into the file.
     *
     * @param file the file to write the license text.
     * @throws IOException when there is an error writing the file
     */
    public void dumpLicense(final File file) throws IOException {
        dumpLicense(new FileOutputStream(file));
    }

    /**
     * Dump the license as clear text into the output stream.
     *
     * @param os the output stream.
     * @throws IOException if the file can not be read
     */
    public void dumpLicense(final OutputStream os) throws IOException {
        if (licenseProperties != null) {
            licenseProperties.store(os, "");
        }
    }

    /**
     * Check if the license was verified. A license verification checks the
     * electronic signature. If this method returns false, your application
     * program should not believe the rights the license may declare.
     *
     * @return true if the license was verified.
     */
    public boolean isVerified() {
        return verified;
    }

    /**
     * Set the hash algorithm to use to sign the license. The default value is
     * SHA512.
     * <p>
     * Call to this method is needed only when encoding license and only when
     * the default algorithm is not appropriate for some reason or if the caller
     * wants to ensure that the default algorithm is used even when a later
     * version of this library is used.
     * <p>
     * The default algorithm may change in future versions of this class. Note
     * that the default algorithm was SHA1 prior to version 1.0.4
     *
     * @param hashAlgorithm the hash algorithm, a constant from the
     *                      {@code class org.bouncycastle.openpgp.PGPUtil}. For more
     *                      information on this class visit <a href=
     *                      "http://www.bouncycastle.org/docs/pgdocs1.6/org/bouncycastle/openpgp/PGPUtil.html"
     *                      >Bouncy Castle</a>
     * @return the license object so method calls can be chained
     */
    public License setHashAlgorithm(final int hashAlgorithm) {
        cryptor.setHashAlgorithm(hashAlgorithm);
        return this;
    }

    /**
     * Load the secret key to be used to encrypt a license.
     *
     * @param fn    the name of the file that contains the key rings.
     * @param keyId the id of the key to be used.
     * @return the license object so method calls can be chained
     * @throws IOException  if the file can not be read
     * @throws PGPException is underlying pgp library throws
     */
    public License loadKey(final String fn, final String keyId)
        throws IOException, PGPException {
        loadKey(new File(fn), keyId);
        return this;
    }

    /**
     * Load a key ring from a resource file (a file that is packaged into the
     * JAR file). This method invokes
     * {@link #loadKeyRing(InputStream in, byte[] digest)}.
     *
     * @param resourceName the name of the file inside the JAR file with full path.
     * @param digest       the SHA512 digest of the key ring.
     * @throws IOException if the file can not be read
     */
    public License loadKeyRingFromResource(final String resourceName,
                                           final byte[] digest) throws IOException {
        return loadKeyRing(
            this.getClass().getClassLoader()
                .getResourceAsStream(resourceName), digest);
    }

    /**
     * Load a key ring from a file. This method invokes
     * {@link #loadKeyRing(InputStream in, byte[] digest)}.
     *
     * @param fileName the name of the file
     * @param digest   the SHA512 digest of the ring
     * @throws IOException if the file can not be read
     */
    public License loadKeyRing(final String fileName, final byte[] digest)
        throws IOException {
        loadKeyRing(new File(fileName), digest);
        return this;
    }

    /**
     * Load a key ring from a file. This method invokes
     * {@link #loadKeyRing(InputStream in, byte[] digest)}.
     *
     * @param file   the file
     * @param digest the SHA512 digest of the ring
     * @throws IOException if the file can not be read
     */
    public License loadKeyRing(final File file, final byte[] digest)
        throws IOException {
        loadKeyRing(new FileInputStream(file), digest);
        return this;
    }

    /**
     * Load a key ring from an input stream that can be used to verify a
     * license. The key ring can only be loaded if the it is a proper PGP key
     * ring and the SHA512 digest of the key ring matches the parameter
     * 'digest'. This is a simple mechanism to avoid someone to replace the key
     * ring and have a spoof license to be verified.
     *
     * @param in     the input stream where the key ring comes from.
     * @param digest the digest of the key ring. If this parameter is {@code null}
     *               then the key ring is loaded no matter of its checksum.
     * @throws IOException              if the file can not be read
     * @throws IllegalArgumentException when the digest does not match the supplied digest. This
     *                                  means that the JAR file containing the key ring was tampered
     *                                  and the key ring is not the same as the one packaged by the
     *                                  developer. In this case using the key ring is useless to
     *                                  check the authenticity of the license and because of this the
     *                                  key ring is not loaded in such a case.
     */
    public License loadKeyRing(final InputStream in, final byte[] digest)
        throws IOException {
        final var baos = new ByteArrayOutputStream();
        int ch;
        while ((ch = in.read()) >= 0) {
            baos.write(ch);
        }
        publicKeyRing = baos.toByteArray();
        if (digest != null) {
            final var calculatedDigest = calculatePublicKeyRingDigest();
            for (int i = 0; i < calculatedDigest.length; i++) {
                if (calculatedDigest[i] != (digest[i])) {
                    publicKeyRing = null;
                    throw new IllegalArgumentException(
                        "Key ring digest does not match.");
                }
            }
        }
        return this;
    }

    /**
     * Calculate the SHA512 digest of the public key ring that is used to decode
     * the license.
     *
     * @return the digest as a byte array.
     */
    public byte[] calculatePublicKeyRingDigest() {
        final var dig = new SHA512Digest();
        dig.reset();
        dig.update(publicKeyRing, 0, publicKeyRing.length);
        final var digest = new byte[dig.getDigestSize()];
        dig.doFinal(digest, 0);
        return digest;
    }

    /**
     * Dump the public key ring digest as a Java code fragment. You can copy
     * this string into your licensed code that calls {@code loadKeyRing} to
     * protect the code from key ring replacement.
     *
     * @return the Java program code fragment as string.
     */
    public String dumpPublicKeyRingDigest() {
        final var calculatedDigest = calculatePublicKeyRingDigest();
        final var retval = new StringBuilder("byte [] digest = new byte[] {\n");
        for (int i = 0; i < calculatedDigest.length; i++) {
            int intVal = ((int) calculatedDigest[i]) & 0xff;
            retval.append(String.format("(byte)0x%02X, ", intVal));
            if (i % 8 == 0) {
                retval.append("\n");
            }
        }
        retval.append("\n};\n");
        return retval.toString();
    }

    /**
     * Load the secret key to be used to encrypt a license. This is a
     * complimentary method that calls
     * {@link #loadKey(InputStream in, String user)}.
     *
     * @param fin  the file that contains the key ring.
     * @param user the user id of the key to be used.
     * @return this
     * @throws IOException  if the file can not be read
     * @throws PGPException is underlying pgp library throws
     */
    public License loadKey(final File fin, final String user)
        throws IOException, PGPException {
        loadKey(new FileInputStream(fin), user);
        return this;
    }

    /**
     * Load the secret key to be used to encrypt the license. After the key is
     * loaded it can be used to encrypt license files.
     *
     * @param keyRing input stream of the file containing the key rings
     * @param user    the user id of the key. If this parameter is {@code null} then
     *                the first key on the key ring appropriate to sign will be
     *                used.
     * @return this
     * @throws IOException  if the file can not be read
     * @throws PGPException is underlying pgp library throws
     */
    public License loadKey(final InputStream keyRing, final String user)
        throws IOException, PGPException {
        final var decoder = PGPUtil.getDecoderStream(keyRing);

        final var keyRingCollection = new JcaPGPSecretKeyRingCollection(decoder);

        for (final var ring : in(keyRingCollection.getKeyRings())) {
            for (final var key : in(ring.getSecretKeys())) {
                for (final var keyUserId : inS(key.getUserIDs())) {
                    if (PGPHelper.keyIsAppropriate(key, user, keyUserId)) {
                        cryptor.setKey(key);
                        return this;
                    }
                }
            }
        }
        throw new IllegalArgumentException("Can't find signing key in key ring.");
    }


    /**
     * Encode the currently loaded/created license.
     *
     * @param keyPassPhraseString the pass phrase to the signing key that was loaded.
     * @return the license encoded as ascii string.
     * @throws IOException  if the file can not be read
     * @throws PGPException is underlying pgp library throws
     */
    public String encodeLicense(final String keyPassPhraseString)
        throws IOException, PGPException {

        final var licensePlain = getLicenseString();
        return new String(cryptor.encodeLicense(keyPassPhraseString, licensePlain), DEFAULT_CHARSET);
    }

    /**
     * Open an encoded license from the string literal.
     *
     * @param license the license encoded in string format.
     * @return this
     * @throws PGPException is underlying pgp library throws
     * @throws IOException  if the file can not be read
     */
    public License setLicenseEncoded(final String license)
        throws IOException, PGPException {
        try {
            setLicenseEncoded(new ByteArrayInputStream(
                license.getBytes(DEFAULT_CHARSET)), DEFAULT_CHARSET);
        } catch (UnsupportedEncodingException shouldNotEverHappen) {
            throw new RuntimeException(shouldNotEverHappen);
        }
        return this;
    }

    /**
     * Open an encoded license from a Java resource. Use this method when the
     * license is inside the JAR file of the shipped code.
     *
     * @param resourceName the name of the resource that contains the license.
     * @return this
     * @throws PGPException is underlying pgp library throws
     * @throws IOException  if the file can not be read
     * @deprecated because it does not define the character set
     */
    @Deprecated
    public License setLicenseEncodedFromResource(final String resourceName)
        throws IOException, PGPException {
        setLicenseEncoded(License.class.getClassLoader().getResourceAsStream(
            resourceName));
        return this;
    }

    /**
     * Open an encoded license from a Java resource. Use this method when the
     * license is inside the JAR file of the shipped code.
     *
     * @param resourceName the name of the resource that contains the license.
     * @param charset      the character set the resource is encoded
     * @return this
     * @throws PGPException is underlying pgp library throws
     * @throws IOException  if the file can not be read
     */
    public License setLicenseEncodedFromResource(final String resourceName,
                                                 final String charset)
        throws IOException, PGPException {
        setLicenseEncoded(License.class.getClassLoader().getResourceAsStream(
            resourceName), charset);
        return this;
    }

    /**
     * Deprecated method. Use the version with the charset argument.
     *
     * @param fileName the name of the file containing the encoded license.
     * @return this
     * @throws PGPException          is underlying pgp library throws
     * @throws IOException           if the file can not be read
     * @throws FileNotFoundException if the file can not be found
     */
    @Deprecated
    public License setLicenseEncodedFromFile(final String fileName)
        throws IOException, PGPException {
        setLicenseEncodedFromFile(fileName, null);
        return this;
    }

    /**
     * Open an encoded license from a file.
     *
     * @param fileName the name of the file containing the encoded license.
     * @param charset  is the character set the file is encoded
     * @return this
     * @throws PGPException          is underlying pgp library throws
     * @throws IOException           if the file can not be read
     * @throws FileNotFoundException if the file can not be found
     */
    public License setLicenseEncodedFromFile(final String fileName,
                                             final String charset) throws IOException,
        PGPException {
        setLicenseEncoded(new File(fileName), charset);
        return this;
    }

    /**
     * Deprecated. Use {@link #setLicenseEncoded(File file, String charset)}.
     *
     * @param file the file from which the encoded file is read
     * @throws PGPException          is underlying pgp library throws
     * @throws IOException           if the file can not be read
     * @throws FileNotFoundException if the file can not be found
     * @deprecated because it does not define the character set and the new method name is simpler
     * using method overload
     */
    @Deprecated
    public void setLicenseEncodedFromFile(final File file)
        throws IOException, PGPException {
        setLicenseEncoded(file);
    }

    /**
     * Deprecated method. Use the version with the charset argument.
     *
     * @param file the file where the encoded license is.
     * @throws PGPException          is underlying pgp library throws
     * @throws IOException           if the file can not be read
     * @throws FileNotFoundException if the file can not be found
     */
    @Deprecated
    public void setLicenseEncoded(final File file)
        throws IOException, PGPException {
        setLicenseEncoded(file, null);
    }

    /**
     * Open an encoded license file.
     *
     * @param file    the file where the encoded license is.
     * @param charset is the character set the file is encoded
     * @throws PGPException          is underlying pgp library throws
     * @throws IOException           if the file can not be read
     * @throws FileNotFoundException if the file can not be found
     */
    public void setLicenseEncoded(final File file, String charset)
        throws IOException, PGPException {
        setLicenseEncoded(new FileInputStream(file), charset);
    }

    /**
     * Get the key id that was used to decode the license. The protected code
     * may need this id to check the key security level. The protection may
     * store different keys in different location. For example the key to
     * generate expiring demo licenses may be stored on the public server to let
     * it automatically generate demo keys.
     * <p>
     * The key used to generate commercial licenses is stored in a bunker, deep
     * down under the ground between steel walls.
     * <p>
     * In such a situation the protected program will not accept a commercial
     * non expiring license (that could have been stolen) signed by the demo
     * license key.
     *
     * @return the key id.
     */
    public Long getDecodeKeyId() {
        return decodeKeyId;
    }

    /**
     * Open an encoded license from input stream, decode and load it. If the
     * file can not be loaded or is not signed properly then the method
     * {@link #isVerified()} will return false.
     * <p>
     * Otherwise the license is loaded and can be used.
     *
     * @param inputStream where the license is read
     * @throws IOException  if the file can not be read
     * @throws PGPException is underlying pgp library throws
     * @deprecated use the version that specifies the charater set as argument
     */
    @Deprecated
    public void setLicenseEncoded(InputStream inputStream) throws IOException,
        PGPException {
        setLicenseEncoded(inputStream, null);
    }

    /**
     * Open an encoded license from input stream, decode and load it. If the
     * file can not be loaded or is not signed properly then the method
     * {@link #isVerified()} will return false.
     * <p>
     * Otherwise the license is loaded and can be used.
     *
     * @param inputStream from where the license is read
     * @param charset     the character set of the input stream
     * @throws IOException  if the file can not be read
     * @throws PGPException is underlying pgp library throws
     */
    public License setLicenseEncoded(InputStream inputStream, String charset)
        throws IOException, PGPException {
        final ByteArrayInputStream keyIn = new ByteArrayInputStream(
            publicKeyRing);
        final var decoderInputStream = PGPUtil
            .getDecoderStream(inputStream);

        var pgpFact = new JcaPGPObjectFactory(decoderInputStream);
        final PGPCompressedData c1 = (PGPCompressedData) pgpFact.nextObject();
        notNull(c1);
        pgpFact = new JcaPGPObjectFactory(c1.getDataStream());
        final var p1 = (PGPOnePassSignatureList) pgpFact.nextObject();

        notNull(p1);
        final var ops = p1.get(0);
        final var p2 = (PGPLiteralData) pgpFact.nextObject();

        notNull(p2);
        final var dIn = p2.getInputStream();
        notNull(dIn);
        int ch;
        final var pgpRing =
            new BcPGPPublicKeyRingCollection(PGPUtil.getDecoderStream(keyIn));
        notNull(ops);
        decodeKeyId = ops.getKeyID();

        final var decodeKey = pgpRing.getPublicKey(decodeKeyId);
        final var out = new ByteArrayOutputStream();
        try {
            final var cvBuilder = new JcaPGPContentVerifierBuilderProvider();
            ops.init(cvBuilder, decodeKey);
            while ((ch = dIn.read()) >= 0) {
                ops.update((byte) ch);
                out.write(ch);
            }
            final var p3 = (PGPSignatureList) pgpFact.nextObject();

            if (ops.verify(p3.get(0))) {
                if (charset == null) {
                    setLicense(new String(out.toByteArray()));
                } else {
                    setLicense(new String(out.toByteArray(), charset));
                }
                verified = true;
            } else {
                verified = false;
                licenseProperties = null;
            }
        } catch (final Exception e) {
            verified = false;
            licenseProperties = null;
        }
        return this;
    }
}
