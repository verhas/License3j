package javax0.license3j.io;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class TestLicenseReader {


    private LicenseReader getSut(final String fileText, int limit) throws FileNotFoundException {
        final URL url = getClass().getResource(fileText);
        Objects.requireNonNull(url, () -> String.format("file %s not found", fileText));
        if (limit < 0) {
            return new LicenseReader(URLDecoder.decode(url.getFile(), StandardCharsets.UTF_8));
        } else {
            return new LicenseReader(URLDecoder.decode(url.getFile(), StandardCharsets.UTF_8), limit);
        }
    }

    @Test
    @DisplayName("Can read license from text file")
    void canReadLicenseFromTextFile() throws IOException {
        try (final var sut = getSut(TestLicenseWriter.FILE_TEXT, -1)) {
            final var lic = sut.read(IOFormat.STRING);
            Assertions.assertEquals("string feature", lic.get("simple").getString());
        }
    }

    @Test
    @DisplayName("Can read license from binary file")
    void canReadLicenseFromBinaryFile() throws IOException {
        try (final var sut = getSut(TestLicenseWriter.FILE_BINARY, -1)) {
            final var lic = sut.read(IOFormat.BINARY);
            Assertions.assertEquals("string feature", lic.get("simple").getString());
        }
    }

    @Test
    @DisplayName("Can read license from binary file checking")
    void canReadLicenseFromBinaryFileChecking() throws IOException {
        try (final var sut = getSut(TestLicenseWriter.FILE_BINARY, -1)) {
            final var lic = sut.readChecking(IOFormat.BINARY);
            Assertions.assertEquals("string feature", lic.get("simple").getString());
        }
    }

    @Test
    @DisplayName("Can read license from binary file using default read()")
    void canReadDefaultLicenseFromBinaryFile() throws IOException {
        try (final var sut = getSut(TestLicenseWriter.FILE_BINARY, -1)) {
            final var lic = sut.read();
            Assertions.assertEquals("string feature", lic.get("simple").getString());
        }
    }

    @Test
    @DisplayName("Can read license from base64 file")
    void canReadLicenseFromBase64File() throws IOException {
        try (final var sut = getSut(TestLicenseWriter.FILE_BASE64, -1)) {
            final var lic = sut.read(IOFormat.BASE64);
            Assertions.assertEquals("string feature", lic.get("simple").getString());
        }
    }

    @Test
    @DisplayName("Can read license from base64 file checking")
    void canReadLicenseFromBase64FileChecking() throws IOException {
        try (final var sut = getSut(TestLicenseWriter.FILE_BASE64, -1)) {
            final var lic = sut.readChecking(IOFormat.BASE64);
            Assertions.assertEquals("string feature", lic.get("simple").getString());
        }
    }

    @Test
    @DisplayName("Avoids reading too long file")
    void avoidTooLongFile() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> getSut(TestLicenseWriter.FILE_TEXT, 1));
    }
}
