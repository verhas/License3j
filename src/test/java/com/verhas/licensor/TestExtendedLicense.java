package com.verhas.licensor;

import static com.verhas.licensor.TestExtendedLicense.NullTester.anyOf;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.InvalidParameterException;
import java.text.ParseException;
import java.util.Date;
import java.util.UUID;


import org.junit.Assert;
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

	static class NullTester {
		final Object[] objects;
		private NullTester(Object[] objects){
			this.objects = objects;
		}
		static NullTester anyOf(Object ...objects){
			return new NullTester(objects);
		}
		boolean isNull(){
			for (final Object object : objects) {
				if (object == null) {
					return true;
				}
			}
			return false;
		}
	}
	

	@BeforeClass
	public static void setupIsCorrect() throws InvalidParameterException {
		if (anyOf(unreachableUrl, githubUrl).isNull()) {
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

	private void mockHttpFetch(final ExtendedLicense lic, final int status, final IOException exception) throws IOException{
		HttpHandler handler = mock(HttpHandler.class);
		if( exception == null ){
			when(handler.getResponseCode((HttpURLConnection)any())).thenReturn(status);
		}else{
			when(handler.getResponseCode((HttpURLConnection)any())).thenThrow(exception);
		}
		when(handler.openConnection((URL)any())).thenReturn(mock(HttpURLConnection.class));
		lic.httpHandler = handler;
	}
	
	@Test
	public void licenseIsNotRevokedWhenHttpReturns200OK() throws IOException {
		final ExtendedLicense lic = new ExtendedLicense();
		mockHttpFetch(lic,200,null);
		lic.setRevocationURL(githubUrl);
		assertGracefullyNotRevoked(lic.isRevoked(true));
	}

	@Test
	public void licenseIsNotRevokedWhenUnreachableGraceful() throws IOException {
		final ExtendedLicense lic = new ExtendedLicense();
		mockHttpFetch(lic,0,new IOException());
		lic.setRevocationURL(unreachableUrl);
		Assert.assertFalse(lic.isRevoked(false));
	}

	@Test
	public void licenseIsNotRevokedWhenUnreachableStrict() throws IOException {
		final ExtendedLicense lic = new ExtendedLicense();
		mockHttpFetch(lic,0,new IOException());
		lic.setRevocationURL(unreachableUrl);
		Assert.assertTrue(lic.isRevoked(true));
	}

	@Test
	public void licenseIsNotRevokedWhenNoIdIsInUrlTemplateAndTheUrlIsOK()
			throws IOException {
		final ExtendedLicense lic = new ExtendedLicense();
		mockHttpFetch(lic,200,null);
		lic.setRevocationURL(githubUrlString);
		lic.setLicenseId(new UUID(0, 1L));
		assertGracefullyNotRevoked(lic.isRevoked());
	}

	@Test
	public void licenseIsNotRevokedWhenIdFileIsThere() throws ParseException, IOException {
		final ExtendedLicense lic = new ExtendedLicense();
		mockHttpFetch(lic,0,new IOException());
		lic.setRevocationURL(licenceUrlTemplate);
		lic.setLicenseId(new UUID(0, 1L));
		Assert.assertFalse(lic.isRevoked(false));
	}

	@Test
	public void licenseIsRevokedWhenIdFileIsNotThere() throws ParseException, IOException {
		final ExtendedLicense lic = new ExtendedLicense();
		mockHttpFetch(lic,404,null);
		lic.setRevocationURL(licenceUrlTemplate);
		lic.setLicenseId(new UUID(0, 2L));
		Assert.assertTrue(lic.isRevoked(false));
	}

	@Test
	public void licenseIsRevokedWhenUrlIsMalformed() throws ParseException, IOException {
		final ExtendedLicense lic = new ExtendedLicense();
		mockHttpFetch(lic,0,new IOException());
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
