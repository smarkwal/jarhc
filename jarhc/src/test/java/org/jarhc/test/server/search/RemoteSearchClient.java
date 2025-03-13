/*
 * Copyright 2025 Stephan Markwalder
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

package org.jarhc.test.server.search;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Optional;
import org.jarhc.test.server.HttpException;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.time.temporal.ChronoUnit.SECONDS;

class RemoteSearchClient implements SearchClient {

	private static final String URL_PATTERN = "https://search.maven.org/solrsearch/select?q=1:%%22%s%%22&rows=20&wt=json";

	private final Duration timeout;

	RemoteSearchClient(int timeout) {
		this.timeout = Duration.of(timeout, SECONDS);
	}

	@Override
	public Optional<byte[]> get(String checksum) throws IOException {

		String url = String.format(URL_PATTERN, URLEncoder.encode(checksum, UTF_8));
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

}
