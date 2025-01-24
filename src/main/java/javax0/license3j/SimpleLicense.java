package javax0.license3j;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class SimpleLicense {

    private final String SECRET_KEY;
    private final String VALUE;
    private final String LICENSE_STRING;

    private SimpleLicense(String secretKey, String value) throws NoSuchAlgorithmException {
        SECRET_KEY = secretKey;
        VALUE = value;
        LICENSE_STRING = generate();
    }

    public static class Builder {
        private String SECRET_KEY;

        public SimpleLicense forValue(String value) throws NoSuchAlgorithmException {
            return new SimpleLicense(SECRET_KEY, value);
        }
    }

    public static Builder withSecret(final String secret) {
        var it = new Builder();
        it.SECRET_KEY = secret;
        return it;
    }

    @Override
    public String toString() {
        return LICENSE_STRING;
    }


    private String generate() throws NoSuchAlgorithmException {
        var dataToEncode = VALUE + SECRET_KEY;
        var digest = MessageDigest.getInstance("SHA-256");
        var hash = digest.digest(dataToEncode.getBytes(StandardCharsets.UTF_8));
        return encode(hash);
    }

    public boolean isOK(String code) {
        return LICENSE_STRING.equals(code);
    }

    private static String encode(byte[] hash) {
        var asc = Base64.getUrlEncoder().withoutPadding().encodeToString(hash).replaceAll("[^A-Za-z0-9]", "").toUpperCase();
        return blockAndTruncate(asc);
    }

    private static final int CHARBLOCKS = 4;
    private static final int BLOCKCHARS = 6;
    private static String blockAndTruncate(String asc) {
        var lic = new StringBuilder(CHARBLOCKS*(BLOCKCHARS+1)-1);
        lic.append(asc, 0, CHARBLOCKS*BLOCKCHARS);
        for( int i = BLOCKCHARS ; i < CHARBLOCKS*(BLOCKCHARS+1)-1; i += BLOCKCHARS+1 ) {
            lic.insert(i,"-");
        }
        return lic.toString();
    }
}
