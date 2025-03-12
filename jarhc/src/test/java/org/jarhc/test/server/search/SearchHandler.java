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
import java.net.URI;
import java.net.URLDecoder;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import org.jarhc.test.server.HttpException;
import org.jarhc.test.server.ServerMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.nio.charset.StandardCharsets.UTF_8;

public class SearchHandler implements HttpHandler {

	private final Logger LOGGER = LoggerFactory.getLogger(SearchHandler.class);

	private final ServerMode mode;
	private final LocalSearchClient localSearchClient;
	private final RemoteSearchClient remoteSearchClient;

	public SearchHandler(ServerMode mode, int timeout, Path rootPath) {
		this.mode = mode;
		localSearchClient = new LocalSearchClient(rootPath);
		remoteSearchClient = new RemoteSearchClient(timeout);
	}

	@Override
	public void handle(HttpExchange exchange) throws IOException {
		URI uri = exchange.getRequestURI();
		LOGGER.debug("Request: {}", uri);

		String path = uri.getPath();
		if (!path.equals("/search")) {
			sendNotFound(exchange);
			return;
		}

		Map<String, String> params = queryToMap(uri);
		String checksum = params.get("checksum");

		if (!checksum.matches("^[0-9a-f]{40}$")) {
			sendNotFound(exchange);
			return;
		}

		if (mode != ServerMode.RemoteOnly) {

			Optional<byte[]> response = localSearchClient.get(checksum);

			if (response.isPresent()) {
				byte[] data = response.get();
				sendData(exchange, data);
				return;
			}
		}

		if (mode != ServerMode.LocalOnly) {

			Optional<byte[]> response;
			try {
				response = remoteSearchClient.get(checksum);
			} catch (HttpException e) {
				LOGGER.warn("Unexpected HTTP status code: {}", e.getStatusCode());
				sendError(exchange, e);
				return;
			} catch (IOException e) {
				LOGGER.error("Unexpected I/O error", e);
				sendInternalServerError(exchange);
				return;
			}

			if (response.isEmpty()) {
				sendNotFound(exchange);
				return;
			}

			byte[] data = response.get();
			sendData(exchange, data);

			if (mode == ServerMode.LocalRemoteUpdate) {
				localSearchClient.put(checksum, data);
			}

		}
	}

	public static Map<String, String> queryToMap(URI uri) {
		String query = uri.getQuery();
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

	private static void sendNotFound(HttpExchange exchange) throws IOException {
		exchange.sendResponseHeaders(404, -1);
		exchange.close();
	}

	private static void sendError(HttpExchange exchange, HttpException exception) throws IOException {
		exchange.sendResponseHeaders(exception.getStatusCode(), -1);
		exchange.close();
	}

	private static void sendInternalServerError(HttpExchange exchange) throws IOException {
		exchange.sendResponseHeaders(500, -1);
		exchange.close();
	}

	private static void sendData(HttpExchange exchange, byte[] data) throws IOException {
		exchange.getResponseHeaders().add("Content-Type", "application/json");
		exchange.sendResponseHeaders(200, data.length);
		exchange.getResponseBody().write(data);
		exchange.close();
	}

}
