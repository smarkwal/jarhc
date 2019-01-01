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

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;

public class FileUtils {

	public static String formatFileSize(long fileSize) {
		if (fileSize < 0) throw new IllegalArgumentException("fileSize");
		if (fileSize < 1024) {
			return String.format("%d B", fileSize);
		}
		double size = fileSize / 1024d;
		if (size < 1024) {
			return String.format("%s KB", formatNumber(size));
		}
		size = size / 1024d;
		return String.format("%s MB", formatNumber(size));
	}

	private static String formatNumber(double number) {
		DecimalFormat format;
		if (number < 10) {
			format = new DecimalFormat("0.00");
		} else if (number < 100) {
			format = new DecimalFormat("0.0");
		} else {
			format = new DecimalFormat("0");
		}
		return format.format(number);
	}

	public static String sha1Hex(File file) throws IOException {
		try (FileInputStream stream = new FileInputStream(file)) {
			return DigestUtils.sha1Hex(stream);
		}
	}

	public static String readFileToString(File file) throws IOException {
		try (FileInputStream stream = new FileInputStream(file)) {
			try (InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
				StringBuilder result = new StringBuilder((int) file.length());
				char[] buffer = new char[1024];
				while (true) {
					int len = reader.read(buffer);
					if (len < 0) break;
					result.append(buffer, 0, len);
				}
				return result.toString();
			}
		}
	}

	public static void writeStringToFile(String text, File file) throws IOException {
		// create parent directories
		file.getParentFile().mkdirs();
		// write text to file using UTF-8
		try (FileOutputStream stream = new FileOutputStream(file)) {
			try (OutputStreamWriter writer = new OutputStreamWriter(stream, StandardCharsets.UTF_8)) {
				writer.write(text);
			}
		}
	}

	public static void touchFile(File file) throws IOException {
		// if file does not exist ...
		if (!file.exists()) {
			// create parent directories
			file.getParentFile().mkdirs();
			// create empty file
			file.createNewFile();
		}
		// set modification time to now
		file.setLastModified(System.currentTimeMillis());
	}

}
