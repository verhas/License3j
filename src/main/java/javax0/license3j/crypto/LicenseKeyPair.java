package javax0.license3j.crypto;

import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;

/**
 * A {@code LisenceKeyPair} contains the public and the private key. In some cases one of the keys may be null.
 * Theobject also contains the string that identifies the signing algorithm to be used with the key pair.
 */
public class LicenseKeyPair {
    private final KeyPair pair;
    private final String cipherTransformation;

    /**
     * Get the stored cipher string as described in the ORACLE documentation:
     * <p>
     * https://docs.oracle.com/javase/7/docs/api/javax/crypto/Cipher.html
     *
     * @return the ipher string as was specified by the user who created the key.
     */
    public String cipher() {
        return cipherTransformation;
    }

    /**
     * Create a new object storing the key pair and the cipher string.
     *
     * @param pair                 the key pair
     * @param cipherTransformation the cipher string
     */
    private LicenseKeyPair(KeyPair pair, String cipherTransformation) {
        this.pair = pair;
        this.cipherTransformation = cipherTransformation;
    }

    /**
     * Get the key pair from the object.
     *
     * @return the key pair.
     */
    public KeyPair getPair() {
        return pair;
    }

    /**
     * Get the byte representation of the private key as it is returned by the underlying security library. This is
     * NOT the byte array that contains the algorithm at the start. This is the key in raw format.
     *
     * @return the key as bytes
     */
    public byte[] getPrivate() {
        keyNotNull(pair.getPrivate());
        Key key = pair.getPrivate();
        return getKeyBytes(key);
    }

    /**
     * Get the byte representation of the public key as it is returned by the underlying security library. This is
     * NOT the byte array that contains the algorithm at the start. This is the key in raw format.
     *
     * @return the key as bytes
     */
    public byte[] getPublic() {
        keyNotNull(pair.getPublic());
        Key key = pair.getPublic();
        return getKeyBytes(key);
    }

    private byte[] getKeyBytes(Key key) {
        final var algorithm = cipherTransformation.getBytes(StandardCharsets.UTF_8);
        final var len = algorithm.length + 1 + key.getEncoded().length;
        final var buffer = new byte[len];
        System.arraycopy(algorithm, 0, buffer, 0, algorithm.length);
        buffer[algorithm.length] = 0x00;
        System.arraycopy(key.getEncoded(), 0, buffer, algorithm.length + 1, key.getEncoded().length);
        return buffer;
    }

    private void keyNotNull(Key key) {
        if (key == null) {
            throw new IllegalArgumentException("KeyPair does not have the key");
        }
    }


    public static class Create {
        /**
         * Create a new key pair.
         *
         * @param publicKey  to be stored in the object
         * @param privateKey to be stored in the object
         * @param cipher     to be stored in the object
         * @return the new object
         */
        public static LicenseKeyPair from(final PublicKey publicKey, PrivateKey privateKey, final String cipher) {
            return new LicenseKeyPair(new KeyPair(publicKey, privateKey), cipher);
        }

        /**
         * Create a new key pair.
         *
         * @param keyPair to be stored in the object
         * @param cipher  to be stored in the object
         * @return the new object
         */
        public static LicenseKeyPair from(final KeyPair keyPair, String cipher) {
            return new LicenseKeyPair(keyPair, cipher);
        }

        private static String algorithmPrefix(final String cipher) {
            if (cipher.contains("/")) {
                return cipher .substring(0, cipher.indexOf("/"));
            } else {
                return cipher;
            }
        }

        /**
         * Create a new key pair using the algorithm and the size for the key. The cipher transformation may optionally
         * contain the mode and the padding as {@code algorithm/mode/padding}. Note that the mode and the padding is
         * not needed for the key generation. Nevertheless these are also stored in the generated {@link LicenseKeyPair}
         * and will be used to sign/verify the license.
         * <p>
         * It is recommended to use the full cipher. Using only the algorithm will let the encryption provider to
         * selects its own favoutire mode and padding. In this case it may happen that the signing and the verification
         * happening in different environments may use different providers that are not compatible and an otherwise
         * completely perfect license will not verify.
         *
         * @param cipher the cipher string
         * @param size   the size of the key to generate
         * @return the new create {@link LicenseKeyPair}
         * @throws NoSuchAlgorithmException when the {@code cipher} specifies an algorithm that is not known by the
         *                                  encryption provider
         */
        public static LicenseKeyPair from(final String cipher, final int size) throws NoSuchAlgorithmException {
            final var algorithm = algorithmPrefix(cipher);
            KeyPairGenerator generator = KeyPairGenerator.getInstance(algorithm);
            generator.initialize(size);
            return new LicenseKeyPair(generator.genKeyPair(), cipher);
        }

