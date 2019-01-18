/*
 * Copyright 2018 Stephan Markwalder
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

import org.jarhc.utils.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Optional;

public class MavenCentralRepository implements Repository {

	private static final String SEARCH_URL_FORMAT_1 = "https://search.maven.org/solrsearch/select?q=g:%%22%s%%22+AND+a:%%22%s%%22+AND+v:%%22%s%%22&core=gav&rows=20&wt=json";
	private static final String SEARCH_URL_FORMAT_2 = "https://search.maven.org/solrsearch/select?q=1:%%22%s%%22&rows=20&wt=json";
	private static final String DOWNLOAD_URL_FORMAT = "https://search.maven.org/remotecontent?filepath=%s";

	private int timeout;

	/**
	 * Create an artifact repository using Maven Central REST API.
	 *
	 * @param timeout Timeout
	 */
	public MavenCentralRepository(Duration timeout) {
		this.timeout = (int) timeout.toMillis();
	}

	@Override
	public Optional<Artifact> findArtifact(String groupId, String artifactId, String version, String type) throws RepositoryException {

		URL url;
		try {
			url = new URL(String.format(SEARCH_URL_FORMAT_1, groupId, artifactId, version));
		} catch (MalformedURLException e) {
			throw new RepositoryException("Malformed URL for coordinates: " + groupId + ":" + artifactId + ":" + version, e);
		}

		return findArtifact(url);
	}

	@Override
	public Optional<Artifact> findArtifact(String checksum) throws RepositoryException {
		validateChecksum(checksum);

		URL url;
		try {
			url = new URL(String.format(SEARCH_URL_FORMAT_2, checksum));
		} catch (MalformedURLException e) {
			throw new RepositoryException("Malformed URL for checksum: " + checksum, e);
		}

		return findArtifact(url);
	}

	private Optional<Artifact> findArtifact(URL url) throws RepositoryException {

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
		return Optional.of(artifact);
	}

	@Override
	public Optional<InputStream> downloadArtifact(Artifact artifact) throws RepositoryException {
		URL url = getDownloadURL(artifact);
		try {
			byte[] data = downloadFile(url);
			if (data == null) return Optional.empty();
			ByteArrayInputStream stream = new ByteArrayInputStream(data);
			return Optional.of(stream);
		} catch (IOException e) {
			throw new RepositoryException("Unexpected I/O error for URL: " + url, e);
		}
	}

	private URL getDownloadURL(Artifact artifact) throws RepositoryException {

		String groupId = artifact.getGroupId();
		String artifactId = artifact.getArtifactId();
		String version = artifact.getVersion();
		String type = artifact.getType();

		StringBuilder path = new StringBuilder();
		path.append(groupId.replace('.', '/'));
		path.append('/');
		path.append(artifactId.replace('.', '/'));
		path.append('/');
		path.append(version);
		path.append('/');
		path.append(artifactId);
		path.append('-');
		path.append(version);
		path.append(".jar"); // TODO: get extension from type?

		try {
			return new URL(String.format(DOWNLOAD_URL_FORMAT, path));
		} catch (MalformedURLException e) {
			throw new RepositoryException("Malformed URL for download: " + groupId + ":" + artifactId + ":" + version, e);
		}
	}

	private String downloadText(URL url) throws RepositoryException {
		try {
			byte[] data = downloadFile(url);
			if (data == null) throw new RepositoryException("URL not found: " + url);
			return new String(data, StandardCharsets.UTF_8);
		} catch (IOException e) {
			throw new RepositoryException("Unexpected I/O error for URL: " + url, e);
		}
	}

	private byte[] downloadFile(URL url) throws IOException {

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
				return null;
			} else if (status != 200) {
				throw new IOException("Unexpected status code '" + status + "' for URL: " + url);
			}

			try (InputStream stream = connection.getInputStream()) {
				return IOUtils.toByteArray(stream);
			}

		} finally {
			if (connection != null) {
				connection.disconnect();
			}
		}

	}

	private JSONObject findBestMatch(JSONArray docs) {

		int num = docs.length();
		if (num == 1) {
			return docs.getJSONObject(0);
		}

		// multiple matches:
		// prefer shorter artifact coordinates

		JSONObject bestDoc = null;
		int minLen = Integer.MAX_VALUE;

		for (int i = 0; i < num; i++) {
			JSONObject doc = docs.getJSONObject(i);
			String id = doc.getString("id");
			int len = id.length();
			if (len < minLen) {
				bestDoc = doc;
				minLen = len;
			}
		}

		return bestDoc;
	}

}
