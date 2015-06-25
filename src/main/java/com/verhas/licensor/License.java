package com.verhas.licensor;

import static com.verhas.utils.Sugar.in;
import static com.verhas.utils.Sugar.to;
import static com.verhas.utils.Sugar.using;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;
import java.security.SignatureException;
import java.util.Date;
import java.util.Iterator;
import java.util.Properties;

import org.bouncycastle.bcpg.ArmoredOutputStream;
import org.bouncycastle.bcpg.BCPGOutputStream;
import org.bouncycastle.crypto.digests.SHA512Digest;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openpgp.PGPCompressedData;
import org.bouncycastle.openpgp.PGPCompressedDataGenerator;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPLiteralData;
import org.bouncycastle.openpgp.PGPLiteralDataGenerator;
import org.bouncycastle.openpgp.PGPObjectFactory;
import org.bouncycastle.openpgp.PGPOnePassSignature;
import org.bouncycastle.openpgp.PGPOnePassSignatureList;
import org.bouncycastle.openpgp.PGPPrivateKey;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPSecretKey;
import org.bouncycastle.openpgp.PGPSecretKeyRing;
import org.bouncycastle.openpgp.PGPSecretKeyRingCollection;
import org.bouncycastle.openpgp.PGPSignature;
import org.bouncycastle.openpgp.PGPSignatureGenerator;
import org.bouncycastle.openpgp.PGPSignatureList;
import org.bouncycastle.openpgp.PGPSignatureSubpacketGenerator;
import org.bouncycastle.openpgp.PGPUtil;
import org.bouncycastle.openpgp.bc.BcPGPPublicKeyRingCollection;
import org.bouncycastle.openpgp.jcajce.JcaPGPObjectFactory;
import org.bouncycastle.openpgp.jcajce.JcaPGPSecretKeyRingCollection;
import org.bouncycastle.openpgp.operator.PBESecretKeyDecryptor;
import org.bouncycastle.openpgp.operator.PGPContentSignerBuilder;
import org.bouncycastle.openpgp.operator.PGPContentVerifierBuilderProvider;
import org.bouncycastle.openpgp.operator.PGPDigestCalculatorProvider;
import org.bouncycastle.openpgp.operator.jcajce.JcaPGPContentSignerBuilder;
import org.bouncycastle.openpgp.operator.jcajce.JcaPGPContentVerifierBuilderProvider;
import org.bouncycastle.openpgp.operator.jcajce.JcaPGPDigestCalculatorProviderBuilder;
import org.bouncycastle.openpgp.operator.jcajce.JcePBESecretKeyDecryptorBuilder;

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
 *
 * License files in clear text are simple properties text files. Electronic
 * signature and coding is applied using the format PGP. The library used to
 * handle encryption is BouncyCastle.
 *
 * @author Peter Verhas
 */
public class License {

	static {
		Security.addProvider(new BouncyCastleProvider());
	}
	protected Properties licenseProperties = null;
	private boolean verified = false;

	/**
	 * Set a license feature.
	 *
	 * @param key
	 *            the name of the feature
	 * @param value
	 *            the string value of the feature
	 */
	public void setFeature(final String key, final String value) {
		if (licenseProperties == null) {
			licenseProperties = new Properties();
		}
		licenseProperties.put(key, value);
	}

	/**
	 * Get a license feature.
	 *
	 * @param key
	 *            the name of the feature.
	 * @return the value of the feature as String
	 */
	public String getFeature(final String key) {
		final String feature;
		if (licenseProperties != null && licenseProperties.containsKey(key)) {
			feature = (String) licenseProperties.getProperty(key);
		} else {
			feature = null;
		}
		return feature;
	}

	/**
	 * Set the license values from an input stream. The stream has to contain
	 * properties.
	 *
	 * @param is
	 *            the input stream to read the properties values from
	 * @throws IOException
	 *             when the input stream can not be read
	 */
	public void setLicense(final InputStream is) throws IOException {
		verified = false;
		licenseProperties = new Properties();
		licenseProperties.load(is);
	}

	/**
	 * Set the license values from a clear text file. The file has to be a
	 * properties file.
	 *
	 * @param file
	 *            the file to read the license data.
	 * @return {@code this}
	 * @throws IOException
	 *             when the file can not be read
	 */
	public License setLicense(final File file) throws IOException {
		setLicense(new FileInputStream(file));
		return this;
	}

