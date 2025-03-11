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
import java.util.Optional;
import org.jarhc.test.server.HttpException;
import org.jarhc.test.server.ServerMode;

public class RepoHandler implements HttpHandler {

	private final ServerMode mode;
	private final LocalRepoClient localRepoClient;
	private final RemoteRepoClient remoteRepoClient;

	public RepoHandler(ServerMode mode, int timeout) {
		this.mode = mode;
		localRepoClient = new LocalRepoClient();
		remoteRepoClient = new RemoteRepoClient(timeout);
	}

	@Override
	public void handle(HttpExchange exchange) throws IOException {

		String path = exchange.getRequestURI().getPath();
		if (path.startsWith("/repo/")) {
			path = path.substring(6);
		} else {
			exchange.sendResponseHeaders(404, -1);
			return;
		}

		if (mode != ServerMode.RemoteOnly) {

			Optional<byte[]> response = localRepoClient.get(path);

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
				response = remoteRepoClient.get(path);
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
				localRepoClient.put(path, data);
			}

		}
	}

}
