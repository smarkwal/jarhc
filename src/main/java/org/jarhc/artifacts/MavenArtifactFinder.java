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

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.jarhc.utils.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MavenArtifactFinder {

	private static final String SEARCH_URL_FORMAT = "https://search.maven.org/solrsearch/select?q=1:%%22%s%%22&rows=20&wt=json";

	private final Map<String, Artifact> cache = new ConcurrentHashMap<>();

	// TODO: configure timeout
	private final int timeout = 10 * 1000;

	public Optional<Artifact> findArtifact(String checksum) throws RepositoryException {

		// check cache
		if (cache.containsKey(checksum)) {
			Artifact artifact = cache.get(checksum);
			return Optional.of(artifact);
		}

		validateChecksum(checksum);

		URL url;
		try {
			url = new URL(String.format(SEARCH_URL_FORMAT, checksum));
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
			return Optional.empty(); // artifact not found
		}

		if (!response.has("docs")) {
			throw new RepositoryException("JSON key 'docs' not found: " + text);
		}

		JSONArray docs = response.getJSONArray("docs");
		if (docs.length() == 0) {
			throw new RepositoryException("JSON array 'docs' is empty: " + text);
		}

		JSONObject doc = findBestMatch(docs);
		String groupId = doc.getString("g");
		String artifactId = doc.getString("a");
		String version = doc.getString("v");
		String type = doc.getString("p");

		Artifact artifact = new Artifact(groupId, artifactId, version, type);
		cache.put(checksum, artifact);
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
			if (!data.isPresent()) throw new RepositoryException("URL not found: " + url);
			return new String(data.get(), StandardCharsets.UTF_8);
		} catch (IOException e) {
			throw new RepositoryException("Unexpected I/O error for URL: " + url, e);
		}
	}

	private Optional<byte[]> downloadFile(URL url) throws IOException {

		HttpURLConnection connection = null;
		try {

			// prepare connection
			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("GET");
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

	private static JSONObject findBestMatch(JSONArray docs) {

		JSONObject bestDoc = docs.getJSONObject(0);

		int num = docs.length();
		if (num > 1) {

			// multiple matches:
			// prefer shorter artifact coordinates

			int bestLen = bestDoc.getString("id").length();

			for (int i = 1; i < num; i++) {
				JSONObject doc = docs.getJSONObject(i);
				String id = doc.getString("id");
				int len = id.length();
				if (len < bestLen) {
					bestDoc = doc;
					bestLen = len;
				}
			}
		}

		return bestDoc;
	}

}
