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

package org.jarhc.test.server;

import com.sun.net.httpserver.HttpServer;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import org.jarhc.test.server.repo.RepoHandler;
import org.jarhc.test.server.search.SearchHandler;

public class MavenProxyServer {

	private final ServerMode mode;
	private final int port;
	private final int timeout;
	private final Path rootPath;

	private HttpServer server;

	public static void main(String[] args) throws IOException {

		// TODO: parse arguments to get mode, port and timeout
		// TODO: support setting address

		Path rootPath = Path.of("jarhc/src/test/resources/proxy");
		MavenProxyServer server = new MavenProxyServer(ServerMode.LocalRemoteUpdate, 8080, 10, rootPath);

		System.out.println("Starting server...");
		server.start();
		System.out.println("Server started.");

		System.out.println("Server URLs:");
		System.out.println("- " + server.getSearchURL());
		System.out.println("- " + server.getRepoURL());

		System.out.println("Type 'exit' to stop the server.");
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		while (true) {
			System.out.print("> ");
			String command = reader.readLine();
			if (command.equals("exit")) {
				break;
			} else {
				System.out.println("Unknown command: " + command);
			}
		}

		System.out.println("Stopping server...");
		server.stop();
		System.out.println("Server stopped.");
	}

	public MavenProxyServer(ServerMode mode, int port, int timeout, Path rootPath) {
		if (mode == null) throw new IllegalArgumentException("mode");
		if (port < 0 || port > 65535) throw new IllegalArgumentException("port");
		if (timeout < 0) throw new IllegalArgumentException("timeout");
		if (rootPath == null) throw new IllegalArgumentException("rootPath");
		this.mode = mode;
		this.port = port;
		this.timeout = timeout;
		this.rootPath = rootPath.toAbsolutePath();
	}

	public String getBaseURL() {
		if (port == 80) {
			return "http://localhost";
		} else {
			return "http://localhost:" + port;
		}
	}

	public String getSearchURL() {
		return getBaseURL() + "/search?checksum=%s";
	}

	public String getRepoURL() {
		return getBaseURL() + "/repo/%s";
	}

	public void start() throws IOException {
		if (server != null) throw new IllegalStateException("Server already started.");

		// check if root path exists
		if (!Files.isDirectory(rootPath)) {
			throw new FileNotFoundException(rootPath.toString());
		}

		// create subdirectories (if not exist)
		Path searchPath = rootPath.resolve("search");
		Files.createDirectories(searchPath);
		Path repoPath = rootPath.resolve("repo");
		Files.createDirectories(repoPath);

		server = HttpServer.create(new InetSocketAddress(port), 0);
		server.createContext("/search", new SearchHandler(mode, timeout, searchPath));
		server.createContext("/repo", new RepoHandler(mode, timeout, repoPath));
		server.setExecutor(null); // creates a default executor
		server.start();
	}

	public void stop() {
		if (server == null) throw new IllegalStateException("Server not started.");
		server.stop(0);
		server = null;
	}

}
