package com.javax0.license3j.three;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;

public class Signer {
    private static final String ALGORITHM = "RSA";
    private static final String DIGEST = "SHA-512";
    KeyPair keyPair;

    public Signer(KeyPair keyPair) {
        this.keyPair = keyPair;
    }

    public byte[] sign(byte[] buffer) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        final var digester = MessageDigest.getInstance(DIGEST);
        final var digest = digester.digest(buffer);
        final var cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, keyPair.getPrivate());
        final var signature = cipher.doFinal(digest);
        return signature;
    }

    public boolean verify(byte[] buffer, byte[] signature) {
        try {
            final var digester = MessageDigest.getInstance(DIGEST);
            final var digest = digester.digest(buffer);

            final var cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, keyPair.getPublic());
            final var sigDigest = cipher.doFinal(signature);
            return Arrays.equals(digest, sigDigest);
        } catch (Exception e) {
            return false;
        }
    }

    public static KeyPair buildKeyPair(String algorithm, final int keySize) throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(algorithm);
        keyPairGenerator.initialize(keySize);
        return keyPairGenerator.genKeyPair();
    }

    public byte[] getPrivate() {
        return new PKCS8EncodedKeySpec(keyPair.getPrivate().getEncoded()).getEncoded();
    }

    public byte[] getPublic() {
        return new X509EncodedKeySpec(keyPair.getPublic().getEncoded()).getEncoded();
    }


    public void setPublic(byte[] encoded) throws NoSuchAlgorithmException, InvalidKeySpecException {
        KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
        X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(encoded);
        PublicKey key = keyFactory.generatePublic(publicKeySpec);
        keyPair = new KeyPair(key, keyPair.getPrivate());
    }

    public void setPrivate(byte[] encoded) throws NoSuchAlgorithmException, InvalidKeySpecException {
        KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
        PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(encoded);
        PrivateKey key = keyFactory.generatePrivate(privateKeySpec);
        keyPair = new KeyPair(keyPair.getPublic(), key);
    }


}
