package com.javax0.license3j.three.crypto;

import java.lang.reflect.Modifier;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class LicenseKeyPair {
    private final KeyPair pair;

    private LicenseKeyPair(KeyPair pair) {
        this.pair = pair;
    }

    public KeyPair getPair() {
        return pair;
    }

    public byte[] getPrivate() {
        keyNotNull(pair.getPrivate());
        return new PKCS8EncodedKeySpec(pair.getPrivate().getEncoded()).getEncoded();
    }

    private void keyNotNull(Key key) {
        if (key == null) {
            throw new IllegalArgumentException("KeyPair does not have the key");
        }
    }

    public byte[] getPublic() {
        keyNotNull(pair.getPublic());
        return new X509EncodedKeySpec(pair.getPublic().getEncoded()).getEncoded();
    }

    public static class Create {
        public static LicenseKeyPair from(final PublicKey publicKey, PrivateKey privateKey) {
            return new LicenseKeyPair(new KeyPair(publicKey, privateKey));
        }

        public static LicenseKeyPair from(final KeyPair keyPair) {
            return new LicenseKeyPair(keyPair);
        }

        public static LicenseKeyPair from(final String algorithm, final int size) throws NoSuchAlgorithmException {
            KeyPairGenerator generator = KeyPairGenerator.getInstance(algorithm);
            generator.initialize(size);
            return new LicenseKeyPair(generator.genKeyPair());
        }

        public static LicenseKeyPair from(byte[] encoded, int type) throws NoSuchAlgorithmException, InvalidKeySpecException {
            if (type == Modifier.PRIVATE)
                return from(null, getPrivateEncoded(encoded));
            else
                return from(getPublicEncoded(encoded), null);
        }

        public static LicenseKeyPair from(byte[] privateEncoded, byte[] publicEncoded) throws NoSuchAlgorithmException, InvalidKeySpecException {
            return from(getPublicEncoded(publicEncoded), getPrivateEncoded(privateEncoded));
        }

        private static PublicKey getPublicEncoded(byte[] publicEncoded) throws NoSuchAlgorithmException, InvalidKeySpecException {
            final var spec = new X509EncodedKeySpec(publicEncoded);
            final var factory = KeyFactory.getInstance(spec.getAlgorithm());
            return factory.generatePublic(spec);
        }

        private static PrivateKey getPrivateEncoded(byte[] privateEncoded) throws NoSuchAlgorithmException, InvalidKeySpecException {
            final var spec = new PKCS8EncodedKeySpec(privateEncoded);
            final var factory = KeyFactory.getInstance(spec.getAlgorithm());
            return factory.generatePrivate(spec);
        }
    }
}
