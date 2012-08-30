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
import java.io.PrintStream;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;
import java.security.SignatureException;
import java.util.Date;
import java.util.Iterator;
import java.util.Properties;
import org.apache.log4j.Logger;
import org.bouncycastle.bcpg.ArmoredOutputStream;
import org.bouncycastle.bcpg.BCPGOutputStream;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPLiteralData;
import org.bouncycastle.openpgp.PGPCompressedDataGenerator;
import org.bouncycastle.openpgp.PGPCompressedData;
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
 * There is no special meaning how to handle these. The License object however can
 * be electronically signed and saved into a file and loaded and verified.
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
  private static Logger logger = Logger.getLogger(License.class);
  protected Properties licenseProperties = null;
  private boolean verified = false;

  /**
   * Set a license feature.
   *
   * @param key the name of the feature
   * @param value the string value of the feature
   */
  public void setFeature(String key, String value) {
    licenseProperties.put(key, value);
  }

  /**
   * Get a license feature.
   *
   * @param key the name of the feature.
   * @return
   */
  public String getFeature(String key) {
    if (licenseProperties == null) {
      return null;
    }
    if (licenseProperties.containsKey(key)) {
      return (String) licenseProperties.getProperty(key);
    } else {
      return null;
    }
  }

  /**
   * Set the license values from a clear text file. The file has to be a
   * properties file.
   *
   * @param file the file to read the license data.
   * @throws IOException when the file can not be read
   */
  public void setLicense(File file) throws IOException {
    verified = false;
    licenseProperties = new Properties();
    licenseProperties.load(new FileInputStream(file));
  }

  /**
   * Set the license from clear text format. This text should be a properties
   * format file.
   *
   * @param licenseString the properties content.
   * @throws IOException when the string is badly formatter and therefore the
   * license can not be loaded from the string
   */
  public void setLicense(String licenseString) throws IOException {
    verified = false;
    licenseProperties = new Properties();
    licenseProperties.load(new ByteArrayInputStream(licenseString.getBytes()));
  }

  /**
   * Get the license as clear text. The license has the format that is usual
   * for properties files.
   *
   * @return the license as clear text
   */
  public String getLicenseString() {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try {
      licenseProperties.store(baos, "-- license file");
      return new String(baos.toByteArray());
    } catch (IOException ex) {
      return "";
    }
  }

  /**
   * Dump the license as clear text into a file.
   * @param fileName the name of the file to dump the license into.
   * @throws java.io.FileNotFoundException
   */
  public void dumpLicense(String fileName) throws FileNotFoundException {
    dumpLicense(new File(fileName));
  }

  /**
   * Dump the license as clear text into the file.
   *
   * @param file the file to write the license text.
   * @throws java.io.FileNotFoundException
   */
  public void dumpLicense(File file) throws FileNotFoundException {
    dumpLicense(new FileOutputStream(file));
  }

  /**
   * Dump the license as clear text into the output stream.
   *
   * @param os the output stream.
   */
  public void dumpLicense(OutputStream os) {
    licenseProperties.list(new PrintStream(os));
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
  private int hashAlgorithm = PGPUtil.SHA1; // default is SHA1

  /**
   * Set the hash algorithm to use to sign the license. The default value is
   * SHA1.
   * <p>
   * Call to this method is needed only when encoding license and only when
   * the default algorithm is not appropriate for some reason or if the caller
   * wants to ensure that the default algorithm is used even when a later
   * version of this library is used.
   * <p>
   * The default algorithm may change in future versions of this class.
   *
   * @param hashAlgorithm the hash algorithm, a constant from the class
   * PGPUtil. For more information on this class visit
   * http://www.bouncycastle.org/docs/pgdocs1.6/org/bouncycastle/openpgp/PGPUtil.html
   */
  public void setHashAlgorithm(int hashAlgorithm) {
    this.hashAlgorithm = hashAlgorithm;
  }

  /**
   * Load the secret key to be used to encrypt a license.
   *
   * @param fn the name of the file that contains the key rings.
   * @param userId the user id of the key to be used.
   * @throws java.io.IOException
   * @throws org.bouncycastle.openpgp.PGPException
   */
  public void loadKey(String fn,
          String userId)
          throws IOException, PGPException {
    loadKey(new File(fn), userId);
  }
  byte[] publicKeyRing = null;

  /**
   * Load a key ring from a resource file (a file that is packaged into the
   * JAR file). This method invokes
   * {@see #loadKeyRing(InputStream in, byte[] digest)}.
   *
   * @param resourceName the name of the file inside the JAR file with full
   * path.
   * @param digest the SHA512 digest of the key ring.
   * @throws java.lang.Exception
   */
  public void loadKeyRingFromResource(String resourceName, byte[] digest)
          throws Exception {
    loadKeyRing(License.class.getClassLoader().
            getResourceAsStream(resourceName), digest);
  }

  /**
   * Load a key ring from a file.
   * This method invokes
   * {@see #loadKeyRing(InputStream in, byte[] digest)}.
   *
   * @param fileName the name of the file
   * @param digest the SHA512 digest of the ring
   * @throws java.lang.Exception
   * @throws java.io.IOException
   */
  public void loadKeyRing(String fileName, byte[] digest) throws Exception,
          IOException {
    try {
      loadKeyRing(new File(fileName), digest);
    } catch (Exception e) {
      logger.error("Can not load key ring from the file '" + fileName + "'", e);
    }
  }

  /**
   * Load a key ring from a file.
   * This method invokes
   * {@see #loadKeyRing(InputStream in, byte[] digest)}.
   *
   * @param file the file
   * @param digest the SHA512 digest of the ring
   * @throws java.lang.Exception
   */
  public void loadKeyRing(File file, byte[] digest) throws Exception {
    loadKeyRing(new FileInputStream(file), digest);
  }

  /**
   * Load a key ring from an input stream that can be used to verify a
   * license. The key ring can only be loaded if the it is a proper PGP
   * key ring and the SHA512 digest of the key ring matches the parameter
   * 'digest'. This is a simple mechanism to avoid someone to replace the
   * key ring and have a spoof license to be verified.
   *
   * @param in the input stream where the key ring comes from.
   * @param digest the digest of the key ring. If this parameter is
   * {@code null} then the key ring is loaded no matter of its checksum.
   * @throws java.lang.Exception
   */
  public void loadKeyRing(InputStream in, byte[] digest) throws Exception {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    int ch;
    while ((ch = in.read()) >= 0) {
      baos.write(ch);
    }
    publicKeyRing = baos.toByteArray();
    if (digest != null) {
      byte[] calculatedDigest = calculatePublicKeyRingDigest();
      for (int i = 0; i < calculatedDigest.length; i++) {
        if (calculatedDigest[i] != (digest[i])) {
          publicKeyRing = null;
          throw new Exception("Key ring digest does not match.");
        }
      }
    }
  }

  /**
   * Calculate the SHA512 digest of the public key ring that is used to
   * decode the license.
   *
   * @return the digest as a byte array.
   */
  public byte[] calculatePublicKeyRingDigest() {
    SHA256Digest dig = new SHA256Digest();
    dig.reset();
    dig.update(publicKeyRing, 0, publicKeyRing.length);
    byte[] digest = new byte[32];
    dig.doFinal(digest, 0);
    return digest;
  }

  /**
   * Dump the public key ring digest as a Java code fragment.
   * You can copy this string into your licensed code that calls
   * {@code loadKeyRing} to protect the code from key ring replacement.
   *
   * @return the Java program code fragment as string.
   */
  public String dumpPublicKeyRingDigest() {
    byte[] calculatedDigest = calculatePublicKeyRingDigest();
    String retval = "byte [] digest = new byte[] {\n";
    for (int i = 0; i < calculatedDigest.length; i++) {
      int intVal = (int) (int) calculatedDigest[i];
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
   * complimentary method that calls
   * {@see #loadKey(InputStream in, String userId)}.
   *
   * @param fin the file that contains the key ring.
   * @param userId the user id of the key to be used.
   * @throws java.io.IOException
   * @throws org.bouncycastle.openpgp.PGPException
   */
  public void loadKey(File fin,
          String userId)
          throws IOException, PGPException {
    loadKey(new FileInputStream(fin), userId);
  }

  /**
   * Load the secret key to be used to encrypt the license. After the key is
   * loaded it can be used to encrypt license files.
   *
   * @param in input stream of the file containing the key rings
   * @param userId the user id of the key. If this parameter is null then
   * the first key on the key ring appropriate to sign will be used.
   * @throws java.io.IOException
   * @throws org.bouncycastle.openpgp.PGPException
   */
  public void loadKey(InputStream in, String userId)
          throws IOException, PGPException {
    in = PGPUtil.getDecoderStream(in);

    PGPSecretKeyRingCollection pgpSec = new PGPSecretKeyRingCollection(in);
    key = null;
    Iterator rIt = pgpSec.getKeyRings();
    while (key == null && rIt.hasNext()) {
      PGPSecretKeyRing kRing = (PGPSecretKeyRing) rIt.next();
      Iterator kIt = kRing.getSecretKeys();

      while (key == null && kIt.hasNext()) {
        PGPSecretKey k = (PGPSecretKey) kIt.next();
        Iterator userIds = k.getUserIDs();
        while (userIds.hasNext()) {
          String keyUserId = (String) userIds.next();
          if (userId == null) {
            if (k.isSigningKey()) {
              key = k;
              return;
            }
          } else if (userId.equals(keyUserId)
                  && k.isSigningKey()) {
            key = k;
            return;
          }
        }
      }
    }

    if (key == null) {
      throw new IllegalArgumentException(
              "Can't find signing key in key ring.");
    }
  }

  /**
   * Encode the currently loaded/created license.
   *
   * @param keyPassPhraseString the pass phrase to the signing key that
   * was loaded.
   * @return the license encoded as ascii string.
   * @throws java.io.IOException
   * @throws java.security.NoSuchAlgorithmException
   * @throws java.security.NoSuchProviderException
   * @throws org.bouncycastle.openpgp.PGPException
   * @throws java.security.SignatureException
   */
  public String encodeLicense(
          String keyPassPhraseString)
          throws IOException, NoSuchAlgorithmException,
          NoSuchProviderException, PGPException, SignatureException {
    char[] keyPassPhrase = keyPassPhraseString.toCharArray();
    String licensePlain = getLicenseString();
    ByteArrayOutputStream baOut = new ByteArrayOutputStream();
    OutputStream out = new ArmoredOutputStream(baOut);

    PGPPrivateKey pgpPrivKey = key.extractPrivateKey(keyPassPhrase, "BC");
    PGPSignatureGenerator sGen = new PGPSignatureGenerator(key.getPublicKey().
            getAlgorithm(), hashAlgorithm, "BC");

    sGen.initSign(PGPSignature.BINARY_DOCUMENT, pgpPrivKey);

    Iterator it = key.getPublicKey().getUserIDs();
    if (it.hasNext()) {
      PGPSignatureSubpacketGenerator spGen =
              new PGPSignatureSubpacketGenerator();

      spGen.setSignerUserID(false, (String) it.next());
      sGen.setHashedSubpackets(spGen.generate());
    }

    PGPCompressedDataGenerator cGen = new PGPCompressedDataGenerator(
            PGPCompressedData.ZLIB);

    BCPGOutputStream bOut = new BCPGOutputStream(cGen.open(out));

    sGen.generateOnePassVersion(false).encode(bOut);

    PGPLiteralDataGenerator lGen = new PGPLiteralDataGenerator();
    OutputStream lOut = lGen.open(bOut, PGPLiteralData.BINARY,
            "licenseFileName-Ignored", new Date(), new byte[1024]);
    InputStream fIn = new ByteArrayInputStream(
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
   * @param licenseStringEncoded the license encoded in string format.
   * @throws java.lang.Exception
   */
  public void setLicenseEncoded(String licenseStringEncoded) throws Exception {
    setLicenseEncoded(new ByteArrayInputStream(licenseStringEncoded.getBytes(
            "utf-8")));
  }

  /**
   * Open an encoded license from a Java resource. Use this method when the
   * license is inside the JAR file of the shipped code.
   *
   * @param resourceName the name of the resource that contains the license.
   * @throws java.lang.Exception
   */
  public void setLicenseEncodedFromResource(String resourceName)
          throws Exception {
    setLicenseEncoded(License.class.getClassLoader().
            getResourceAsStream(resourceName));
  }

  /**
   * Open an encoded license from a file.
   *
   * @param fileName the name of the file containing the encoded license.
   * @throws java.lang.Exception
   */
  public void setLicenseEncodedFromFile(String fileName)
          throws Exception {
    setLicenseEncoded(new File(fileName));
  }

  /**
   * Same as {@code setLicenseEncoded(File file)}.
   * @param file
   * @throws java.lang.Exception
   */
  public void setLicenseEncodedFromFile(File file) throws Exception {
    setLicenseEncoded(file);
  }

  /**
   * Open an encoded license file.
   *
   * @param file the file where the encoded license is.
   * @throws java.lang.Exception
   */
  public void setLicenseEncoded(File file) throws Exception {
    setLicenseEncoded(new FileInputStream(file));
  }
  private Long decodeKeyId = null;

  /**
   * Get the key id that was used to decode the license.
   * The protected code may need this id to check the key security level.
   * The protection may store different keys in different location. For
   * example the key to generate expiring demo licenses may be stored
   * on the public server to let it automatically generate demo keys.
   * <p>
   * The key used to generate commercial licenses is stored in a bunker,
   * deep down under the ground between steel walls.
   * <p>
   * In such a situation the protected program will not accept a commercial
   * non expiring license (that could have been stolen) signed by the
   * demo license key.
   *
   * @return the key id.
   */
  public Long getDecodeKeyId() {
    return decodeKeyId;
  }

  /**
   * Open an encoded license from input stream and decode and load it.
   * If the file can not be loaded or is not signed properly then the
   * method {@see #isVerified()} will return false.
   * <p>
   * Otherwise the license will be loaded and can be used.
   * @param in
   * @throws java.lang.Exception
   */
  public void setLicenseEncoded(InputStream in)
          throws Exception {
    ByteArrayInputStream keyIn = new ByteArrayInputStream(publicKeyRing);
    in = PGPUtil.getDecoderStream(in);

    PGPObjectFactory pgpFact = new PGPObjectFactory(in);
    PGPCompressedData c1 = (PGPCompressedData) pgpFact.nextObject();
    pgpFact = new PGPObjectFactory(c1.getDataStream());
    PGPOnePassSignatureList p1 =
            (PGPOnePassSignatureList) pgpFact.nextObject();
    PGPOnePassSignature ops = p1.get(0);
    PGPLiteralData p2 = (PGPLiteralData) pgpFact.nextObject();
    InputStream dIn = p2.getInputStream();

    int ch;
    PGPPublicKeyRingCollection pgpRing = new PGPPublicKeyRingCollection(
            PGPUtil.getDecoderStream(keyIn));
    decodeKeyId = ops.getKeyID();
    if (decodeKeyId == null) {
      // there is no key in the key ring that can decode the license
      verified = false;
      licenseProperties = null;
    } else {
      PGPPublicKey decodeKey = pgpRing.getPublicKey(decodeKeyId);
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      try {
        ops.initVerify(decodeKey, "BC");
        while ((ch = dIn.read()) >= 0) {
          ops.update((byte) ch);
          out.write(ch);
        }
        PGPSignatureList p3 = (PGPSignatureList) pgpFact.nextObject();

        if (ops.verify(p3.get(0))) {
          setLicense(new String(out.toByteArray()));
          verified = true;
        } else {
          verified = false;
          licenseProperties = null;
        }
      } catch (Exception e) {
        verified = false;
        licenseProperties = null;
      }
    }
  }
}
