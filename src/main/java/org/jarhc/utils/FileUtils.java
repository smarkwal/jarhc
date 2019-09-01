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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;

public class FileUtils {

	private FileUtils() {
		throw new IllegalStateException("utility class");
	}

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

	public static byte[] readFileToByteArray(File file) throws IOException {
		try (FileInputStream stream = new FileInputStream(file)) {
			return IOUtils.toByteArray(stream);
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

	public static void writeStreamToFile(InputStream stream, File file) throws IOException {
		// create parent directories
		file.getParentFile().mkdirs();
		// copy data from stream to file
		try (FileOutputStream out = new FileOutputStream(file)) {
			IOUtils.copy(stream, out);
		}
	}

	/**
	 * Make sure that the given file exists and set its "last modified" time
	 * stamp to the current time.
	 *
	 * @param file File
	 * @throws IOException If the file does not exist and can not be created,
	 *                     or if the last modified time stamp can not be set.
	 */
	public static void touchFile(File file) throws IOException {
		if (file == null) throw new IllegalArgumentException("file == null");

		// if file does not exist ...
		if (!file.exists()) {

			// create parent directories
			File directory = file.getParentFile();
			if (!directory.exists()) {
				boolean created = directory.mkdirs();
				if (!created) throw new IOException("Unable to create directory: " + directory.getAbsolutePath());
			}

			// create empty file
			boolean created = file.createNewFile();
			if (!created) throw new IOException("Unable to create file: " + file.getAbsolutePath());
		}

		// set modification time to now
		boolean modified = file.setLastModified(System.currentTimeMillis());
		if (!modified) throw new IOException("Unable to set modification time of file: " + file.getAbsolutePath());
	}

	public static String getFilename(String path) {
		int pos = path.lastIndexOf("/");
		if (pos < 0) return path;
		return path.substring(pos + 1);
	}

	/**
	 * Compare (sort) files and directories by name (case-insensitive).
	 *
	 * @param file1 First file
	 * @param file2 Second file
	 * @return A negative value if the first file should appear before the
	 * second file, a positive value if the first file should appear after
	 * the second file, and zero if both files have the exact same name.
	 */
	public static int compareByName(File file1, File file2) {
		int diff = file1.getName().compareToIgnoreCase(file2.getName());
		if (diff == 0) {
			// if two files have the same case-insensitive name,
			// perform a case-sensitive comparison.
			diff = file1.getName().compareTo(file2.getName());
		}
		return diff;
	}

}
