/*
 * Copyright 2026 Stephan Markwalder
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jarhc.artifacts;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import org.jarhc.utils.IOUtils;
import org.slf4j.Logger;

/**
 * Shared HTTP client for the <a href="https://deps.dev">deps.dev</a> API.
 * <p>
 * Performs a GET request against a deps.dev endpoint and returns the response
 * body, translating an HTTP 404 into an empty result and an unexpected I/O error
 * into a {@link RepositoryException}. Used by both {@link DepsDevApiArtifactFinder}
 * and {@link DepsDevApiVulnerabilityFinder}, which build the request URLs and parse
 * the JSON responses.
 */
class DepsDevApiClient {

	private final Logger logger;
	private final DepsDevApiSettings settings;

	private final AtomicBoolean statusInfoLogged = new AtomicBoolean(false);

	DepsDevApiClient(Logger logger, DepsDevApiSettings settings) {
		this.logger = logger;
		this.settings = settings;
	}

	/**
	 * Download the response body for the given URL.
	 *
	 * @param url URL
	 * @return Response body, or an empty {@link Optional} if the API responded with HTTP 404.
	 * @throws RepositoryException If an unexpected I/O error occurs.
	 */
	Optional<String> downloadText(URL url) throws RepositoryException {
		try {
			Optional<byte[]> data = downloadFile(url);
			return data.map(bytes -> new String(bytes, StandardCharsets.UTF_8));
		} catch (IOException e) {
			// log information about deps.dev status once
			if (statusInfoLogged.compareAndSet(false, true)) {
				logger.warn("Problem with api.deps.dev. Visit https://deps.dev to check the status of the deps.dev API.");
			}
			throw new RepositoryException("Unexpected I/O error for URL: " + url, e);
		}
	}

	private Optional<byte[]> downloadFile(URL url) throws IOException {

		HttpURLConnection connection = null;
		try {

			// get connection settings
			int timeout = settings.getTimeout() * 1000;
			Map<String, String> headers = settings.getHeaders();

			// prepare connection
			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("GET");
			for (Map.Entry<String, String> header : headers.entrySet()) {
				connection.setRequestProperty(header.getKey(), header.getValue());
			}
			connection.setConnectTimeout(timeout);
			connection.setReadTimeout(timeout);
			connection.setDoOutput(false);
			connection.setDoInput(true);

			// get response
			int status = connection.getResponseCode();
			if (status == 404) {
				// not found (no results match the query)
				return Optional.empty();
			} else if (status != 200) {
				throw new IOException("Unexpected status code '" + status + "' for URL: " + url);
			}

			try (InputStream stream = connection.getInputStream()) {
				byte[] data = IOUtils.toByteArray(stream);
				return Optional.of(data);
			}

		} finally {
			if (connection != null) {
				connection.disconnect();
			}
		}

	}

}
