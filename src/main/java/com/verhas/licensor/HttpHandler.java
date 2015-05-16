package com.verhas.licensor;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

/**
 * A simple handler to make it possible to mock the network use when revocation
 * is tested.
 * 
 * @author Peter Verhas <peter@verhas.com>
 *
 */
public class HttpHandler {

	int getResponseCode(final HttpURLConnection httpUrlConnection)
			throws IOException {
		return httpUrlConnection.getResponseCode();
	}

	/**
	 * This should be mocked when testing revocation not to wait for a
	 * connection build up to a remote server.
	 * 
	 * @param url
	 * @return
	 * @throws IOException
	 */
	URLConnection openConnection(URL url) throws IOException {
		return url.openConnection();
	}
}
