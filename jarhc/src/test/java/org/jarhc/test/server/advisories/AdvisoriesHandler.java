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

package org.jarhc.test.server.advisories;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.file.Path;
import java.util.Optional;
import org.jarhc.test.server.HttpException;
import org.jarhc.test.server.ServerMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles the deps.dev "GetAdvisory" endpoint
 * (<code>/advisories/{advisoryId}</code>), returning the details of a security
 * advisory.
 */
public class AdvisoriesHandler implements HttpHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(AdvisoriesHandler.class);

	private static final String CONTEXT = "/advisories/";

	private final ServerMode mode;
	private final LocalAdvisoriesClient localClient;
	private final RemoteAdvisoriesClient remoteClient;

	public AdvisoriesHandler(ServerMode mode, int timeout, Path rootPath) {
		this.mode = mode;
		this.localClient = new LocalAdvisoriesClient(rootPath);
		this.remoteClient = new RemoteAdvisoriesClient(timeout);
	}

	@Override
	public void handle(HttpExchange exchange) throws IOException {
		String rawPath = exchange.getRequestURI().getRawPath();
		LOGGER.trace("Request: {}", rawPath);

		// expected path: /advisories/<advisoryId>
		if (!rawPath.startsWith(CONTEXT)) {
			sendNotFound(exchange);
			return;
		}
		String advisoryId = URLDecoder.decode(rawPath.substring(CONTEXT.length()), UTF_8);
		if (advisoryId.isEmpty() || advisoryId.contains("/")) {
			sendNotFound(exchange);
			return;
		}

		if (mode != ServerMode.RemoteOnly) {
			Optional<byte[]> response = localClient.get(advisoryId);
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
			response = remoteClient.get(advisoryId);
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
			localClient.put(advisoryId, data);
		}
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