        /**
         * Create a new {@link LicenseKeyPair} that contains only one of the keys, either the private or the public.
         * The key is provided in binary format as a byte array.
         * @param encoded the key encoded as byte array. This is the format that contains the algorithm at the start
         *                of the byte array as zero byte terminated string.
         * @param type either 1 for public key specification or 2 for private key specification. These commands are
         *             defined in the JDK in the {@link Modifier} class as {@link Modifier#PUBLIC} and
         *             {@link Modifier#PRIVATE}.
         * @return the newly created {@link LicenseKeyPair}
         * @throws NoSuchAlgorithmException if the algorithm in the encoded key is not implemented by the actual
         * encryption provider
         * @throws InvalidKeySpecException if the bytes of the key are garbage and cannot be decoded by the actual
         * encryption provider.
         */
        public static LicenseKeyPair from(byte[] encoded, int type) throws NoSuchAlgorithmException, InvalidKeySpecException {
            final String cipher = getAlgorithm(encoded);
            if (type == Modifier.PRIVATE)
                return from(null, getPrivateEncoded(encoded), cipher);
            else
                return from(getPublicEncoded(encoded), null, cipher);
        }

        /**
         * Create a new {@link LicenseKeyPair} from the public and the private key pairs.
         *
         * The method does not check that the encoded cipher specification at the start of the keys are the same or not.
         * They are supposed to be the same. There is also no check that the private and the public key are a pair.
         * @param privateEncoded the private key encoded including the cipher specification with a zero byte
         *                       terminated at the start of the byte array
         * @param publicEncoded the public key encoded including the cipher specification with a zero byte
         *                      terminated at the start of the byte array
         * @return the newly created {@link LicenseKeyPair}
         * @throws NoSuchAlgorithmException if the algorithm in the encoded key is not implemented by the actual
         * encryption provider
         * @throws InvalidKeySpecException if the bytes of the key are garbage and cannot be decoded by the actual
         * encryption provider.
         */
        public static LicenseKeyPair from(byte[] privateEncoded, byte[] publicEncoded) throws NoSuchAlgorithmException, InvalidKeySpecException {
            final String cipher = getAlgorithm(publicEncoded);
            return from(getPublicEncoded(publicEncoded), getPrivateEncoded(privateEncoded), cipher);
        }

        private static PublicKey getPublicEncoded(byte[] buffer) throws NoSuchAlgorithmException, InvalidKeySpecException {
            final var spec = new X509EncodedKeySpec(getEncoded(buffer));
            final var factory = KeyFactory.getInstance(getAlgorithm(buffer));
            return factory.generatePublic(spec);
        }

        private static PrivateKey getPrivateEncoded(byte[] buffer) throws NoSuchAlgorithmException, InvalidKeySpecException {
            final var spec = new PKCS8EncodedKeySpec(getEncoded(buffer));
            final var factory = KeyFactory.getInstance(getAlgorithm(buffer));
            return factory.generatePrivate(spec);
        }

        private static String getAlgorithm(byte[] buffer) {
            for (int i = 0; i < buffer.length; i++) {
                if (buffer[i] == 0x00) {
                    return new String(Arrays.copyOf(buffer, i), StandardCharsets.UTF_8);
                }
            }
            throw new IllegalArgumentException("key does not contain algorithm specification");
        }

        private static byte[] getEncoded(byte[] buffer) {
            for (int i = 0; i < buffer.length; i++) {
                if (buffer[i] == 0x00) {
                    return Arrays.copyOfRange(buffer, i + 1, buffer.length);
                }
            }
            throw new IllegalArgumentException("key does not contain algorithm specification");
        }
    }
}
