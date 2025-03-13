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

import java.nio.file.Path;
import org.jarhc.test.server.MavenProxyServer;
import org.jarhc.test.server.ServerMode;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public class MavenProxyServerExtension implements BeforeAllCallback, AfterAllCallback {

	private MavenProxyServer server;
	private String originalSearchUrl;
	private String originalRepositoryUrl;

	@Override
	public void beforeAll(ExtensionContext context) throws Exception {
		if (server != null) {
			throw new IllegalStateException("Server is already running.");
		}

		Path proxyPath = Path.of("src/test/resources/maven-proxy-server");
		server = new MavenProxyServer(ServerMode.LocalOnly, 10, proxyPath);
		server.start();

		// update search URL in Java System Properties
		originalSearchUrl = System.getProperty("jarhc.search.url");
		String searchUrl = server.getSearchURL();
		System.setProperty("jarhc.search.url", searchUrl);

		// update repository URL in Java System Properties
		originalRepositoryUrl = System.getProperty("jarhc.repository.url");
		String repositoryUrl = server.getRepoURL();
		System.setProperty("jarhc.repository.url", repositoryUrl);
	}

	@Override
	public void afterAll(ExtensionContext context) {

		// stop server
		if (server != null) {
			server.stop();
			server = null;
		}

		// restore original search URL in Java System Properties
		if (originalSearchUrl != null) {
			System.setProperty("jarhc.search.url", originalSearchUrl);
		} else {
			System.clearProperty("jarhc.search.url");
		}

		// restore original repository URL in Java System Properties
		if (originalRepositoryUrl != null) {
			System.setProperty("jarhc.repository.url", originalRepositoryUrl);
		} else {
			System.clearProperty("jarhc.repository.url");
		}
	}

}
