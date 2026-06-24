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

package org.jarhc.test.server.systems;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.time.temporal.ChronoUnit.SECONDS;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Optional;
import org.jarhc.test.server.HttpException;

/**
 * Fetches the deps.dev "GetVersion" response (the advisory keys affecting a
 * given Maven version) from the real deps.dev API.
 */
class RemoteSystemsClient {

	private static final String URL_PATTERN = "https://api.deps.dev/v3/systems/MAVEN/packages/%s/versions/%s";

	private final Duration timeout;

	RemoteSystemsClient(int timeout) {
		this.timeout = Duration.of(timeout, SECONDS);
	}

	/**
	 * @param name    Maven package name ("groupId:artifactId")
	 * @param version Version
	 */
	Optional<byte[]> get(String name, String version) throws IOException {

		String url = String.format(URL_PATTERN, encode(name), encode(version));
		URI uri = URI.create(url);

		HttpClient client = HttpClient.newHttpClient();
		HttpRequest request = HttpRequest.newBuilder()
				.GET()
				.uri(uri)
				.timeout(timeout)
				.build();

		HttpResponse<byte[]> response;
		try {
			response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
		} catch (InterruptedException e) {
			throw new IOException(e);
		}

		int statusCode = response.statusCode();
		if (statusCode == 200) {
			return Optional.of(response.body());
		} else if (statusCode == 404) {
			return Optional.empty(); // not found
		} else {
			throw new HttpException(statusCode);
		}
	}

	private static String encode(String value) {
		return URLEncoder.encode(value, UTF_8);
	}

}
