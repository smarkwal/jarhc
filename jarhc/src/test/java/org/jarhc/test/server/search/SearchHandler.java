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

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import org.jarhc.test.server.HttpException;
import org.jarhc.test.server.ServerMode;

import static java.nio.charset.StandardCharsets.UTF_8;

public class SearchHandler implements HttpHandler {

	private final ServerMode mode;
	private final LocalSearchClient localSearchClient;
	private final RemoteSearchClient remoteSearchClient;

	public SearchHandler(ServerMode mode, int timeout) {
		this.mode = mode;
		localSearchClient = new LocalSearchClient();
		remoteSearchClient = new RemoteSearchClient(timeout);
	}

	@Override
	public void handle(HttpExchange exchange) throws IOException {

		String path = exchange.getRequestURI().getPath();
		if (!path.equals("/search")) {
			exchange.sendResponseHeaders(404, -1);
			return;
		}

		Map<String, String> params = queryToMap(exchange);
		String checksum = params.get("checksum");

		if (mode != ServerMode.RemoteOnly) {

			Optional<byte[]> response = localSearchClient.get(checksum);

			if (response.isPresent()) {
				byte[] data = response.get();
				exchange.sendResponseHeaders(200, data.length);
				exchange.getResponseBody().write(data);
				return;
			}
		}

		if (mode != ServerMode.LocalOnly) {

			Optional<byte[]> response;
			try {
				response = remoteSearchClient.get(checksum);
			} catch (HttpException e) {
				exchange.sendResponseHeaders(e.getStatusCode(), -1);
				return;
			} catch (IOException e) {
				e.printStackTrace();
				exchange.sendResponseHeaders(500, -1);
				return;
			}

			if (response.isEmpty()) {
				exchange.sendResponseHeaders(404, -1);
				return;
			}

			byte[] data = response.get();
			exchange.sendResponseHeaders(200, data.length);
			exchange.getResponseBody().write(data);

			if (mode == ServerMode.LocalRemoteUpdate) {
				localSearchClient.put(checksum, data);
			}

		}

	}

	public static Map<String, String> queryToMap(HttpExchange exchange) {
		String query = exchange.getRequestURI().getQuery();
		Map<String, String> result = new LinkedHashMap<>();
		if (query == null) {
			return result;
		}
		for (String params : query.split("&")) {
			String[] param = params.split("=", 2);
			String name = URLDecoder.decode(param[0], UTF_8);
			String value = param.length > 1 ? URLDecoder.decode(param[1], UTF_8) : "";
			result.put(name, value);
		}
		return result;
	}

}
