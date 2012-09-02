import java.io.File;

import junit.framework.Assert;

import org.bouncycastle.openpgp.PGPException;
import org.junit.Test;

public class TestLicense3j {
	@Test
	public void testnoArgs() throws Exception {
		License3j.main(new String[] {});
		License3j.main(null);
	}

	@Test
	public void testNoCommand() throws Exception {
		License3j.main(new String[] {
				"--license-file=src/test/resources/license-plain.txt",
				"--keyring-file=src/test/resources/secring.pgp",
				"--key=Peter Verhas (licensor test key) <peter@verhas.com>",
				"--password=alma",
				"--output=target/license-encoded-from-commandline.txt" });
	}

	@Test
	public void testEncodeAndDecode() throws Exception {
		final String encodedFromCommandLine = "target/license-encoded-from-commandline.txt";
		final String decodedFromCommandLine = "target/license-decoded-from-commandline.txt";
		License3j.main(new String[] { "encode",
				"--license-file=src/test/resources/license-plain.txt",
				"--keyring-file=src/test/resources/secring.gpg",
				"--key=Peter Verhas (licensor test key) <peter@verhas.com>",
				"--password=alma", "--output=" + encodedFromCommandLine });
		File encodedOutputFile = new File(encodedFromCommandLine);
		Assert.assertTrue(encodedOutputFile.isFile());
		License3j.main(new String[] { "decode",
				"--license-file=" + encodedFromCommandLine,
				"--keyring-file=src/test/resources/pubring.gpg",
				"--output=" + decodedFromCommandLine });
		License3j.main(new String[] { "decode",
				"--license-file=" + encodedFromCommandLine,
				"--keyring-file=src/test/resources/pubring.gpg",
				"--charset=utf-8", "--output=" + decodedFromCommandLine });
		File decodedOutputFile = new File(decodedFromCommandLine);
		Assert.assertTrue(decodedOutputFile.isFile());
		encodedOutputFile.delete();
		decodedOutputFile.delete();
	}

	@Test(expected=PGPException.class)
	public void testDecodeFail() throws Exception {
		License3j.main(new String[] { "decode",
				"--license-file=src/test/resources/license-plain.txt",
				"--keyring-file=src/test/resources/pubring.gpg",
				"--output=justAnythingDecodingFailsAnyway.txt" });
	}
}
