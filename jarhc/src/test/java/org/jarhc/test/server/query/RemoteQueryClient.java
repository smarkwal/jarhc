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

package org.jarhc.test.server.query;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.time.temporal.ChronoUnit.SECONDS;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Base64;
import java.util.Optional;
import org.jarhc.test.server.HttpException;

class RemoteQueryClient implements QueryClient {

	private static final String URL_PATTERN = "https://api.deps.dev/v3/query?hash.type=SHA1&hash.value=%s";

	private final Duration timeout;

	RemoteQueryClient(int timeout) {
		this.timeout = Duration.of(timeout, SECONDS);
	}

	@Override
	public Optional<byte[]> get(String checksum) throws IOException {

		// convert the hex checksum into a URL-encoded base64 value
		byte[] bytes = hexToBytes(checksum);
		String base64 = Base64.getEncoder().encodeToString(bytes);
		String hashValue = URLEncoder.encode(base64, UTF_8);

		String url = String.format(URL_PATTERN, hashValue);
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

}
