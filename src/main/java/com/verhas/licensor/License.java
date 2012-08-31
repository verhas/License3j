package com.verhas.licensor;

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
import org.bouncycastle.openpgp.PGPPublicKeyRingCollection;
import org.bouncycastle.openpgp.PGPSecretKey;
import org.bouncycastle.openpgp.PGPSecretKeyRing;
import org.bouncycastle.openpgp.PGPSecretKeyRingCollection;
import org.bouncycastle.openpgp.PGPSignature;
import org.bouncycastle.openpgp.PGPSignatureGenerator;
import org.bouncycastle.openpgp.PGPSignatureList;
import org.bouncycastle.openpgp.PGPSignatureSubpacketGenerator;
import org.bouncycastle.openpgp.PGPUtil;

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
 * @author Peter Verhas <peter@verhas.com>
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
		licenseProperties.put(key, value);
	}

	/**
	 * Get a license feature.
	 * 
	 * @param key
	 *            the name of the feature.
	 * @return
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

	private void setLicense(final InputStream is) throws IOException {
		verified = false;
		(licenseProperties = new Properties()).load(is);
	}

	/**
	 * Set the license values from a clear text file. The file has to be a
	 * properties file.
	 * 
	 * @param file
	 *            the file to read the license data.
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
			licenseProperties.store(baos, "-- license file");
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
		licenseProperties.store(os, "");
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
	 * @param userId
	 *            the user id of the key to be used.
	 * @throws java.io.IOException
	 * @throws org.bouncycastle.openpgp.PGPException
	 */
	public License loadKey(final String fn, final String userId)
			throws IOException, PGPException {
		loadKey(new File(fn), userId);
		return this;
	}

	private byte[] publicKeyRing = null;

	/**
	 * Load a key ring from a resource file (a file that is packaged into the
	 * JAR file). This method invokes {@see #loadKeyRing(InputStream in, byte[]
	 * digest)}.
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
	 * Load a key ring from a file. This method invokes {@see
	 * #loadKeyRing(InputStream in, byte[] digest)}.
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
	 * Load a key ring from a file. This method invokes {@see
	 * #loadKeyRing(InputStream in, byte[] digest)}.
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
			int intVal = (int) calculatedDigest[i];
			if (intVal < 0) {
				intVal += 256;
			}
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
	 * complimentary method that calls {@see #loadKey(InputStream in, String
	 * userId)}.
	 * 
	 * @param fin
	 *            the file that contains the key ring.
	 * @param userId
	 *            the user id of the key to be used.
	 * @throws java.io.IOException
	 * @throws org.bouncycastle.openpgp.PGPException
	 */
	public License loadKey(final File fin, final String userId)
			throws IOException, PGPException {
		loadKey(new FileInputStream(fin), userId);
		return this;
	}

	/**
	 * Load the secret key to be used to encrypt the license. After the key is
	 * loaded it can be used to encrypt license files.
	 * 
	 * @param in
	 *            input stream of the file containing the key rings
	 * @param userId
	 *            the user id of the key. If this parameter is null then the
	 *            first key on the key ring appropriate to sign will be used.
	 * @throws java.io.IOException
	 * @throws org.bouncycastle.openpgp.PGPException
	 */
	@SuppressWarnings("unchecked")
	public License loadKey(InputStream in, final String userId)
			throws IOException, PGPException {
		in = PGPUtil.getDecoderStream(in);

		final PGPSecretKeyRingCollection pgpSec = new PGPSecretKeyRingCollection(
				in);
		key = null;
		final Iterator<PGPSecretKeyRing> rIt = pgpSec.getKeyRings();
		while (key == null && rIt.hasNext()) {
			final PGPSecretKeyRing kRing = rIt.next();
			final Iterator<PGPSecretKey> kIt = kRing.getSecretKeys();

			while (key == null && kIt.hasNext()) {
				final PGPSecretKey k = kIt.next();
				final Iterator<String> userIds = k.getUserIDs();
				while (userIds.hasNext()) {
					final String keyUserId = userIds.next();
					if (userId == null) {
						if (k.isSigningKey()) {
							key = k;
							return this;
						}
					} else if (userId.equals(keyUserId) && k.isSigningKey()) {
						key = k;
						return this;
					}
				}
			}
			return this;
		}

		if (key == null) {
			throw new IllegalArgumentException(
					"Can't find signing key in key ring.");
		}
		return this;
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
		final char[] keyPassPhrase = keyPassPhraseString.toCharArray();
		final String licensePlain = getLicenseString();
		final ByteArrayOutputStream baOut = new ByteArrayOutputStream();
		final OutputStream out = new ArmoredOutputStream(baOut);

		final PGPPrivateKey pgpPrivKey = key.extractPrivateKey(keyPassPhrase,
				"BC");
		final PGPSignatureGenerator sGen = new PGPSignatureGenerator(key
				.getPublicKey().getAlgorithm(), hashAlgorithm, "BC");

		sGen.initSign(PGPSignature.BINARY_DOCUMENT, pgpPrivKey);

		@SuppressWarnings("unchecked")
		final Iterator<String> it = key.getPublicKey().getUserIDs();
		if (it.hasNext()) {
			final PGPSignatureSubpacketGenerator spGen = new PGPSignatureSubpacketGenerator();

			spGen.setSignerUserID(false, it.next());
			sGen.setHashedSubpackets(spGen.generate());
		}

		final PGPCompressedDataGenerator cGen = new PGPCompressedDataGenerator(
				PGPCompressedData.ZLIB);

		final BCPGOutputStream bOut = new BCPGOutputStream(cGen.open(out));

		sGen.generateOnePassVersion(false).encode(bOut);

		final PGPLiteralDataGenerator lGen = new PGPLiteralDataGenerator();
		final OutputStream lOut = lGen.open(bOut, PGPLiteralData.BINARY,
				"licenseFileName-Ignored", new Date(), new byte[1024]);
		final InputStream fIn = new ByteArrayInputStream(
				licensePlain.getBytes("utf-8"));
		int ch = 0;
		while ((ch = fIn.read()) >= 0) {
			lOut.write(ch);
			sGen.update((byte) ch);
		}
		lGen.close();
		sGen.generate().encode(bOut);
		cGen.close();
		out.close();
		return new String(baOut.toByteArray());
	}

	/**
	 * Open an encoded license from the string literal.
	 * 
	 * @param licenseStringEncoded
	 *            the license encoded in string format.
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
	 * Open an encoded license from a file.
	 * 
	 * @param fileName
	 *            the name of the file containing the encoded license.
	 * @throws PGPException
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	public License setLicenseEncodedFromFile(final String fileName)
			throws FileNotFoundException, IOException, PGPException {
		setLicenseEncoded(new File(fileName));
		return this;
	}

	/**
	 * Same as {@code setLicenseEncoded(File file)}.
	 * 
	 * @param file
	 * @throws PGPException
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	public void setLicenseEncodedFromFile(final File file)
			throws FileNotFoundException, IOException, PGPException {
		setLicenseEncoded(file);
	}

	/**
	 * Open an encoded license file.
	 * 
	 * @param file
	 *            the file where the encoded license is.
	 * @throws PGPException
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	public void setLicenseEncoded(final File file)
			throws FileNotFoundException, IOException, PGPException {
		setLicenseEncoded(new FileInputStream(file));
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

	/**
	 * Open an encoded license from input stream and decode and load it. If the
	 * file can not be loaded or is not signed properly then the method {@see
	 * #isVerified()} will return false.
	 * <p>
	 * Otherwise the license will be loaded and can be used.
	 * 
	 * @param in
	 * @throws IOException
	 * @throws PGPException
	 */
	public void setLicenseEncoded(InputStream in) throws IOException,
			PGPException {
		final ByteArrayInputStream keyIn = new ByteArrayInputStream(
				publicKeyRing);
		in = PGPUtil.getDecoderStream(in);

		PGPObjectFactory pgpFact = new PGPObjectFactory(in);
		final PGPCompressedData c1 = (PGPCompressedData) pgpFact.nextObject();
		pgpFact = new PGPObjectFactory(c1.getDataStream());
		final PGPOnePassSignatureList p1 = (PGPOnePassSignatureList) pgpFact
				.nextObject();
		final PGPOnePassSignature ops = p1.get(0);
		final PGPLiteralData p2 = (PGPLiteralData) pgpFact.nextObject();
		final InputStream dIn = p2.getInputStream();

		int ch;
		final PGPPublicKeyRingCollection pgpRing = new PGPPublicKeyRingCollection(
				PGPUtil.getDecoderStream(keyIn));
		decodeKeyId = ops.getKeyID();
		if (decodeKeyId == null) {
			// there is no key in the key ring that can decode the license
			verified = false;
			licenseProperties = null;
		} else {
			final PGPPublicKey decodeKey = pgpRing.getPublicKey(decodeKeyId);
			final ByteArrayOutputStream out = new ByteArrayOutputStream();
			try {
				ops.initVerify(decodeKey, "BC");
				while ((ch = dIn.read()) >= 0) {
					ops.update((byte) ch);
					out.write(ch);
				}
				final PGPSignatureList p3 = (PGPSignatureList) pgpFact
						.nextObject();

				if (ops.verify(p3.get(0))) {
					setLicense(new String(out.toByteArray()));
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
}
