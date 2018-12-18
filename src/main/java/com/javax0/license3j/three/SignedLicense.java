package com.javax0.license3j.three;

import com.javax0.license3j.three.crypto.LicenseVerifier;

import java.security.PublicKey;

public class SignedLicense extends License {

    /**
     * Get the signature from the license.
     * @return the signature
     */
    public byte[] getSignature() {
        return signature;
    }

    private final byte[] signature;

    public SignedLicense(License license, byte[] signature) {
        super(license);
        this.signature = signature;
    }

    /**
     * @return the unsigned version of the license.
     */
    public License unsigned(){
        return new License(this);
    }

    public boolean isOK(PublicKey key){
        final var verifier = new LicenseVerifier(key,this);
        return verifier.verify();
    }
}
