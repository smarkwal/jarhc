/*
 * Copyright 2021 Stephan Markwalder
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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import org.jarhc.utils.FileUtils;
import org.jarhc.utils.IOUtils;
import org.jarhc.utils.JarHcException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;

public class MavenArtifactFinder implements ArtifactFinder {

	private static final String SEARCH_URL = "https://search.maven.org/solrsearch/select?q=1:%%22%s%%22&rows=20&wt=json";
	private static final int SEARCH_TIMEOUT = 65;
	private static final String SEARCH_USER_AGENT = "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:129.0) Gecko/20100101 Firefox/129.0";

	private final File cacheDir;
	private final Logger logger;
	private final Settings settings;

	private final Map<String, Artifact> cache = new ConcurrentHashMap<>();

	public MavenArtifactFinder(File cacheDir, Logger logger) {
		this(cacheDir, logger, Settings.fromSystemProperties());
	}

	MavenArtifactFinder(File cacheDir, Logger logger, Settings settings) {
		this.cacheDir = cacheDir;
		this.logger = logger;
		this.settings = settings;
	}

	@Override
	@SuppressWarnings("java:S3776") // Cognitive Complexity of methods should not be too high
	public Optional<Artifact> findArtifact(String checksum) throws RepositoryException {

		// check memory cache
		if (cache.containsKey(checksum)) {
			Artifact artifact = cache.get(checksum);
			logger.debug("Artifact found: {} -> {} (memory cache)", checksum, artifact);
			return Optional.of(artifact);
		}

		validateChecksum(checksum);

		// check disk cache
		File cacheFile = new File(cacheDir, checksum + ".txt");
		if (cacheFile.isFile()) {

			// read coordinates from file
			String coordinates;
			try {
				coordinates = FileUtils.readFileToString(cacheFile);
			} catch (IOException e) {
				throw new JarHcException(e);
			}

			// validate coordinates and return artifact
			if (Artifact.validateCoordinates(coordinates)) {
				Artifact artifact = new Artifact(coordinates);

				// update in-memory cache
				cache.put(checksum, artifact);

				logger.debug("Artifact found: {} -> {} (disk cache)", checksum, artifact);
				return Optional.of(artifact);
			}
		}

		long time = 0;
		if (logger.isDebugEnabled()) {
			time = System.nanoTime();
		}

		URL url;
		try {
			url = new URL(String.format(settings.getUrl(), checksum));
		} catch (MalformedURLException e) {
			throw new RepositoryException("Malformed URL for checksum: " + checksum, e);
		}

		String text = downloadText(url);
		// TODO: special handling for timeout?

		// parse response
		JSONObject json;
		try {
			json = new JSONObject(text);
		} catch (JSONException e) {
			throw new RepositoryException("JSON parser error for URL: " + url, e);
		}

		if (!json.has("response")) {
			throw new RepositoryException("JSON key 'response' not found: " + text);
		}

		JSONObject response = json.getJSONObject("response");
		if (!response.has("numFound")) {
			throw new RepositoryException("JSON key 'numFound' not found: " + text);
		}

		int numFound = response.getInt("numFound");
		if (numFound == 0) {
			// TODO: cache negative result?

			if (logger.isDebugEnabled()) {
				time = System.nanoTime() - time;
				logger.warn("Artifact not found: {} (time: {} ms)", checksum, time / 1000 / 1000);
			}
			return Optional.empty(); // artifact not found
		}

		if (!response.has("docs")) {
			throw new RepositoryException("JSON key 'docs' not found: " + text);
		}

		JSONArray docs = response.getJSONArray("docs");
		if (docs.isEmpty()) {
			throw new RepositoryException("JSON array 'docs' is empty: " + text);
		}

		JSONObject doc = findBestMatch(docs);
		String groupId = doc.getString("g");
		String artifactId = doc.getString("a");
		String version = doc.getString("v");
		String type = doc.getString("p");

		Artifact artifact = new Artifact(groupId, artifactId, version, type);

		// update memory cache
		cache.put(checksum, artifact);

		// update disk cache
		try {
			FileUtils.writeStringToFile(artifact.toString(), cacheFile);
		} catch (IOException e) {
			throw new JarHcException(e);
		}

		if (logger.isDebugEnabled()) {
			time = System.nanoTime() - time;
			logger.debug("Artifact found: {} -> {} (time: {} ms)", checksum, artifact, time / 1000 / 1000);
		}
		return Optional.of(artifact);
	}

	/**
	 * Checks if the given checksum is valid:
	 * <ol>
	 * <li>checksum must not be <code>null</code>.</li>
	 * <li>checksum must contain only the digits '0' - '9' and letters 'a' - 'f' (hex numbers).</li>
	 * </ol>
	 * <p>
	 * This method can be used by repository implementations to validate the input value.
	 *
	 * @param checksum Checksum
	 * @throws IllegalArgumentException if the given checksum is not valid.
	 */
	private static void validateChecksum(String checksum) {
		if (checksum == null || !checksum.matches("[0-9a-f]+")) {
			throw new IllegalArgumentException("checksum: " + checksum);
		}
	}

	private String downloadText(URL url) throws RepositoryException {
		try {
			Optional<byte[]> data = downloadFile(url);
			if (data.isEmpty()) throw new RepositoryException("URL not found: " + url);
			return new String(data.get(), StandardCharsets.UTF_8);
		} catch (IOException e) {
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

	private JSONObject findBestMatch(JSONArray docs) {

		JSONObject bestDoc = docs.getJSONObject(0);

		int num = docs.length();
		if (num > 1) {

			// multiple matches:
			// prefer shorter artifact coordinates

			String bestId = bestDoc.getString("id");
			int bestLen = bestId.length();

			logger.debug("Multiple artifacts found: {}", num);
			logger.debug("- {} (length = {})", bestId, bestLen);

			for (int i = 1; i < num; i++) {
				JSONObject doc = docs.getJSONObject(i);
				String id = doc.getString("id");
				int len = id.length();
				logger.debug("- {} (length = {})", id, len);
				if (len < bestLen) {
					bestDoc = doc;
					bestId = id;
					bestLen = len;
				}
			}

			logger.debug("Shortest: {} (length = {})", bestId, bestLen);
		}

		return bestDoc;
	}

	/**
	 * Settings for Maven Search API.
	 */
	public static class Settings {

		private final String url;
		private final int timeout;
		private final Map<String, String> headers;

		/**
		 * Read settings from Java System Properties.
		 */
		public static Settings fromSystemProperties() {
			Properties systemProperties = System.getProperties();
			return fromProperties(systemProperties);
		}

		/**
		 * Read settings from Java properties.
		 */
		public static Settings fromProperties(Properties properties) {

			// read settings from properties (using default values if property is not set)
			String url = properties.getProperty("jarhc.search.url", SEARCH_URL);
			int timeout = Integer.parseInt(properties.getProperty("jarhc.search.timeout", String.valueOf(SEARCH_TIMEOUT)));
			Map<String, String> headers = new HashMap<>();
			headers.put("User-Agent", properties.getProperty("jarhc.search.headers.User-Agent", SEARCH_USER_AGENT));

			// read additional HTTP headers from properties
			for (String propertyName : properties.stringPropertyNames()) {
				if (propertyName.startsWith("jarhc.search.headers.")) {
					String headerName = propertyName.substring("jarhc.search.headers.".length());
					String headerValue = properties.getProperty(propertyName);
					headers.put(headerName, headerValue);
				}
			}

			return new Settings(url, timeout, headers);
		}

		public Settings(String url, int timeout, Map<String, String> headers) {
			this.url = url;
			this.timeout = timeout;
			this.headers = headers;
		}

		public String getUrl() {
			return url;
		}

		public int getTimeout() {
			return timeout;
		}

		public Map<String, String> getHeaders() {
			return headers;
		}

	}

}
