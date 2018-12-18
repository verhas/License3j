package com.javax0.license3j.three.crypto;

import com.javax0.license3j.three.Feature;
import com.javax0.license3j.three.License;
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
        final var signer = new LicenseSigner(keyPair.getPair().getPrivate(),license);
        final var signedLicense = signer.sign();
        Assertions.assertTrue(signedLicense.isOK(keyPair.getPair().getPublic()));
        signedLicense.getSignature()[0] = 0;
        Assertions.assertFalse(signedLicense.isOK(keyPair.getPair().getPublic()));
    }
}
