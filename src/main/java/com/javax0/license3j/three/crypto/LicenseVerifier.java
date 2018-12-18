package com.javax0.license3j.three.crypto;

import com.javax0.license3j.three.License;
import com.javax0.license3j.three.SignedLicense;

import javax.crypto.Cipher;
import java.security.*;
import java.util.Arrays;

public class LicenseVerifier extends LicenseVerifierSigner<LicenseVerifier> {
    final PublicKey key;
    final SignedLicense license;

    public LicenseVerifier(PublicKey key, SignedLicense license) {
        this.key = key;
        this.license = license;
    }

    public boolean verify() {
        try {
            final var digester = MessageDigest.getInstance(digest);
            final var ser = license.serialized();
            final var digestValue = digester.digest(ser);
            final var cipher = Cipher.getInstance(algorithm);
            cipher.init(Cipher.DECRYPT_MODE, key);
            final var sigDigest = cipher.doFinal(license.getSignature());
            return Arrays.equals(digestValue, sigDigest);
        } catch (Exception e) {
            return false;
        }
    }
}
