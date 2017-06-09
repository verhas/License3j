package com.verhas.licensor;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

/**
 * A simple wrapper class to make it possible to mock the network use when revocation
 * is tested. In tests a mock class extending this is injected.
 * 
 * @author Peter Verhas <peter@verhas.com>
 *
 */
class HttpHandler {

	int getResponseCode(final HttpURLConnection httpUrlConnection)
			throws IOException {
		return httpUrlConnection.getResponseCode();
	}

	/**
	 * This should be mocked when testing revocation not to wait for a
	 * connection build up to a remote server.
	 * 
	 * @param url the url to which connection is to be opened
	 * @return the connection
	 * @throws IOException if the connection can not be made
	 */
	URLConnection openConnection(URL url) throws IOException {
		return url.openConnection();
	}
}
