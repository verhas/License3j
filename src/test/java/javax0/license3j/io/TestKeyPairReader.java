package javax0.license3j.io;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Objects;

public class TestKeyPairReader {

    private KeyPairReader getSut(final String publicKeyBinary) throws FileNotFoundException {
        final URL url = getClass().getResource(publicKeyBinary);
        Objects.requireNonNull(url, () -> String.format("The resource %s was not found", publicKeyBinary));
        return new KeyPairReader(URLDecoder.decode(url.getFile(), StandardCharsets.UTF_8));
    }

    @Test
    @DisplayName("Can read public key from BINARY file")
    void canReadPublicKeyFromBinaryFile() throws IOException, InvalidKeySpecException, NoSuchAlgorithmException {
        try (final var sut = getSut(TestKeyPairWriter.PUBLIC_KEY_BINARY)) {
            final var key = sut.readPublic();
            Assertions.assertEquals("RSA", key.cipher());
        }
    }

    @Test
    @DisplayName("Can read private key from BINARY file")
    void canReadPrivateKeyFromBinaryFile() throws IOException, InvalidKeySpecException, NoSuchAlgorithmException {
        try (final var sut = getSut(TestKeyPairWriter.PRIVATE_KEY_BINARY)) {
            final var key = sut.readPrivate();
            Assertions.assertEquals("RSA", key.cipher());
        }
    }

    @Test
    @DisplayName("Can read public key from BASE64 file")
    void canReadPublicKeyFromBase64File() throws IOException, InvalidKeySpecException, NoSuchAlgorithmException {
        try (final var sut = getSut(TestKeyPairWriter.PUBLIC_KEY_BASE64)) {
            final var key = sut.readPublic(IOFormat.BASE64);
            Assertions.assertEquals("RSA", key.cipher());
        }
    }

    @Test
    @DisplayName("Can read private key from BASE64 file")
    void canReadPrivateKeyFromBase64File() throws IOException, InvalidKeySpecException, NoSuchAlgorithmException {
        try (final var sut = getSut(TestKeyPairWriter.PRIVATE_KEY_BASE64)) {
            final var key = sut.readPrivate(IOFormat.BASE64);
            Assertions.assertEquals("RSA", key.cipher());
        }
    }

    @Test
    @DisplayName("Throws exception when trying to read key file using STRING format")
    void throwsUpOnStringFormat() throws IOException {
        try (final var sut = getSut(TestKeyPairWriter.PRIVATE_KEY_BASE64)) {
            Assertions.assertThrows(IllegalArgumentException.class, () -> sut.readPrivate(IOFormat.STRING));
        }
    }
}
