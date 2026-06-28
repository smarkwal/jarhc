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
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import org.jarhc.utils.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of {@link ArtifactFinder} that uses the
 * <a href="https://deps.dev">deps.dev</a> API from Google to find artifacts.
 * <p>
 * Artifacts are looked up by the SHA-1 checksum of their content. The checksum
 * is passed to the API as a URL-encoded, base64-encoded value.
 */
public class DepsDevAPIArtifactFinder implements ArtifactFinder {

	// Example request URLs (hash.value is the URL-encoded base64 of the raw SHA-1 bytes):
	// https://api.deps.dev/v3/query?hash.type=SHA1&hash.value=gViT318x2i7OQED%2BChL9RLV3r68%3D (commons-io:commons-io:2.6)
	// https://api.deps.dev/v3/query?hash.type=SHA1&hash.value=1UEMI%2BFNREuy%2FBO0fX6q1KIXEoE%3D (commons-codec:commons-codec:1.11)
	//
	// Note: the "versionKey.system=MAVEN" filter is intentionally NOT used, as it is
	// incompatible with hash queries (the API responds with HTTP 404). Results are
	// filtered to the Maven system on the client side instead.

	// endpoint path appended to the deps.dev base URL (see DepsDevSettings)
	private static final String QUERY_PATH = "/query?hash.type=SHA1&hash.value=%s";

	private static final String MAVEN_SYSTEM = "MAVEN";

	private final Logger logger;
	private final DepsDevSettings settings;

	private final AtomicBoolean statusInfoLogged = new AtomicBoolean(false);

	public DepsDevAPIArtifactFinder() {
		this(LoggerFactory.getLogger(DepsDevAPIArtifactFinder.class), DepsDevSettings.fromSystemProperties());
	}

	public DepsDevAPIArtifactFinder(Logger logger) {
		this(logger, DepsDevSettings.fromSystemProperties());
	}

	DepsDevAPIArtifactFinder(Logger logger, DepsDevSettings settings) {
		this.logger = logger;
		this.settings = settings;
	}

	@Override
	public List<Artifact> findArtifacts(String checksum) throws RepositoryException {

		ArtifactFinder.validateChecksum(checksum);

		long time = 0;
		if (logger.isDebugEnabled()) {
			time = System.nanoTime();
		}

		// convert the hex checksum into a URL-encoded base64 value
		String hashValue = encodeChecksum(checksum);

		URL url;
		try {
			url = new URL(settings.getBaseUrl() + String.format(QUERY_PATH, hashValue));
		} catch (MalformedURLException e) {
			throw new RepositoryException("Malformed URL for checksum: " + checksum, e);
		}

		Optional<String> text = downloadText(url);
		if (text.isEmpty()) {
			// artifact not found (API responded with HTTP 404)
			if (logger.isDebugEnabled()) {
				time = System.nanoTime() - time;
				logger.warn("Artifact not found: {} (time: {} ms)", checksum, time / 1000 / 1000);
			}
			return List.of();
		}

		// parse response
		JSONObject json;
		try {
			json = new JSONObject(text.get());
		} catch (JSONException e) {
			throw new RepositoryException("JSON parser error for URL: " + url, e);
		}

		if (!json.has("results")) {
			throw new RepositoryException("JSON key 'results' not found: " + text.get());
		}

		JSONArray results = json.getJSONArray("results");

		// parse JSON code and create list of artifacts (ordered by relevance)
		List<Artifact> artifacts = parseArtifacts(results);

		if (artifacts.isEmpty()) {
			// no matching Maven artifact found
			if (logger.isDebugEnabled()) {
				time = System.nanoTime() - time;
				logger.warn("Artifact not found: {} (time: {} ms)", checksum, time / 1000 / 1000);
			}
			return List.of();
		}

		if (logger.isDebugEnabled()) {
			time = System.nanoTime() - time;
			logger.debug("Artifact found: {} -> {} (time: {} ms)", checksum, artifacts, time / 1000 / 1000);
		}
		return artifacts;
	}

	/**
	 * Convert a hex-encoded SHA-1 checksum into the URL-encoded base64 value
	 * expected by the deps.dev API.
	 *
	 * @param checksum Hex-encoded SHA-1 checksum
	 * @return URL-encoded base64 value
	 */
	private static String encodeChecksum(String checksum) {
		byte[] bytes = hexToBytes(checksum);
		String base64 = Base64.getEncoder().encodeToString(bytes);
		return URLEncoder.encode(base64, StandardCharsets.UTF_8);
	}

	private static byte[] hexToBytes(String hex) {
		int length = hex.length();
		byte[] bytes = new byte[length / 2];
		for (int i = 0; i < bytes.length; i++) {
			int high = Character.digit(hex.charAt(i * 2), 16);
			int low = Character.digit(hex.charAt(i * 2 + 1), 16);
			bytes[i] = (byte) ((high << 4) | low);
		}
		return bytes;
	}

	/**
	 * Download the response body for the given URL.
	 *
	 * @param url URL
	 * @return Response body, or an empty {@link Optional} if the API responded with HTTP 404.
	 * @throws RepositoryException If an unexpected I/O error occurs.
	 */
	private Optional<String> downloadText(URL url) throws RepositoryException {
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
				// no results match the query (artifact not found)
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

	private List<Artifact> parseArtifacts(JSONArray results) {

		// prepare list of artifacts
		ArrayList<Artifact> artifacts = new ArrayList<>();

		for (int i = 0; i < results.length(); i++) {
			Artifact artifact = parseArtifact(results.getJSONObject(i));
			if (artifact != null) {
				artifacts.add(artifact);
			}
		}

		// multiple matches:
		// prefer shorter artifact coordinates
		artifacts.sort((a1, a2) -> {
			int len1 = a1.getGroupId().length() + a1.getArtifactId().length() + a1.getVersion().length();
			int len2 = a2.getGroupId().length() + a2.getArtifactId().length() + a2.getVersion().length();
			return Integer.compare(len1, len2);
		});

		return artifacts;
	}

	/**
	 * Parse a single deps.dev result into a Maven artifact, or return
	 * <code>null</code> if the result is not a usable Maven artifact.
	 */
	private static Artifact parseArtifact(JSONObject result) {

		JSONObject version = result.getJSONObject("version");
		JSONObject versionKey = version.getJSONObject("versionKey");

		String system = versionKey.getString("system");
		if (!MAVEN_SYSTEM.equals(system)) {
			// skip artifacts from other package systems (e.g. NPM, PYPI)
			return null;
		}

		// deps.dev returns the Maven name as "groupId:artifactId"
		String name = versionKey.getString("name");
		int index = name.indexOf(':');
		if (index < 0) {
			return null;
		}
		String groupId = name.substring(0, index);
		String artifactId = name.substring(index + 1);
		String versionNumber = versionKey.getString("version");

		// deps.dev does not report the packaging type, default to "jar"
		return new Artifact(groupId, artifactId, versionNumber, "jar");
	}

}
