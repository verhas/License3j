import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.lang.reflect.Field;


import org.bouncycastle.openpgp.PGPException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.verhas.filecompare.FilesAre;

public class TestLicense3j {
	private static final ByteArrayOutputStream baos = new ByteArrayOutputStream();
	private static final PrintStream observableErrorOutput = new PrintStream(
			baos);

	@Before
	public void resetErrorOutput() {
		baos.reset();
	}

	@BeforeClass
	public static void redirectLicense3jErrorOutput() throws SecurityException,
			NoSuchFieldException, IllegalArgumentException,
			IllegalAccessException {
		Field errorOutputField = License3j.class
				.getDeclaredField("errorOutput");
		errorOutputField.setAccessible(true);
		errorOutputField.set(null, observableErrorOutput);
	}

	private String errorOutput() {
		observableErrorOutput.flush();
		return baos.toString();
	}

	@Test
	public void noArgumentPrintsUsage() throws Exception {
		License3j.main(new String[] {});
		Assert.assertTrue(errorOutput().contains("Usage:"));
	}

	@Test
	public void nullArgumentPrintsUsage() throws Exception {
		License3j.main(null);
		Assert.assertTrue(errorOutput().contains("Usage:"));
	}

	@Test
	public void testNoCommand() throws Exception {
		License3j.main(new String[] {
				"--license-file=src/test/resources/license-plain.txt",
				"--keyring-file=src/test/resources/secring.pgp",
				"--key=Peter Verhas (licensor test key) <peter@verhas.com>",
				"--password=alma",
				"--output=target/license-encoded-from-commandline.txt" });
		Assert.assertTrue(errorOutput().contains("Usage:"));
	}

	@Test
	public void testEncodeAndDecode() throws Exception {
		final String plain = "src/test/resources/license-plain.txt";
		final String decodedReference = "src/test/resources/license-decoded.txt";
		final String encoded = "target/license-encoded-from-commandline.txt";
		final String decoded = "target/license-decoded-from-commandline.txt";
		License3j.main(new String[] { "encode", "--license-file=" + plain,
				"--keyring-file=src/test/resources/secring.gpg",
				"--key=Peter Verhas (licensor test key) <peter@verhas.com>",
				"--password=alma", "--output=" + encoded });
		License3j.main(new String[] { "decode", "--license-file=" + encoded,
				"--keyring-file=src/test/resources/pubring.gpg",
				"--output=" + decoded });
		Assert.assertTrue(FilesAre.theSame(decoded, decodedReference));
		new File(encoded).delete();
		new File(decoded).delete();
	}

	@Test(expected = PGPException.class)
	public void testDecodeFail() throws Exception {
		License3j.main(new String[] { "decode",
				"--license-file=src/test/resources/license-plain.txt",
				"--keyring-file=src/test/resources/pubring.gpg",
				"--output=justAnythingDecodingFailsAnyway.txt" });
		System.err.print(errorOutput());
	}
}
