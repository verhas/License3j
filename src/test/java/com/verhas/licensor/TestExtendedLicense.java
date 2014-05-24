package com.verhas.licensor;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.InvalidParameterException;
import java.text.ParseException;
import java.util.Date;
import java.util.UUID;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;

public class TestExtendedLicense {
	private static URL unreachableUrl;
	private static URL githubUrl;
	private static final String licenceUrlTemplate = "https://github.com/verhas/License3j/blob/master/src/test/resources/${licenseId}";
	private static final String githubUrlString = "https://github.com/verhas/License3j";
	static {
		try {
			unreachableUrl = new URL(
					"http://klasdsjdakdjalskdsalkdklaskdjalksjdlkasjdlaklsjda.com");
			githubUrl = new URL(githubUrlString);
		} catch (final MalformedURLException e) {
			unreachableUrl = null;
		}
	}

	private static boolean thereIsNull(final Object... objects) {
		for (final Object object : objects) {
			if (object == null) {
				return true;
			}
		}
		return false;
	}

	@BeforeClass
	public static void setupIsCorrect() throws InvalidParameterException {
		if (thereIsNull(unreachableUrl, githubUrl)) {
			throw new InvalidParameterException(
					"unreachableUrl was not properly initialized");
		}
	}

	private static boolean urlIsNotReachable(final URL url) {
		boolean reachable;
		try {
			final URLConnection connection = url.openConnection();
			connection.setUseCaches(false);
			connection.connect();
			reachable = true;
		} catch (final IOException exception) {
			reachable = false;
		}
		return !reachable;
	}

	private static boolean githubUrlIsNotReachable() {
		return urlIsNotReachable(githubUrl);
	}

	private static void assertGracefullyNotRevoked(
			final boolean seemsToBeRevoked) {
		if (seemsToBeRevoked) {
			Assert.assertTrue(githubUrlIsNotReachable());
		}
	}

	@Test
	public void licenseIsNotRevokedWhenHttpReturns200OK() throws IOException {
		final ExtendedLicense lic = new ExtendedLicense();
		lic.setRevocationURL(githubUrl);
		assertGracefullyNotRevoked(lic.isRevoked(true));
	}

	@Test
	public void licenseIsNotRevokedWhenUnreachableGraceful() throws IOException {
		final ExtendedLicense lic = new ExtendedLicense();
		lic.setRevocationURL(unreachableUrl);
		Assert.assertFalse(lic.isRevoked(false));
	}

	@Test
	public void licenseIsNotRevokedWhenUnreachableStrickt() throws IOException {
		final ExtendedLicense lic = new ExtendedLicense();
		lic.setRevocationURL(unreachableUrl);
		Assert.assertTrue(lic.isRevoked(true));
	}

	@Test
	public void licenseIsNotRevokedWhenNoIdIsInUrlTemplateAndTheUrlIsOK()
			throws IOException {
		final ExtendedLicense lic = new ExtendedLicense();
		lic.setRevocationURL(githubUrlString);
		lic.setLicenseId(new UUID(0, 1L));
		assertGracefullyNotRevoked(lic.isRevoked());
	}

	@Test
	public void licenseIsNotRevokedWhenIdFileIsThere() throws ParseException {
		final ExtendedLicense lic = new ExtendedLicense();
		lic.setRevocationURL(licenceUrlTemplate);
		lic.setLicenseId(new UUID(0, 1L));
		Assert.assertFalse(lic.isRevoked(false));
	}

	@Test
	public void licenseIsRevokedWhenIdFileIsNotThere() throws ParseException {
		final ExtendedLicense lic = new ExtendedLicense();
		lic.setRevocationURL(licenceUrlTemplate);
		lic.setLicenseId(new UUID(0, 2L));
		Assert.assertTrue(lic.isRevoked(false));
	}

	@Test
	public void licenseIsRevokedWhenUrlIsMalformed() throws ParseException {
		final ExtendedLicense lic = new ExtendedLicense();
		lic.setRevocationURL("ftp://index.hu/");
		Assert.assertTrue(lic.isRevoked(true));
	}

	@Test
	public void pastExpiryTimeReportsExpired() throws ParseException {
		final ExtendedLicense lic = new ExtendedLicense();
		lic.setExpiry(new Date(new Date().getTime() - 24 * 60 * 60 * 1000));
		Assert.assertTrue(lic.isExpired());
	}

	@Test
	public void futureExpiryTimeReportsNonExpired() throws ParseException {
		final ExtendedLicense lic = new ExtendedLicense();
		lic.setExpiry(new Date(new Date().getTime() + 24 * 60 * 60 * 1000));
		Assert.assertFalse(lic.isExpired());
	}

	@Test
	public void badlyFormattedExpiryDateReportsExpiredLicense()
			throws ParseException {
		final ExtendedLicense lic = new ExtendedLicense();
		lic.setFeature("expiryDate", "not a valid date");
		Assert.assertTrue(lic.isExpired());
	}

	@Test
	public void uuidGenerationResultsNonNullUuid() throws ParseException {
		final ExtendedLicense lic = new ExtendedLicense();
		lic.generateLicenseId();
		Assert.assertNotNull(lic.getLicenseId());
	}

	@Test
	public void settingIntFeaturesGetsTheValueBackAsInteger()
			throws ParseException, MalformedURLException,
			InstantiationException, IllegalAccessException {
		final int n = 10;
		final ExtendedLicense lic = new ExtendedLicense();
		for (int i = 0; i < n; i++) {
			lic.setFeature("testInteger" + i, i);
		}
		for (int i = 0; i < n; i++) {
			Assert.assertEquals((Integer) i,
					lic.getFeature("testInteger" + i, Integer.class));
		}
	}

	@Test
	public void settingUrlFeatureReturnsTheUrl() throws ParseException,
			MalformedURLException, InstantiationException,
			IllegalAccessException {
		final ExtendedLicense lic = new ExtendedLicense();
		lic.setFeature("testURL", new URL("http://index.hu"));
		Assert.assertEquals(new URL("http://index.hu"),
				lic.getFeature("testURL", URL.class));
	}

	@Test(expected = IllegalArgumentException.class)
	public void gettingInvalidFeatureTypeThrowsException()
			throws MalformedURLException, InstantiationException,
			IllegalAccessException, ParseException {
		final ExtendedLicense lic = new ExtendedLicense();
		lic.getFeature("testURL", Object.class);
	}

	@Test
	public void notSettingRevocationUrlResultNullRevocationUrl()
			throws MalformedURLException {
		final ExtendedLicense lic = new ExtendedLicense();
		Assert.assertNull(lic.getRevocationURL());
	}

	@Test
	public void licenseWithNoRevocationUrlIsNotRevoked()
			throws MalformedURLException {
		final ExtendedLicense lic = new ExtendedLicense();
		Assert.assertFalse(lic.isRevoked(true));
	}
}
