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

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.Optional;
import org.jarhc.test.server.HttpException;
import org.jarhc.test.server.ServerMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles the deps.dev "GetVersion" endpoint
 * (<code>/systems/MAVEN/packages/{groupId:artifactId}/versions/{version}</code>),
 * returning the advisory keys affecting the given Maven version.
 */
public class SystemsHandler implements HttpHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(SystemsHandler.class);

	private static final String CONTEXT = "/systems/MAVEN/packages/";

	private final ServerMode mode;
	private final LocalSystemsClient localClient;
	private final RemoteSystemsClient remoteClient;

	public SystemsHandler(ServerMode mode, int timeout, java.nio.file.Path rootPath) {
		this.mode = mode;
		this.localClient = new LocalSystemsClient(rootPath);
		this.remoteClient = new RemoteSystemsClient(timeout);
	}

	@Override
	public void handle(HttpExchange exchange) throws IOException {
		String rawPath = exchange.getRequestURI().getRawPath();
		LOGGER.trace("Request: {}", rawPath);

		// expected path: /systems/MAVEN/packages/<url-encoded "groupId:artifactId">/versions/<version>
		if (!rawPath.startsWith(CONTEXT)) {
			sendNotFound(exchange);
			return;
		}
		String[] segments = rawPath.substring(CONTEXT.length()).split("/");
		if (segments.length != 3 || !segments[1].equals("versions")) {
			sendNotFound(exchange);
			return;
		}

		String name = URLDecoder.decode(segments[0], UTF_8);
		String version = URLDecoder.decode(segments[2], UTF_8);

		int index = name.indexOf(':');
		if (index < 0) {
			sendNotFound(exchange);
			return;
		}
		String groupId = name.substring(0, index);
		String artifactId = name.substring(index + 1);
		if (isInvalid(groupId) || isInvalid(artifactId) || isInvalid(version)) {
			sendNotFound(exchange);
			return;
		}

		if (mode != ServerMode.RemoteOnly) {
			Optional<byte[]> response = localClient.get(groupId, artifactId, version);
			if (response.isPresent()) {
				sendData(exchange, response.get());
				return;
			}
			if (mode == ServerMode.LocalOnly) {
				sendNotFound(exchange);
				return;
			}
		}

		Optional<byte[]> response;
		try {
			response = remoteClient.get(name, version);
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
			localClient.put(groupId, artifactId, version, data);
		}
	}

	private static boolean isInvalid(String value) {
		return value.isEmpty() || value.contains("/") || value.contains("\\") || value.equals("..");
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
