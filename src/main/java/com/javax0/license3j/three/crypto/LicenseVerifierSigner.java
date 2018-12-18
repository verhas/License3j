package com.javax0.license3j.three.crypto;

public abstract class LicenseVerifierSigner<T extends LicenseVerifierSigner> {
    protected static final String ALGORITHM = "RSA";
    protected static final String DIGEST = "SHA-512";
    protected String algorithm = ALGORITHM;
    protected String digest = DIGEST;

    public T withAlgorithm(String algorithm) {
        this.algorithm = algorithm;
        return (T)this;
    }

    public T withDigest(String digest) {
        this.digest = digest;
        return (T)this;
    }


}
