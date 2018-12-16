/*
 * Copyright 2018 Stephan Markwalder
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

package org.jarhc.report.html;

import org.jarhc.utils.ResourceUtils;

import java.io.IOException;

/**
 * Default implementation of {@link StyleProvider} loading
 * CSS styles from a resource on the classpath.
 */
class DefaultStyleProvider implements StyleProvider {

	private static final String RESOURCE = "/html-report-style.css";

	private final String style;

	DefaultStyleProvider() {

		// load CSS styles from resource
		String css = null;
		try {
			css = ResourceUtils.getResourceAsString(RESOURCE, "UTF-8");
		} catch (IOException e) {
			System.err.println("Failed to load default style: " + RESOURCE);
			e.printStackTrace();
		}
		this.style = css;
	}

	@Override
	public String getStyle() {
		return style;
	}

	/*
	private static String minify(String css) {
		css = css.replaceAll("[\r\n]+", "");
		css = css.replaceAll("\\s+", " ");
		css = css.replaceAll("([{:;]) ", "$1");
		css = css.replaceAll("}", "}" + System.lineSeparator());
		return css;
	}
	*/

}
