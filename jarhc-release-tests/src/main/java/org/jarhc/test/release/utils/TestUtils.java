/*
 * Copyright 2022 Stephan Markwalder
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

package org.jarhc.test.release.utils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import org.apache.commons.io.FileUtils;
import org.assertj.core.util.diff.Delta;
import org.assertj.core.util.diff.DiffUtils;
import org.assertj.core.util.diff.Patch;

public class TestUtils {

	public static String getJavaVersion() {
		String value = System.getProperty("java.version");
		String[] parts = value.split("\\.");
		if (parts[0].equals("1")) {
			// Java 1.5 - 1.8
			return parts[1];
		} else {
			// Java 9+
			return parts[0];
		}
	}

	public static String getJavaHome() {
		return System.getProperty("java.home");
	}

	/**
	 * Checks if the system property "jarhc.test.resources.generate" is set.
	 * This property is used as flag to instruct tests to re-generate their test resources.
	 *
	 * @return <code>true</code> if test resources should be generated.
	 */
	public static boolean createResources() {
		Properties properties = System.getProperties();
		return properties.containsKey("jarhc.test.resources.generate");
	}

	public static void normalizeReport(File file) {

		if (file.isFile() && file.canRead()) {
			try {

				// read content of file
				String content = FileUtils.readFileToString(file, StandardCharsets.UTF_8);

				// mask checksum of JarHC JAR file
				content = content.replaceAll("jarhc(.*) \\| [0-9a-f]{40} \\|", "jarhc$1 | **************************************** |");

				// write content back into file
				FileUtils.writeStringToFile(file, content, StandardCharsets.UTF_8);

			} catch (IOException e) {
				throw new AssertionError("Unexpected I/O error.", e);
			}
		}

	}

	public static String getDiff(String expected, String actual) {
		StringBuilder diff = new StringBuilder();
		appendDiff(diff, expected, actual);
		return diff.toString();
	}

	public static void appendDiff(StringBuilder buffer, String expected, String actual) {
		List<String> lines1 = splitLines(expected);
		List<String> lines2 = splitLines(actual);
		Patch<String> patch = DiffUtils.diff(lines1, lines2);
		for (Delta<String> delta : patch.getDeltas()) {
			buffer.append(delta).append("\n");
		}
	}

	private static List<String> splitLines(String text) {
		String[] lines = text.split("\\r?\\n");
		return Arrays.asList(lines);
	}

}
