package com.javax0.license3j.licensor;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.util.Date;
import java.util.UUID;

public class TestExtendedLicense {
    private static final String licenceUrlTemplate = "https://github.com/verhas/License3j/blob/master/src/test/resources/${licenseId}";
    private static final String reachableUrlString = "https://github.com/verhas/License3j";
    private static final String unreachableUrlString = "https://any.com";

    private void mockHttpFetch(final ExtendedLicense lic, final int status, final IOException exception) {
        var handler = new MockHttpHandler();
        if (exception == null) {
            handler.setResponseCode(status);
        } else {
            handler.setException(exception);
        }
        lic.httpHandler = handler;
    }

    @Test
    public void licenseIsNotRevokedWhenHttpReturns200OK() throws IOException {
        final var lic = new ExtendedLicense();
        mockHttpFetch(lic, 200, null);
        lic.setRevocationURL(new URL(reachableUrlString));
        Assert.assertFalse(lic.isRevoked(true));
    }

    @Test
    public void licenseIsNotRevokedWhenUnreachableGraceful() throws IOException {
        final var lic = new ExtendedLicense();
        mockHttpFetch(lic, 0, new IOException());
        lic.setRevocationURL(new URL(unreachableUrlString));
        Assert.assertFalse(lic.isRevoked(false));
    }

    @Test
    public void licenseIsNotRevokedWhenUnreachableStrict() throws IOException {
        final var lic = new ExtendedLicense();
        mockHttpFetch(lic, 0, new IOException());
        lic.setRevocationURL(new URL(unreachableUrlString));
        Assert.assertTrue(lic.isRevoked(true));
    }

    @Test
    public void licenseIsNotRevokedWhenNoIdIsInUrlTemplateAndTheUrlIsOK()
        throws IOException {
        final var lic = new ExtendedLicense();
        mockHttpFetch(lic, 200, null);
        lic.setRevocationURL(reachableUrlString);
        lic.setLicenseId(new UUID(0, 1L));
        Assert.assertFalse(lic.isRevoked());
    }

    @Test
    public void licenseIsNotRevokedWhenIdFileIsThere() throws ParseException, IOException {
        final var lic = new ExtendedLicense();
        mockHttpFetch(lic, 0, new IOException());
        lic.setRevocationURL(licenceUrlTemplate);
        lic.setLicenseId(new UUID(0, 1L));
        Assert.assertFalse(lic.isRevoked(false));
    }

    @Test
    public void licenseIsRevokedWhenIdFileIsNotThere() throws ParseException, IOException {
        final var lic = new ExtendedLicense();
        mockHttpFetch(lic, 404, null);
        lic.setRevocationURL(licenceUrlTemplate);
        lic.setLicenseId(new UUID(0, 2L));
        Assert.assertTrue(lic.isRevoked());
    }

    @Test
    public void licenseIsRevokedWhenUrlIsMalformed() throws ParseException, IOException {
        final var lic = new ExtendedLicense();
        mockHttpFetch(lic, 0, new IOException());
        lic.setRevocationURL("ftp://index.hu/");
        Assert.assertTrue(lic.isRevoked(true));
    }

    @Test
    public void pastExpiryTimeReportsExpired() throws ParseException {
        final var lic = new ExtendedLicense();
        lic.setExpiry(new Date(new Date().getTime() - 24 * 60 * 60 * 1000));
        Assert.assertTrue(lic.isExpired());
    }

    @Test
    public void futureExpiryTimeReportsNonExpired() throws ParseException {
        final var lic = new ExtendedLicense();
        lic.setExpiry(new Date(new Date().getTime() + 24 * 60 * 60 * 1000));
        Assert.assertFalse(lic.isExpired());
    }

    @Test
    public void badlyFormattedExpiryDateReportsExpiredLicense()
        throws ParseException {
        final var lic = new ExtendedLicense();
        lic.setFeature("expiryDate", "not a valid date");
        Assert.assertTrue(lic.isExpired());
    }

    @Test
    public void uuidGenerationResultsNonNullUuid() throws ParseException {
        final var lic = new ExtendedLicense();
        lic.generateLicenseId();
        Assert.assertNotNull(lic.getLicenseId());
    }

    @Test
    public void settingIntFeaturesGetsTheValueBackAsInteger() {
        final int n = 10;
        final var lic = new ExtendedLicense();
        for (int i = 0; i < n; i++) {
            lic.setFeature("testInteger" + i, i);
        }
        for (int i = 0; i < n; i++) {
            Assert.assertEquals((Integer) i,
                lic.getFeature("testInteger" + i, Integer.class));
        }
    }

    @Test
    public void settingUrlFeatureReturnsTheUrl() throws MalformedURLException {
        final var lic = new ExtendedLicense();
        lic.setFeature("testURL", new URL("http://index.hu"));
        Assert.assertEquals(new URL("http://index.hu"),
            lic.getFeature("testURL", URL.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void gettingInvalidFeatureTypeThrowsException() {
        final var lic = new ExtendedLicense();
        lic.getFeature("testURL", Object.class);
    }

    @Test
    public void notSettingRevocationUrlResultNullRevocationUrl()
        throws MalformedURLException {
        final var lic = new ExtendedLicense();
        Assert.assertNull(lic.getRevocationURL());
    }

    @Test
    public void licenseWithNoRevocationUrlIsNotRevoked() {
        final var lic = new ExtendedLicense();
        Assert.assertFalse(lic.isRevoked(true));
    }

    private static class MockHttpHandler extends HttpHandler {
        private int responseCode;

        public void setException(IOException exception) {
            this.exception = exception;
        }

        private IOException exception = null;

        public void setResponseCode(int responseCode) {
            this.responseCode = responseCode;
        }

        int getResponseCode(final HttpURLConnection httpUrlConnection)
            throws IOException {
            if (exception != null) {
                throw exception;
            }
            return responseCode;
        }

        URLConnection openConnection(URL url) {
            return new HttpURLConnection(url) {

                @Override
                public void disconnect() {
                }

                @Override
                public boolean usingProxy() {
                    return false;
                }

                @Override
                public void connect() throws IOException {
                    if (exception != null) {
                        throw exception;
                    }
                }
            };
        }
    }
}
