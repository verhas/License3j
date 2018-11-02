package com.javax0.license3j;

import com.javax0.license3j.filecompare.FilesAre;
import org.bouncycastle.openpgp.PGPException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;

public class TestLicense3j {
    private static final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    private static final PrintStream observableErrorOutput = new PrintStream(
            baos);

    private String errorOutput() {
        observableErrorOutput.flush();
        return baos.toString();
    }

    @Test
    public void noArgumentPrintsUsage() throws Exception {
        redirectError();
        License3j.main(new String[]{});
        Assertions.assertTrue(errorOutput().contains("Usage:"));
    }

    @Test
    public void nullArgumentPrintsUsage() throws Exception {
        redirectError();
        License3j.main(null);
        Assertions.assertTrue(errorOutput().contains("Usage:"));
    }

    @Test
    public void testNoCommand() throws Exception {
        redirectError();
        License3j.main(new String[]{
                "--license-file=src/test/resources/license-plain.txt",
                "--keyring-file=src/test/resources/secring.pgp",
                "--key=Peter Verhas (licensor test key) <peter@verhas.com>",
                "--password=alma",
                "--output=target/license-encoded-from-commandline.txt"});
        Assertions.assertTrue(errorOutput().contains("Usage:"));
    }

    private void redirectError() {
        License3j.errorOutput = observableErrorOutput;
        baos.reset();
    }

    @Test
    public void testEncodeAndDecode() throws Exception {
        final String plain = "src/test/resources/license-plain.txt";
        final String decodedReference = "src/test/resources/license-decoded.txt";
        final String encoded = "target/license-encoded-from-commandline.txt";
        final String decoded = "target/license-decoded-from-commandline.txt";
        License3j.main(new String[]{"encode", "--license-file=" + plain,
                "--keyring-file=src/test/resources/secring.gpg",
                "--key=Peter Verhas (licensor test key) <peter@verhas.com>",
                "--password=alma", "--output=" + encoded});
        License3j.main(new String[]{"decode", "--license-file=" + encoded,
                "--keyring-file=src/test/resources/pubring.gpg",
                "--output=" + decoded});
        Assertions.assertTrue(FilesAre.theSame(decoded, decodedReference));
        new File(encoded).delete();
        new File(decoded).delete();
    }

    @Test
    public void testDecodeFail() throws Exception {
        Assertions.assertThrows(PGPException.class, () ->
                License3j.main(new String[]{"decode",
                        "--license-file=src/test/resources/license-plain.txt",
                        "--keyring-file=src/test/resources/pubring.gpg",
                        "--output=justAnythingDecodingFailsAnyway.txt"}));
    }
}