	/**
	 * Set the license from clear text format. This text should be a properties
	 * format file.
	 *
	 * @param licenseString
	 *            the properties content.
	 * @throws IOException
	 *             when the string is badly formatter and therefore the license
	 *             can not be loaded from the string
	 */
	public void setLicense(final String licenseString) throws IOException {
		setLicense(new ByteArrayInputStream(licenseString.getBytes()));
	}

	/**
	 * Get the license as clear text. The license has the format that is usual
	 * for properties files.
	 *
	 * @return the license as clear text
	 */
	public String getLicenseString() {
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			if (licenseProperties != null) {
				licenseProperties.store(baos, "-- license file");
			}
			baos.close();
			return new String(baos.toByteArray());
		} catch (final IOException ex) {
			return "";
		}
	}

	/**
	 * Dump the license as clear text into a file.
	 *
	 * @param fileName
	 *            the name of the file to dump the license into.
	 * @throws IOException
	 *             when there is an error writing the file
	 */
	public void dumpLicense(final String fileName) throws IOException {
		dumpLicense(new File(fileName));
	}

	/**
	 * Dump the license as clear text into the file.
	 *
	 * @param file
	 *            the file to write the license text.
	 * @throws IOException
	 *             when there is an error writing the file
	 */
	public void dumpLicense(final File file) throws IOException {
		dumpLicense(new FileOutputStream(file));
	}

	/**
	 * Dump the license as clear text into the output stream.
	 *
	 * @param os
	 *            the output stream.
	 * @throws IOException
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

	private PGPSecretKey key = null;
	private int hashAlgorithm = PGPUtil.SHA512;

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
	 * @param hashAlgorithm
	 *            the hash algorithm, a constant from the
	 *            {@code class org.bouncycastle.openpgp.PGPUtil}. For more
	 *            information on this class visit <a href=
	 *            "http://www.bouncycastle.org/docs/pgdocs1.6/org/bouncycastle/openpgp/PGPUtil.html"
	 *            >Bouncy Castle</a>
	 */
	public void setHashAlgorithm(final int hashAlgorithm) {
		this.hashAlgorithm = hashAlgorithm;
	}

	/**
	 * Load the secret key to be used to encrypt a license.
	 *
	 * @param fn
	 *            the name of the file that contains the key rings.
	 * @param keyId
	 *            the id of the key to be used.
	 * @return this
	 * @throws java.io.IOException
	 * @throws org.bouncycastle.openpgp.PGPException
	 */
	public License loadKey(final String fn, final String keyId)
			throws IOException, PGPException {
		loadKey(new File(fn), keyId);
		return this;
	}

	private byte[] publicKeyRing = null;

	/**
	 * Load a key ring from a resource file (a file that is packaged into the
	 * JAR file). This method invokes
	 * {@link #loadKeyRing(InputStream in, byte[] digest)}.
	 *
	 * @param resourceName
	 *            the name of the file inside the JAR file with full path.
	 * @param digest
	 *            the SHA512 digest of the key ring.
	 * @throws IOException
	 */
	public void loadKeyRingFromResource(final String resourceName,
			final byte[] digest) throws IOException {
		loadKeyRing(
				this.getClass().getClassLoader()
						.getResourceAsStream(resourceName), digest);
	}

	/**
	 * Load a key ring from a file. This method invokes
	 * {@link #loadKeyRing(InputStream in, byte[] digest)}.
	 *
	 * @param fileName
	 *            the name of the file
	 * @param digest
	 *            the SHA512 digest of the ring
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	public License loadKeyRing(final String fileName, final byte[] digest)
			throws FileNotFoundException, IOException {
		loadKeyRing(new File(fileName), digest);
		return this;
	}

	/**
	 * Load a key ring from a file. This method invokes
	 * {@link #loadKeyRing(InputStream in, byte[] digest)}.
	 *
	 * @param file
	 *            the file
	 * @param digest
	 *            the SHA512 digest of the ring
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	public License loadKeyRing(final File file, final byte[] digest)
			throws FileNotFoundException, IOException {
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
	 * @param in
	 *            the input stream where the key ring comes from.
	 * @param digest
	 *            the digest of the key ring. If this parameter is {@code null}
	 *            then the key ring is loaded no matter of its checksum.
	 * @throws IOException
	 * @throws IllegalArgumentException
	 *             when the digest does not match the supplied digest. This
	 *             means that the JAR file containing the key ring was tampered
	 *             and the key ring is not the same as the one packaged by the
	 *             developer. In this case using the key ring is useless to
	 *             check the authenticity of the license and because of this the
	 *             key ring is not loaded in such a case.
	 */
	public License loadKeyRing(final InputStream in, final byte[] digest)
			throws IOException {
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		int ch;
		while ((ch = in.read()) >= 0) {
			baos.write(ch);
		}
		publicKeyRing = baos.toByteArray();
		if (digest != null) {
			final byte[] calculatedDigest = calculatePublicKeyRingDigest();
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
		final SHA512Digest dig = new SHA512Digest();
		dig.reset();
		dig.update(publicKeyRing, 0, publicKeyRing.length);
		final byte[] digest = new byte[dig.getDigestSize()];
		dig.doFinal(digest, 0);
		return digest;
	}

	private static int convertByteToInt(byte b) {
		return (int) b & 0xff;
	}

	/**
	 * Dump the public key ring digest as a Java code fragment. You can copy
	 * this string into your licensed code that calls {@code loadKeyRing} to
	 * protect the code from key ring replacement.
	 *
	 * @return the Java program code fragment as string.
	 */
	public String dumpPublicKeyRingDigest() {
		final byte[] calculatedDigest = calculatePublicKeyRingDigest();
		String retval = "byte [] digest = new byte[] {\n";
		for (int i = 0; i < calculatedDigest.length; i++) {
			int intVal = convertByteToInt(calculatedDigest[i]);
			retval += String.format("(byte)0x%02X, ", intVal);
			if (i % 8 == 0) {
				retval += "\n";
			}
		}
		retval += "\n};\n";
		return retval;
	}

	/**
	 * Load the secret key to be used to encrypt a license. This is a
	 * complimentary method that calls
	 * {@link #loadKey(InputStream in, String userId)}.
	 *
	 * @param fin
	 *            the file that contains the key ring.
	 * @param userId
	 *            the user id of the key to be used.
	 * @throws java.io.IOException
	 * @throws org.bouncycastle.openpgp.PGPException
	 * @return this
	 */
	public License loadKey(final File fin, final String userId)
			throws IOException, PGPException {
		loadKey(new FileInputStream(fin), userId);
		return this;
	}

	private boolean keyIsAppropriate(String userId, String keyUserId,
			PGPSecretKey k) {
		return k.isSigningKey() && (userId == null || userId.equals(keyUserId));
	}

	/**
	 * Load the secret key to be used to encrypt the license. After the key is
	 * loaded it can be used to encrypt license files.
	 *
	 * @param inputStream
	 *            input stream of the file containing the key rings
	 * @param userId
	 *            the user id of the key. If this parameter is {@code null} then
	 *            the first key on the key ring appropriate to sign will be
	 *            used.
	 * @return this
	 * @throws java.io.IOException
	 * @throws org.bouncycastle.openpgp.PGPException
	 */
	@SuppressWarnings("unchecked")
	public License loadKey(final InputStream inputStream, final String userId)
			throws IOException, PGPException {
		final InputStream decoderInputStream = PGPUtil
				.getDecoderStream(inputStream);

		final PGPSecretKeyRingCollection pgpSec = new JcaPGPSecretKeyRingCollection(
				decoderInputStream);

		key = null;
		for (final PGPSecretKeyRing kRing : in((Iterator<PGPSecretKeyRing>) pgpSec
				.getKeyRings())) {
			for (final PGPSecretKey k : in((Iterator<PGPSecretKey>) kRing
					.getSecretKeys())) {
				for (final String keyUserId : in((Iterator<String>) k
						.getUserIDs())) {
					if (keyIsAppropriate(userId, keyUserId, k)) {
						key = k;
						return this;
					}
				}
			}
		}

		throw new IllegalArgumentException(
				"Can't find signing key in key ring.");
	}

	private PGPSignatureGenerator createPGPSignatureGenerator(
			final char[] keyPassPhrase) throws PGPException {
		final PGPContentSignerBuilder csBuilder = new JcaPGPContentSignerBuilder(
				key.getPublicKey().getAlgorithm(), hashAlgorithm);
		final PGPSignatureGenerator generator = new PGPSignatureGenerator(
				csBuilder);
		final PGPPrivateKey privateKey = extractPGPPrivateKey(keyPassPhrase);

		init(generator, privateKey);
		return generator;
	}

	private PGPPrivateKey extractPGPPrivateKey(final char[] keyPassPhrase)
			throws PGPException {
		PGPDigestCalculatorProvider calcProvider = new JcaPGPDigestCalculatorProviderBuilder()
				.setProvider(BouncyCastleProvider.PROVIDER_NAME).build();
		PBESecretKeyDecryptor decryptor = new JcePBESecretKeyDecryptorBuilder(
				calcProvider).setProvider(BouncyCastleProvider.PROVIDER_NAME)
				.build(keyPassPhrase);
		final PGPPrivateKey pgpPrivKey = key.extractPrivateKey(decryptor);
		return pgpPrivKey;
	}

	private void setHashedSubpackets(
			final PGPSignatureGenerator signatureGenerator) {
		final Iterator<String> it = key.getPublicKey().getUserIDs();
		if (it.hasNext()) {
			final PGPSignatureSubpacketGenerator generator = new PGPSignatureSubpacketGenerator();
			generator.setSignerUserID(false, it.next());
			signatureGenerator.setHashedSubpackets(generator.generate());
		}
	}

	private void init(final PGPSignatureGenerator signatureGenerator,
			final PGPPrivateKey pgpPrivKey) throws PGPException {
		signatureGenerator.init(PGPSignature.BINARY_DOCUMENT, pgpPrivKey);
		setHashedSubpackets(signatureGenerator);
	}

	private PGPCompressedDataGenerator createZlibCompressedDataGenerator() {
		return new PGPCompressedDataGenerator(PGPCompressedData.ZLIB);
	}

	private void encode(final String licensePlain,
			final PGPSignatureGenerator signatureGenerator,
			OutputStream outputStream) throws UnsupportedEncodingException,
			IOException {

		final PGPLiteralDataGenerator literalDataGenerator = new PGPLiteralDataGenerator();
		final OutputStream literalDataStream = literalDataGenerator.open(
				outputStream, PGPLiteralData.BINARY, "licenseFileName-Ignored",
				new Date(), new byte[1024]);
		final InputStream fIn = new ByteArrayInputStream(
				licensePlain.getBytes("utf-8"));
		int ch = 0;
		while ((ch = fIn.read()) >= 0) {
			literalDataStream.write(ch);
			signatureGenerator.update((byte) ch);
		}
		literalDataGenerator.close();
	}

	/**
	 * Encode the currently loaded/created license.
	 *
	 * @param keyPassPhraseString
	 *            the pass phrase to the signing key that was loaded.
	 * @return the license encoded as ascii string.
	 * @throws java.io.IOException
	 * @throws java.security.NoSuchAlgorithmException
	 * @throws java.security.NoSuchProviderException
	 * @throws org.bouncycastle.openpgp.PGPException
	 * @throws java.security.SignatureException
	 */
	public String encodeLicense(final String keyPassPhraseString)
			throws IOException, NoSuchAlgorithmException,
			NoSuchProviderException, PGPException, SignatureException {

		final String licensePlain = getLicenseString();
		final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		final OutputStream armoredOutputStream = new ArmoredOutputStream(
				byteArrayOutputStream);
		final PGPSignatureGenerator signatureGenerator = createPGPSignatureGenerator(keyPassPhraseString
				.toCharArray());

		final PGPCompressedDataGenerator generator = createZlibCompressedDataGenerator();

		final BCPGOutputStream outputStream = new BCPGOutputStream(
				generator.open(armoredOutputStream));

		signatureGenerator.generateOnePassVersion(false).encode(outputStream);
		encode(licensePlain, using(signatureGenerator), to(outputStream));
		signatureGenerator.generate().encode(outputStream);
		generator.close();
		armoredOutputStream.close();
		return new String(byteArrayOutputStream.toByteArray());
	}

	/**
	 * Open an encoded license from the string literal.
	 *
	 * @param licenseStringEncoded
	 *            the license encoded in string format.
	 * @return this
	 * @throws PGPException
	 * @throws IOException
	 * @throws UnsupportedEncodingException
	 */
	public License setLicenseEncoded(final String licenseStringEncoded)
			throws UnsupportedEncodingException, IOException, PGPException {
		setLicenseEncoded(new ByteArrayInputStream(
				licenseStringEncoded.getBytes("utf-8")));
		return this;
	}

	/**
	 * Open an encoded license from a Java resource. Use this method when the
	 * license is inside the JAR file of the shipped code.
	 *
	 * @param resourceName
	 *            the name of the resource that contains the license.
	 * @return this
	 * @throws PGPException
	 * @throws IOException
	 */
	public License setLicenseEncodedFromResource(final String resourceName)
			throws IOException, PGPException {
		setLicenseEncoded(License.class.getClassLoader().getResourceAsStream(
				resourceName));
		return this;
	}

	/**
	 * Deprecated method. Use the version with the charset argument.
	 * 
	 * @param fileName
	 *            the name of the file containing the encoded license.
	 * @return this
	 * @throws PGPException
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	@Deprecated
	public License setLicenseEncodedFromFile(final String fileName)
			throws FileNotFoundException, IOException, PGPException {
		setLicenseEncodedFromFile(fileName, null);
		return this;
	}

	/**
	 * Open an encoded license from a file.
	 *
	 * @param fileName
	 *            the name of the file containing the encoded license.
	 * @param charset
	 *            is the character set the file is encoded
	 * @return this
	 * @throws PGPException
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	public License setLicenseEncodedFromFile(final String fileName,
			final String charset) throws FileNotFoundException, IOException,
			PGPException {
		setLicenseEncoded(new File(fileName), charset);
		return this;
	}

	/**
	 * Deprecated. Use {@link #setLicenseEncoded(File file)}.
	 *
	 * @param file
	 * @throws PGPException
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	@Deprecated
	public void setLicenseEncodedFromFile(final File file)
			throws FileNotFoundException, IOException, PGPException {
		setLicenseEncoded(file);
	}

	/**
	 * Deprecated method. Use the version with the charset argument.
	 *
	 * @param file
	 *            the file where the encoded license is.
	 * @throws PGPException
	 * @throws IOException
	 * @throws FileNotFoundException
	 * 
	 */
	@Deprecated
	public void setLicenseEncoded(final File file)
			throws FileNotFoundException, IOException, PGPException {
		setLicenseEncoded(file, null);
	}

	/**
	 * Open an encoded license file.
	 *
	 * @param file
	 *            the file where the encoded license is.
	 * @param charset
	 *            is the character set the file is encoded
	 * @throws PGPException
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	public void setLicenseEncoded(final File file, String charset)
			throws FileNotFoundException, IOException, PGPException {
		setLicenseEncoded(new FileInputStream(file), charset);
	}

	private Long decodeKeyId = null;

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

	private static void pgpAssertNotNull(Object o) throws PGPException {
		if (o == null) {
			throw new PGPException("can not decode");
		}
	}

	/**
	 * Open an encoded license from input stream, decode and load it. If the
	 * file can not be loaded or is not signed properly then the method
	 * {@link #isVerified()} will return false.
	 * <p>
	 * Otherwise the license is loaded and can be used.
	 *
	 * @param inputStream
	 * @throws IOException
	 * @throws PGPException
	 */
	public void setLicenseEncoded(InputStream inputStream) throws IOException,
			PGPException {
		setLicenseEncoded(inputStream, null);
	}

	public void setLicenseEncoded(InputStream inputStream, String charset)
			throws IOException, PGPException {
		final ByteArrayInputStream keyIn = new ByteArrayInputStream(
				publicKeyRing);
		final InputStream decoderInputStream = PGPUtil
				.getDecoderStream(inputStream);

		PGPObjectFactory pgpFact = new JcaPGPObjectFactory(decoderInputStream);
		final PGPCompressedData c1 = (PGPCompressedData) pgpFact.nextObject();
		pgpAssertNotNull(c1);
		pgpFact = new JcaPGPObjectFactory(c1.getDataStream());
		final PGPOnePassSignatureList p1 = (PGPOnePassSignatureList) pgpFact
				.nextObject();

		pgpAssertNotNull(p1);
		final PGPOnePassSignature ops = p1.get(0);
		final PGPLiteralData p2 = (PGPLiteralData) pgpFact.nextObject();

		pgpAssertNotNull(p2);
		final InputStream dIn = p2.getInputStream();
		pgpAssertNotNull(dIn);
		int ch;
		final BcPGPPublicKeyRingCollection pgpRing = new BcPGPPublicKeyRingCollection(
				PGPUtil.getDecoderStream(keyIn));
		pgpAssertNotNull(ops);
		decodeKeyId = ops.getKeyID();
		if (decodeKeyId == null) {
			// there is no key in the key ring that can decode the license
			verified = false;
			licenseProperties = null;
		} else {
			final PGPPublicKey decodeKey = pgpRing.getPublicKey(decodeKeyId);
			final ByteArrayOutputStream out = new ByteArrayOutputStream();
			try {
				final PGPContentVerifierBuilderProvider cvBuilder = new JcaPGPContentVerifierBuilderProvider();
				ops.init(cvBuilder, decodeKey);
				while ((ch = dIn.read()) >= 0) {
					ops.update((byte) ch);
					out.write(ch);
				}
				final PGPSignatureList p3 = (PGPSignatureList) pgpFact
						.nextObject();

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
		}
	}

	public static String fromResource(String resourceName) {
		return License.class.getClassLoader().getResource(resourceName)
				.getFile();
	}
}
