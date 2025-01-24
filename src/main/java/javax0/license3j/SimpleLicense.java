package javax0.license3j;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * This class represents a simple license generation and validation mechanism.
 * It uses a secret key and a value to generate a license string that can be verified later.
 */
public class SimpleLicense {

    private final String SECRET_KEY;
    private final String VALUE;
    private final String LICENSE_STRING;

    /**
     * Constructs a SimpleLicense instance using the given secret key and value.
     *
     * @param secretKey the secret key used to generate the license
     * @param value     the value to be licensed
     * @throws NoSuchAlgorithmException if the SHA-256 algorithm is not available
     */
    private SimpleLicense(String secretKey, String value) throws NoSuchAlgorithmException {
        SECRET_KEY = secretKey;
        VALUE = value;
        LICENSE_STRING = generate();
    }

    /**
     * Builder class for creating a {@link SimpleLicense} instance.
     */
    public static class Builder {

        /**
         * The secret key used for license generation.
         */
        private String SECRET_KEY;

        /**
         * Creates a {@link SimpleLicense} for the specified value.
         *
         * @param value the value to be licensed
         * @return a new {@link SimpleLicense} instance
         * @throws NoSuchAlgorithmException if the SHA-256 algorithm is not available
         */
        public SimpleLicense forValue(String value) throws NoSuchAlgorithmException {
            return new SimpleLicense(SECRET_KEY, value);
        }
    }

    /**
     * Initializes a {@link Builder} with the specified secret key.
     *
     * @param secret the secret key to use
     * @return a {@link Builder} instance
     */
    public static Builder withSecret(final String secret) {
        var it = new Builder();
        it.SECRET_KEY = secret;
        return it;
    }

    /**
     * Returns the license string.
     *
     * @return the license string
     */
    @Override
    public String toString() {
        return LICENSE_STRING;
    }

    /**
     * Generates the license string by hashing the concatenation of the value and the secret key.
     *
     * @return the generated license string
     * @throws NoSuchAlgorithmException if the SHA-256 algorithm is not available
     */
    private String generate() throws NoSuchAlgorithmException {
        var dataToEncode = VALUE + SECRET_KEY;
        var digest = MessageDigest.getInstance("SHA-256");
        var hash = digest.digest(dataToEncode.getBytes(StandardCharsets.UTF_8));
        return encode(hash);
    }

    /**
     * Validates the provided code against the generated license string.
     *
     * @param code the code to validate
     * @return {@code true} if the code matches the license string, {@code false} otherwise
     */
    public boolean isOK(String code) {
        return LICENSE_STRING.equals(code);
    }

    /**
     * Encodes the given hash into a formatted license string.
     *
     * @param hash the byte array to encode
     * @return the formatted license string
     */
    private static String encode(byte[] hash) {
        var asc = Base64.getUrlEncoder().withoutPadding().encodeToString(hash).replaceAll("[^A-Za-z0-9]", "").toUpperCase();
        return blockAndTruncate(asc);
    }

    /**
     * The number of character blocks in the formatted license string.
     */
    private static final int CHARBLOCKS = 4;

    /**
     * The number of characters in each block of the formatted license string.
     */
    private static final int BLOCKCHARS = 6;
    /**
     * Formats the encoded string into blocks and truncates it to the desired length.
     *
     * @param asc the encoded string
     * @return the formatted license string
     */
    private static String blockAndTruncate(String asc) {
        var lic = new StringBuilder(CHARBLOCKS*(BLOCKCHARS+1)-1);
        lic.append(asc, 0, CHARBLOCKS*BLOCKCHARS);
        for( int i = BLOCKCHARS ; i < CHARBLOCKS*(BLOCKCHARS+1)-1; i += BLOCKCHARS+1 ) {
            lic.insert(i,"-");
        }
        return lic.toString();
    }
}
