package com.javax0.license3j.three.crypto;

import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;

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
        Key key = pair.getPrivate();
        return getKeyBytes(key);
    }

    private byte[] getKeyBytes(Key key) {
        final var algorithm = key.getAlgorithm().getBytes(StandardCharsets.UTF_8);
        final var len =  algorithm.length + 1 + key.getEncoded().length;
        final var buffer = new byte[len];
        System.arraycopy(algorithm,0,buffer,0,algorithm.length);
        buffer[algorithm.length] = 0x00;
        System.arraycopy(key.getEncoded(),0,buffer,algorithm.length+1,key.getEncoded().length);
        return buffer;
    }

    private void keyNotNull(Key key) {
        if (key == null) {
            throw new IllegalArgumentException("KeyPair does not have the key");
        }
    }

    public byte[] getPublic() {
        keyNotNull(pair.getPublic());
        Key key = pair.getPublic();
        return getKeyBytes(key);
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

        private static PublicKey getPublicEncoded(byte[] buffer) throws NoSuchAlgorithmException, InvalidKeySpecException {
            final var spec = new X509EncodedKeySpec(getEncoded(buffer));
            final var factory = KeyFactory.getInstance(getAlgorithm(buffer));
            return factory.generatePublic(spec);
        }

        private static PrivateKey getPrivateEncoded(byte[] buffer) throws NoSuchAlgorithmException, InvalidKeySpecException {
            final var spec = new PKCS8EncodedKeySpec(getEncoded(buffer));
            final var factory = KeyFactory.getInstance(getAlgorithm(buffer));
            return factory.generatePrivate(spec);
        }

        private static String getAlgorithm(byte[] buffer){
            for(int i = 0 ; i < buffer.length ; i ++){
                if( buffer[i] == 0x00){
                    return new String(Arrays.copyOf(buffer,i),StandardCharsets.UTF_8);
                }
            }
            throw new IllegalArgumentException("key does not contain algorithm specification");
        }
        private static byte[] getEncoded(byte[] buffer){
            for(int i = 0 ; i < buffer.length ; i ++){
                if( buffer[i] == 0x00){
                    return Arrays.copyOfRange(buffer,i+1,buffer.length);
                }
            }
            throw new IllegalArgumentException("key does not contain algorithm specification");
        }
    }
}
