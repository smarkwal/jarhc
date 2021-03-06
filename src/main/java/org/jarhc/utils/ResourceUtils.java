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

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class ResourceUtils {

	private ResourceUtils() {
		throw new IllegalStateException("utility class");
	}

	public static InputStream getResourceAsStream(String resource) throws IOException {
		if (resource == null) throw new IllegalArgumentException("resource");
		InputStream stream = ResourceUtils.class.getResourceAsStream(resource);
		if (stream == null) throw new IOException("Resource not found: " + resource);
		return stream;
	}

	public static String getResourceAsString(String resource, String encoding) throws IOException {
		if (resource == null) throw new IllegalArgumentException("resource");
		InputStream stream = getResourceAsStream(resource);
		ByteArrayOutputStream result = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		while (true) {
			int len = stream.read(buffer);
			if (len < 0) break;
			result.write(buffer, 0, len);
		}
		return result.toString(encoding);
	}

	public static List<String> getResourceAsLines(String resource, String encoding) throws IOException {
		List<String> lines = new ArrayList<>();
		try (InputStream stream = getResourceAsStream(resource)) {
			BufferedReader reader = new BufferedReader(new InputStreamReader(stream, encoding));
			while (true) {
				String line = reader.readLine();
				if (line == null) break;
				lines.add(line);
			}
		}
		return lines;
	}

}
