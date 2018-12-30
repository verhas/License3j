package javax0.license3j.three.crypto;

import javax0.license3j.three.Feature;
import javax0.license3j.three.License;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class LicenseSignerTest {

    @Test
    public void testSignature() throws NoSuchAlgorithmException, IllegalBlockSizeException, InvalidKeyException, BadPaddingException, NoSuchPaddingException {
        final var keyPair = LicenseKeyPair.Create.from("RSA", 2048);
        final var license = new License();
        license.add(Feature.Create.stringFeature("owner","Peter Verhas"));
        license.sign(keyPair.getPair().getPrivate(),"SHA-512");
        Assertions.assertTrue(license.isOK(keyPair.getPair().getPublic()));
        license.getSignature()[0] = 0;
        Assertions.assertFalse(license.isOK(keyPair.getPair().getPublic()));
    }
}
