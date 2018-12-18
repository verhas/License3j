package com.javax0.license3j.three.crypto;


import com.javax0.license3j.three.License;
import com.javax0.license3j.three.SignedLicense;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.*;

public class LicenseSigner extends LicenseVerifierSigner<LicenseVerifier> {
    final PrivateKey key;
    final License license;

    public LicenseSigner(PrivateKey key, License license) {
        this.key = key;
        this.license = license;
    }

    public SignedLicense sign() throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        final var digester = MessageDigest.getInstance(digest);
        final var ser = license.serialized();
        final var digestValue = digester.digest(ser);
        final var cipher = Cipher.getInstance(algorithm);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        final var signature = cipher.doFinal(digestValue);
        return new SignedLicense(license, signature);
    }
}
