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

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.Optional;

public class MavenCentralResolver implements Resolver {

	private static final String URL_FORMAT = "https://search.maven.org/solrsearch/select?q=1:%%22%s%%22&rows=20&wt=json";

	private int timeout;

	/**
	 * Create an artifact resolver using Maven Central REST API.
	 *
	 * @param timeout Timeout
	 */
	public MavenCentralResolver(Duration timeout) {
		this.timeout = (int) timeout.toMillis();
	}

	@Override
	public Optional<Artifact> getArtifact(String checksum) throws ResolverException {
		validateChecksum(checksum);

		URL url;
		try {
			url = new URL(String.format(URL_FORMAT, checksum));
		} catch (MalformedURLException e) {
			throw new ResolverException("Malformed URL for checksum: " + checksum, e);
		}

		String text = executeHttpRequest(url);
		// TODO: special handling for timeout?

		// parse response
		JSONObject json;
		try {
			json = new JSONObject(text);
		} catch (JSONException e) {
			throw new ResolverException("JSON parser error for URL: " + url, e);
		}

		if (!json.has("response")) {
			throw new ResolverException("JSON key 'response' not found: " + text);
		}

		JSONObject response = json.getJSONObject("response");
		if (!response.has("numFound")) {
			throw new ResolverException("JSON key 'numFound' not found: " + text);
		}

		int numFound = response.getInt("numFound");
		if (numFound == 0) {
			return Optional.empty(); // artifact not found
		}

		if (!response.has("docs")) {
			throw new ResolverException("JSON key 'docs' not found: " + text);
		}

		JSONArray docs = response.getJSONArray("docs");
		if (docs.length() == 0) {
			throw new ResolverException("JSON array 'docs' is empty: " + text);
		}

		JSONObject doc = findBestMatch(docs);
		String groupId = doc.getString("g");
		String artifactId = doc.getString("a");
		String version = doc.getString("v");
		String type = doc.getString("p");

		Artifact artifact = new Artifact(groupId, artifactId, version, type);
		return Optional.of(artifact);
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

	private String executeHttpRequest(URL url) throws ResolverException {

		try {

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
				if (status != 200) {
					throw new ResolverException("Unexpected status code '" + status + "' for URL: " + url);
				}

				try (InputStream stream = connection.getInputStream()) {
					return IOUtils.toString(stream);
				}

			} finally {
				if (connection != null) {
					connection.disconnect();
				}
			}

		} catch (IOException e) {
			throw new ResolverException("Unexpected I/O error for URL: " + url, e);
		}

	}

}
