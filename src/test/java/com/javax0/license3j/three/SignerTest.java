package com.javax0.license3j.three;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;

public class SignerTest {

    @Test
    public void testSignature() throws NoSuchAlgorithmException, IllegalBlockSizeException, InvalidKeyException, BadPaddingException, NoSuchPaddingException {
        KeyPair keyPair = Signer.buildKeyPair("RSA", 2048);
        License license = new License();
        license.add(Feature.Create.stringFeature("owner","Peter Verhas"));
        var serializedLicense = license.serialized();
        var signer = new Signer(keyPair);
        var signature = signer.sign(serializedLicense);
        Assertions.assertTrue(signer.verify(serializedLicense,signature));
        signature[0] = 0;
        Assertions.assertFalse(signer.verify(serializedLicense,signature));
    }
}
