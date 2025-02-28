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

import java.io.IOException;
import org.jarhc.utils.ResourceUtils;
import org.slf4j.Logger;

/**
 * Default implementation of {@link StyleProvider} loading
 * CSS styles and JavaScript code from a resource on the classpath.
 */
class DefaultStyleProvider implements StyleProvider {

	private static final String STYLE_RESOURCE = "/html-report-style.css";
	private static final String SCRIPT_RESOURCE = "/html-report-script.js";

	private final Logger logger;
	private final String style;
	private final String script;

	DefaultStyleProvider(Logger logger) {
		this.logger = logger;

		// load CSS styles from resource
		String css = null;
		try {
			css = ResourceUtils.getResourceAsString(STYLE_RESOURCE, "UTF-8");
		} catch (IOException e) {
			this.logger.warn("Failed to load default style: {}", STYLE_RESOURCE, e);
		}

		if (css != null) {
			// remove multi-line comments (copyright header)
			css = css.replaceAll("/\\*[\\s\\S]*?\\*/", "").trim();
		}

		// load JavaScript code from resource
		String js = null;
		try {
			js = ResourceUtils.getResourceAsString("/html-report-script.js", "UTF-8");
		} catch (IOException e) {
			this.logger.warn("Failed to load default script: {}", SCRIPT_RESOURCE, e);
		}

		if (js != null) {
			// remove multi-line comments (copyright header)
			js = js.replaceAll("/\\*[\\s\\S]*?\\*/", "").trim();
		}

		this.style = css;
		this.script = js;
	}

	@Override
	public String getStyle() {
		return style;
	}

	@Override
	public String getScript() {
		return script;
	}

}
