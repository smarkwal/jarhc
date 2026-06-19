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

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.file.Path;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import org.jarhc.test.server.HttpException;
import org.jarhc.test.server.ServerMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.nio.charset.StandardCharsets.UTF_8;

public class QueryHandler implements HttpHandler {

	private final Logger LOGGER = LoggerFactory.getLogger(QueryHandler.class);

	private final ServerMode mode;
	private final LocalQueryClient localQueryClient;
	private final RemoteQueryClient remoteQueryClient;

	public QueryHandler(ServerMode mode, int timeout, Path rootPath) {
		this.mode = mode;
		localQueryClient = new LocalQueryClient(rootPath);
		remoteQueryClient = new RemoteQueryClient(timeout);
	}

	@Override
	public void handle(HttpExchange exchange) throws IOException {
		URI uri = exchange.getRequestURI();
		LOGGER.trace("Request: {}", uri);

		String path = uri.getPath();
		if (!path.equals("/query")) {
			sendNotFound(exchange);
			return;
		}

		Map<String, String> params = queryToMap(uri);
		String hash = params.get("hash");

		// decode the base64 hash value into a hex checksum
		String checksum = decodeChecksum(hash);
		if (checksum == null) {
			sendNotFound(exchange);
			return;
		}

		if (mode != ServerMode.RemoteOnly) {

			Optional<byte[]> response = localQueryClient.get(checksum);

			if (response.isPresent()) {
				byte[] data = response.get();
				sendData(exchange, data);
				return;
			}

			if (mode == ServerMode.LocalOnly) {
				sendNotFound(exchange);
				return;
			}
		}

		Optional<byte[]> response;
		try {
			response = remoteQueryClient.get(checksum);
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
			localQueryClient.put(checksum, data);
		}

	}

	/**
	 * Decode a base64-encoded SHA-1 hash value into a lowercase hex checksum.
	 *
	 * @param hashValue Base64-encoded SHA-1 hash value
	 * @return Lowercase hex checksum, or <code>null</code> if the value is not a valid SHA-1 hash.
	 */
	private static String decodeChecksum(String hashValue) {
		byte[] bytes;
		try {
			bytes = Base64.getDecoder().decode(hashValue);
		} catch (IllegalArgumentException e) {
			return null;
		}
		if (bytes.length != 20) {
			return null; // not a SHA-1 hash (20 bytes)
		}
		StringBuilder result = new StringBuilder(bytes.length * 2);
		for (byte value : bytes) {
			result.append(Character.forDigit((value >> 4) & 0xF, 16));
			result.append(Character.forDigit((value & 0xF), 16));
		}
		return result.toString();
	}

	public static Map<String, String> queryToMap(URI uri) {
		// use the raw (still percent-encoded) query, so that base64 values are decoded
		// exactly once below; otherwise a '+' in the base64 hash value would be turned
		// into a space (URI.getQuery() already decodes the query component).
		String query = uri.getRawQuery();
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
