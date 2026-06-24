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

package org.jarhc.artifacts;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Shared settings for the HTTP clients accessing the
 * <a href="https://deps.dev">deps.dev</a> API.
 * <p>
 * All deps.dev endpoints are derived from a single base URL
 * (system property {@code jarhc.depsdev.url}); the individual endpoint paths are
 * appended by the respective client.
 */
public class DepsDevSettings {

	private static final String DEFAULT_BASE_URL = "https://api.deps.dev/v3";
	private static final int DEFAULT_TIMEOUT = 30;
	private static final String DEFAULT_USER_AGENT = "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:129.0) Gecko/20100101 Firefox/129.0";

	private final String baseUrl;
	private final int timeout;
	private final Map<String, String> headers;

	/**
	 * Read settings from Java System Properties.
	 *
	 * @return Settings loaded from Java System Properties.
	 */
	public static DepsDevSettings fromSystemProperties() {
		return fromProperties(System.getProperties());
	}

	/**
	 * Read settings from Java properties.
	 *
	 * @param properties Java properties
	 * @return Settings loaded from Java properties.
	 */
	public static DepsDevSettings fromProperties(Properties properties) {

		// base URL of the deps.dev API (a trailing slash is removed so that
		// endpoint paths can be appended safely)
		String baseUrl = properties.getProperty("jarhc.depsdev.url", DEFAULT_BASE_URL);
		while (baseUrl.endsWith("/")) {
			baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
		}

		int timeout = Integer.parseInt(properties.getProperty("jarhc.depsdev.timeout", String.valueOf(DEFAULT_TIMEOUT)));

		Map<String, String> headers = new HashMap<>();
		headers.put("User-Agent", properties.getProperty("jarhc.depsdev.headers.User-Agent", DEFAULT_USER_AGENT));

		// read additional HTTP headers from properties
		for (String propertyName : properties.stringPropertyNames()) {
			if (propertyName.startsWith("jarhc.depsdev.headers.")) {
				String headerName = propertyName.substring("jarhc.depsdev.headers.".length());
				String headerValue = properties.getProperty(propertyName);
				headers.put(headerName, headerValue);
			}
		}

		return new DepsDevSettings(baseUrl, timeout, headers);
	}

	public DepsDevSettings(String baseUrl, int timeout, Map<String, String> headers) {
		this.baseUrl = baseUrl;
		this.timeout = timeout;
		this.headers = headers;
	}

	/**
	 * Get the base URL of the deps.dev API, without a trailing slash (for
	 * example, {@code https://api.deps.dev/v3}).
	 *
	 * @return Base URL.
	 */
	public String getBaseUrl() {
		return baseUrl;
	}

	public int getTimeout() {
		return timeout;
	}

	public Map<String, String> getHeaders() {
		return headers;
	}

}
