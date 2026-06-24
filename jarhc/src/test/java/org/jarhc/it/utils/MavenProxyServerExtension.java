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
import org.jarhc.TestUtils;
import org.jarhc.test.server.MavenProxyServer;
import org.jarhc.test.server.ServerMode;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public class MavenProxyServerExtension implements BeforeAllCallback, AfterAllCallback {

	private MavenProxyServer server;
	private String originalDepsDevUrl;
	private String originalRepositoryUrl;

	@Override
	public void beforeAll(ExtensionContext context) throws Exception {
		if (server != null) {
			throw new IllegalStateException("Server is already running.");
		}

		ServerMode mode = TestUtils.createResources() ? ServerMode.LocalRemoteUpdate : ServerMode.LocalOnly;
		Path proxyPath = Path.of("src/test/resources/maven-proxy-server");
		server = new MavenProxyServer(mode, 10, proxyPath);
		server.start();

		// point the deps.dev API base URL at the mock server (one property covers
		// the query, versions and advisories endpoints)
		originalDepsDevUrl = System.getProperty("jarhc.depsdev.url");
		System.setProperty("jarhc.depsdev.url", server.getBaseURL());

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

		// restore original deps.dev base URL in Java System Properties
		restoreProperty("jarhc.depsdev.url", originalDepsDevUrl);

		// restore original repository URL in Java System Properties
		restoreProperty("jarhc.repository.url", originalRepositoryUrl);
	}

	private static void restoreProperty(String name, String originalValue) {
		if (originalValue != null) {
			System.setProperty(name, originalValue);
		} else {
			System.clearProperty(name);
		}
	}

}
