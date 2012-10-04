package com.verhas.licensor;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.util.Date;
import java.util.UUID;

import junit.framework.Assert;

import org.junit.Test;

public class TestExtendedLicense {

	@Test
	public void testFetchUrlIsOK() throws IOException {
		ExtendedLicense lic = new ExtendedLicense();
		lic.setRevocationURL(new URL("http://index.hu"));
		Assert.assertFalse(lic.isRevoked(false));
	}

	@Test
	public void testFetchUrlIsOKWithLicenseId() throws IOException {
		ExtendedLicense lic = new ExtendedLicense();
		lic.setRevocationURL("http://index.hu");
		lic.setLicenseId(new UUID(0, 1L));
		Assert.assertFalse(lic.isRevoked());
	}

	@Test
	public void testFetchUrlFAILWithLicenseId() throws ParseException {
		ExtendedLicense lic = new ExtendedLicense();
		lic.setRevocationURL("http://index.hu/${licenseId}");
		lic.setLicenseId(new UUID(0, 1L));
		Assert.assertTrue(lic.isRevoked(false));
	}

	@Test
	public void testFetchUrlFAILNonHttp() throws ParseException {
		ExtendedLicense lic = new ExtendedLicense();
		lic.setRevocationURL("ftp://index.hu/");
		Assert.assertTrue(lic.isRevoked(true));
	}

	@Test
	public void testFetchUrlFAILNoConnection() throws ParseException {
		ExtendedLicense lic = new ExtendedLicense();
		lic.setRevocationURL("http://thereisnosuchdomain.hu/");
		Assert.assertTrue(lic.isRevoked(true));
	}

	@Test
	public void testExpiredLicense() throws ParseException {
		ExtendedLicense lic = new ExtendedLicense();
		lic.setExpiry(new Date(new Date().getTime() - 24 * 60 * 60 * 1000));
		Assert.assertTrue(lic.isExpired());
	}

	@Test
	public void testNonExpiredLicense() throws ParseException {
		ExtendedLicense lic = new ExtendedLicense();
		lic.setExpiry(new Date(new Date().getTime() + 24 * 60 * 60 * 1000));
		Assert.assertFalse(lic.isExpired());
	}

	@Test
	public void testBadFormatExpiredLicense() throws ParseException {
		ExtendedLicense lic = new ExtendedLicense();
		lic.setFeature("expiryDate", "not a valid date");
		Assert.assertTrue(lic.isExpired());
	}

	@Test
	public void testUUIDGeneration() throws ParseException {
		ExtendedLicense lic = new ExtendedLicense();
		lic.generateLicenseId();
		Assert.assertNotNull(lic.getLicenseId());
	}

	@Test
	public void testIntegerFeature() throws ParseException,
			MalformedURLException, InstantiationException,
			IllegalAccessException {
		ExtendedLicense lic = new ExtendedLicense();
		for (int i = 0; i < 100000; i++) {
			lic.setFeature("testInteger" + i, i);
		}
		for (int i = 0; i < 100000; i++) {
			Assert.assertEquals((Integer) i,
					lic.getFeature("testInteger" + i, Integer.class));
		}
	}

	@Test
	public void testURLFeature() throws ParseException, MalformedURLException,
			InstantiationException, IllegalAccessException {
		ExtendedLicense lic = new ExtendedLicense();
		lic.setFeature("testURL", new URL("http://index.hu"));
		Assert.assertEquals(new URL("http://index.hu"),
				lic.getFeature("testURL", URL.class));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testGetFeatureIllegal() throws MalformedURLException,
			InstantiationException, IllegalAccessException, ParseException {
		ExtendedLicense lic = new ExtendedLicense();
		lic.getFeature("testURL", Object.class);
	}

	@Test
	public void testNullRevocationUrl() throws MalformedURLException {
		ExtendedLicense lic = new ExtendedLicense();
		Assert.assertNull(lic.getRevocationURL());
	}
}
