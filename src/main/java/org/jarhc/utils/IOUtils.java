/*
 * Copyright 2019 Stephan Markwalder
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class IOUtils {

	private static final int INITIAL_BUFFER_SIZE = 256 * 1024;
	private static final int MAX_BUFFER_SIZE = 4 * 1024 * 1024;

	/**
	 * Thread-local cached stream. Same thread always uses same buffer for performance reasons.
	 */
	private static final ThreadLocal<ByteArrayOutputStream> cachedStream = ThreadLocal.withInitial(() -> new ByteArrayOutputStream(INITIAL_BUFFER_SIZE));

	private IOUtils() {
		throw new IllegalStateException("utility class");
	}

	public static byte[] toByteArray(InputStream stream) throws IOException {
		if (stream == null) throw new IllegalArgumentException("stream");
		ByteArrayOutputStream result = toByteArrayOutputStream(stream);
		byte[] bytes = result.toByteArray();
		if (result.size() > MAX_BUFFER_SIZE) {
			cachedStream.remove();
		}
		return bytes;
	}

	public static String toString(InputStream stream) throws IOException {
		if (stream == null) throw new IllegalArgumentException("stream");
		ByteArrayOutputStream result = toByteArrayOutputStream(stream);
		return result.toString(StandardCharsets.UTF_8.name());
	}

	private static ByteArrayOutputStream toByteArrayOutputStream(InputStream stream) throws IOException {
		ByteArrayOutputStream result = cachedStream.get();
		result.reset();
		copy(stream, result);
		return result;
	}

	protected static void copy(InputStream in, OutputStream out) throws IOException {
		byte[] buffer = new byte[1024];
		while (true) {
			int len = in.read(buffer);
			if (len < 0) break;
			out.write(buffer, 0, len);
		}
	}

}
