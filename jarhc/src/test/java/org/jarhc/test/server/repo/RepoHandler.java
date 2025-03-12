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

package org.jarhc.test.server.repo;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.jarhc.test.server.HttpException;
import org.jarhc.test.server.ServerMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RepoHandler implements HttpHandler {

	private final Logger LOGGER = LoggerFactory.getLogger(RepoHandler.class);

	private static final Map<String, String> contentTypes = new HashMap<>();

	static {
		contentTypes.put("jar", "application/java-archive");
		contentTypes.put("war", "application/java-archive");
		contentTypes.put("pom", "application/xml");
		contentTypes.put("json", "application/json");
		contentTypes.put("xml", "application/xml");
		contentTypes.put("txt", "text/plain");
		contentTypes.put("asc", "text/plain");
		contentTypes.put("md5", "text/plain");
		contentTypes.put("sha1", "text/plain");
		contentTypes.put("sha256", "text/plain");
		contentTypes.put("sha512", "text/plain");
	}

	private final ServerMode mode;
	private final LocalRepoClient localRepoClient;
	private final RemoteRepoClient remoteRepoClient;

	public RepoHandler(ServerMode mode, int timeout, Path rootPath) {
		this.mode = mode;
		localRepoClient = new LocalRepoClient(rootPath);
		remoteRepoClient = new RemoteRepoClient(timeout);
	}

	@Override
	public void handle(HttpExchange exchange) throws IOException {
		URI uri = exchange.getRequestURI();
		LOGGER.debug("Request: {}", uri);

		String path = uri.getPath();
		if (path.startsWith("/repo/")) {
			path = path.substring(6);
		} else {
			sendNotFound(exchange);
			return;
		}

		if (path.endsWith("/")) {
			sendBadRequest(exchange);
			return;
		}

		String contentType = getContentType(path);

		if (mode != ServerMode.RemoteOnly) {

			Optional<byte[]> response = localRepoClient.get(path);

			if (response.isPresent()) {
				byte[] data = response.get();
				sendData(exchange, contentType, data);
				return;
			}
		}

		if (mode != ServerMode.LocalOnly) {

			Optional<byte[]> response;
			try {
				response = remoteRepoClient.get(path);
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
			sendData(exchange, contentType, data);

			if (mode == ServerMode.LocalRemoteUpdate) {
				localRepoClient.put(path, data);
			}

		}
	}

	private static void sendNotFound(HttpExchange exchange) throws IOException {
		exchange.sendResponseHeaders(404, -1);
		exchange.close();
	}

	private static void sendBadRequest(HttpExchange exchange) throws IOException {
		exchange.sendResponseHeaders(400, -1);
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

	private static String getContentType(String path) {
		int idx = path.lastIndexOf('.');
		if (idx < 0) return null;
		String extension = path.substring(idx + 1);
		return contentTypes.get(extension);
	}

	private static void sendData(HttpExchange exchange, String contentType, byte[] data) throws IOException {
		if (contentType != null) {
			exchange.getResponseHeaders().add("Content-Type", contentType);
		}
		exchange.sendResponseHeaders(200, data.length);
		exchange.getResponseBody().write(data);
		exchange.close();
	}

}
