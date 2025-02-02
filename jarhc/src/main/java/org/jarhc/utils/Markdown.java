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

package org.jarhc.utils;

import java.util.regex.Pattern;

public class Markdown {

	private static final Pattern CODE = Pattern.compile("`([^`]+)`");

	public static String code(String text) {
		if (text == null || text.isEmpty()) return text;
		return "`" + text + "`";
	}

	public static String toText(String text) {
		if (text == null || text.isEmpty()) return text;
		return text.replace("`", "");
	}

	public static String toHtml(String text) {
		if (text == null || text.isEmpty()) return text;
		return CODE.matcher(text).replaceAll("<code>$1</code>");
	}

}
