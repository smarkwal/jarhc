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

package org.jarhc.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class StringUtils {

	private StringUtils() {
		throw new IllegalStateException("utility class");
	}

	public static String repeat(String text, int count) {
		if (count < 0) throw new IllegalArgumentException("count");
		if (count == 0 || text == null) return "";
		if (count == 1) return text;
		StringBuilder buffer = new StringBuilder(text.length() * count);
		while (count > 0) {
			buffer.append(text);
			count--;
		}
		return buffer.toString();
	}

	public static String joinLines(Collection<String> lines) {
		return String.join(System.lineSeparator(), lines);
	}

	public static String joinLines(String... lines) {
		return String.join(System.lineSeparator(), lines);
	}

	public static Collector<CharSequence, ?, String> joinLines() {
		return Collectors.joining(System.lineSeparator());
	}

	/**
	 * Split a longer text using commas as separators.
	 *
	 * @param text      Text to split.
	 * @param maxLength Maximum length of a line (best-effort).
	 * @return List of lines.
	 */
	public static List<String> splitText(String text, int maxLength) {
		List<String> lines = new ArrayList<>();

		while (text.length() > maxLength) {
			int pos = text.lastIndexOf(',', maxLength);
			if (pos < 0) {
				pos = text.indexOf(',');
				if (pos < 0) {
					break;
				}
			}
			String line = text.substring(0, pos + 1);
			lines.add(line.trim());
			text = text.substring(pos + 1).trim();
		}

		lines.add(text);
		return lines;
	}

	/**
	 * Wrap a text into multiple lines.
	 *
	 * @param text      Text to wrap.
	 * @param maxLength Maximum length of a line (best-effort).
	 * @return Wrapped text.
	 */
	public static String wrapText(String text, int maxLength) {
		return joinLines(splitText(text, maxLength));
	}

}
