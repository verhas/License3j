package com.javax0.license3j.licensor;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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

    /**
     * Set up a mock HttpHandler in the license that will return with the status or will throw exception.
     * @param lic the license where the mock will be inserted into
     * @param status to be returned when {@code exception} is {@code null}
     * @param exception the exception to be thrown or {@code null} if there should be no exception calling the handler
     */
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
    @DisplayName("license is not revoked when the http request returns true")
    public void licenseIsNotRevokedWhenHttpReturns200OK() throws IOException {
        final var lic = new ExtendedLicense();
        mockHttpFetch(lic, 200, null);
        lic.setRevocationURL(new URL(reachableUrlString));
        Assertions.assertFalse(lic.isRevoked(true));
    }

    @Test
    @DisplayName("license is not revoked when the URL is not reachable")
    public void licenseIsNotRevokedWhenUnreachableGraceful() throws IOException {
        final var lic = new ExtendedLicense();
        mockHttpFetch(lic, 0, new IOException());
        lic.setRevocationURL(new URL(unreachableUrlString));
        Assertions.assertFalse(lic.isRevoked(false));
    }

    @Test
    public void licenseIsNotRevokedWhenUnreachableStrict() throws IOException {
        final var lic = new ExtendedLicense();
        mockHttpFetch(lic, 0, new IOException());
        lic.setRevocationURL(new URL(unreachableUrlString));
        Assertions.assertTrue(lic.isRevoked(true));
    }

    @Test
    public void licenseIsNotRevokedWhenNoIdIsInUrlTemplateAndTheUrlIsOK()
            throws IOException {
        final var lic = new ExtendedLicense();
        mockHttpFetch(lic, 200, null);
        lic.setRevocationURL(reachableUrlString);
        lic.setLicenseId(new UUID(0, 1L));
        Assertions.assertFalse(lic.isRevoked());
    }

    @Test
    public void licenseIsNotRevokedWhenIdFileIsThere() throws ParseException, IOException {
        final var lic = new ExtendedLicense();
        mockHttpFetch(lic, 0, new IOException());
        lic.setRevocationURL(licenceUrlTemplate);
        lic.setLicenseId(new UUID(0, 1L));
        Assertions.assertFalse(lic.isRevoked(false));
    }

    @Test
    public void licenseIsRevokedWhenIdFileIsNotThere() throws ParseException, IOException {
        final var lic = new ExtendedLicense();
        mockHttpFetch(lic, 404, null);
        lic.setRevocationURL(licenceUrlTemplate);
        lic.setLicenseId(new UUID(0, 2L));
        Assertions.assertTrue(lic.isRevoked());
    }

    @Test
    public void licenseIsRevokedWhenUrlIsMalformed() throws ParseException, IOException {
        final var lic = new ExtendedLicense();
        mockHttpFetch(lic, 0, new IOException());
        lic.setRevocationURL("ftp://index.hu/");
        Assertions.assertTrue(lic.isRevoked(true));
    }

    @Test
    public void pastExpiryTimeReportsExpired() throws ParseException {
        final var lic = new ExtendedLicense();
        lic.setExpiry(new Date(new Date().getTime() - 24 * 60 * 60 * 1000));
        Assertions.assertTrue(lic.isExpired());
    }

    @Test
    public void futureExpiryTimeReportsNonExpired() throws ParseException {
        final var lic = new ExtendedLicense();
        lic.setExpiry(new Date(new Date().getTime() + 24 * 60 * 60 * 1000));
        Assertions.assertFalse(lic.isExpired());
    }

    @Test
    public void badlyFormattedExpiryDateReportsExpiredLicense()
            throws ParseException {
        final var lic = new ExtendedLicense();
        lic.setFeature("expiryDate", "not a valid date");
        Assertions.assertTrue(lic.isExpired());
    }

    @Test
    public void uuidGenerationResultsNonNullUuid() throws ParseException {
        final var lic = new ExtendedLicense();
        lic.generateLicenseId();
        Assertions.assertNotNull(lic.getLicenseId());
    }

    @Test
    public void settingIntFeaturesGetsTheValueBackAsInteger() {
        final int n = 10;
        final var lic = new ExtendedLicense();
        for (int i = 0; i < n; i++) {
            lic.setFeature("testInteger" + i, i);
        }
        for (int i = 0; i < n; i++) {
            Assertions.assertEquals((Integer) i,
                    lic.getFeature("testInteger" + i, Integer.class));
        }
    }

    @Test
    public void settingUrlFeatureReturnsTheUrl() throws MalformedURLException {
        final var lic = new ExtendedLicense();
        lic.setFeature("testURL", new URL("http://index.hu"));
        Assertions.assertEquals(new URL("http://index.hu"),
                lic.getFeature("testURL", URL.class));
    }

    @Test
    public void gettingInvalidFeatureTypeThrowsException() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            final var lic = new ExtendedLicense();
            lic.getFeature("testURL", Object.class);
        });
    }

    @Test
    public void notSettingRevocationUrlResultNullRevocationUrl()
            throws MalformedURLException {
        final var lic = new ExtendedLicense();
        Assertions.assertNull(lic.getRevocationURL());
    }

    @Test
    public void licenseWithNoRevocationUrlIsNotRevoked() {
        final var lic = new ExtendedLicense();
        Assertions.assertFalse(lic.isRevoked(true));
    }

    private static class MockHttpHandler extends HttpHandler {
        private int responseCode;
        private IOException exception = null;

        public void setException(IOException exception) {
            this.exception = exception;
        }

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
