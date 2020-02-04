package javax0.license3j.crypto;

import javax0.license3j.Feature;
import javax0.license3j.License;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class LicenseSignerTest {

    @Test
    @DisplayName("License is encoded and verified with keys generated in the test on the fly")
    public void testSignature() throws NoSuchAlgorithmException, IllegalBlockSizeException, InvalidKeyException, BadPaddingException, NoSuchPaddingException {
        final var keyPair = LicenseKeyPair.Create.from("RSA", 2048);
        final var license = new License();
        license.add(Feature.Create.stringFeature("owner", "Peter Verhas"));
        license.sign(keyPair.getPair().getPrivate(), "SHA-512");
        Assertions.assertTrue(license.isOK(keyPair.getPair().getPublic()));
        license.getSignature()[0] = (byte) ~license.getSignature()[0];
        Assertions.assertFalse(license.isOK(keyPair.getPair().getPublic()));
    }

    @Test
    @DisplayName("License is encoded and verified properly when the keys are generated specifying the full cipher transformation string and not only the algorithm")
    public void testSignatureWithFullcipher() throws NoSuchAlgorithmException, IllegalBlockSizeException, InvalidKeyException, BadPaddingException, NoSuchPaddingException {
        final var keyPair = LicenseKeyPair.Create.from("RSA/ECB/PKCS1Padding", 2048);
        final var license = new License();
        license.add(Feature.Create.stringFeature("owner", "Peter Verhas"));
        license.sign(keyPair.getPair().getPrivate(), "SHA-512");
        Assertions.assertTrue(license.isOK(keyPair.getPair().getPublic()));
        license.getSignature()[0] = (byte) ~license.getSignature()[0];
        Assertions.assertFalse(license.isOK(keyPair.getPair().getPublic()));
    }

    @Test
    @DisplayName("The key contain at the start null terminated the full cipher transformation string not only the algorithm")
    public void testKeyContainsFullcipher() throws NoSuchAlgorithmException {
        final var keyPair = LicenseKeyPair.Create.from("RSA/ECB/PKCS1Padding", 2048);
        final var pubFull = new String(keyPair.getPublic());
        final var pub = pubFull.substring(0, pubFull.indexOf(0));
        Assertions.assertEquals("RSA/ECB/PKCS1Padding", pub);
        final var privFull = new String(keyPair.getPublic());
        final var priv = privFull.substring(0, privFull.indexOf(0));
        Assertions.assertEquals("RSA/ECB/PKCS1Padding", priv);
    }
}
