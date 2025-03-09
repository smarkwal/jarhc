/*
 * Copyright 2024 Stephan Markwalder
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

package org.jarhc.it.utils;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import org.jarhc.TestUtils;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MavenSearchApiMockServer implements BeforeAllCallback, AfterAllCallback {

	private static final Logger LOGGER = LoggerFactory.getLogger(MavenSearchApiMockServer.class);

	private HttpServer httpServer;
	private String originalSearchUrl;

	@Override
	public void beforeAll(ExtensionContext context) throws Exception {
		if (httpServer != null) {
			throw new IllegalStateException("Server is already running.");
		}

		// start HTTP server on random port
		httpServer = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
		httpServer.createContext("/checksum", new RequestHandler());
		httpServer.setExecutor(null); // use default executor
		httpServer.start();

		// get selected port
		int port = httpServer.getAddress().getPort();

		// update search URL in Java System Properties
		originalSearchUrl = System.getProperty("jarhc.search.url");
		String searchUrl = "http://localhost:" + port + "/checksum?q=%s";
		System.setProperty("jarhc.search.url", searchUrl);
	}

	@Override
	public void afterAll(ExtensionContext context) {

		// stop HTTP server
		if (httpServer != null) {
			httpServer.stop(0);
			httpServer = null;
		}

		// restore original search URL in Java System Properties
		if (originalSearchUrl != null) {
			System.setProperty("jarhc.search.url", originalSearchUrl);
		} else {
			System.clearProperty("jarhc.search.url");
		}
	}

	private static class RequestHandler implements HttpHandler {

		public void handle(HttpExchange exchange) throws IOException {

			// validate query string
			String query = exchange.getRequestURI().getQuery();
			if (query == null || !query.startsWith("q=")) {
				LOGGER.error("Invalid query: '{}'", query);
				exchange.sendResponseHeaders(400, 0);
				exchange.getResponseBody().close();
				return;
			}

			// get checksum from query string
			String checksum = query.substring(2);

			LOGGER.debug("Mock Maven Search API response for checksum: '{}'", checksum);

			// load response from test resources
			String response = TestUtils.getResourceAsString("/maven-search-api-responses/" + checksum + ".json", "UTF-8");

			// send response headers
			Headers headers = exchange.getResponseHeaders();
			headers.set("Content-Type", "application/json");
			exchange.sendResponseHeaders(200, response.length());

			// send response body
			OutputStream stream = exchange.getResponseBody();
			stream.write(response.getBytes());
			stream.close();
		}
	}

}
