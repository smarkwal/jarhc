/*
 * Copyright 2025 Stephan Markwalder
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class CompressUtils {

	private CompressUtils() {
		throw new IllegalStateException("utility class");
	}

	public static String compressString(String text) {
		if (text == null) throw new IllegalArgumentException("text");

		// convert string to bytes
		byte[] data = text.getBytes(StandardCharsets.UTF_8);

		// compress bytes
		data = compress(data);

		// Base64-encode compressed data
		return Base64.getEncoder().encodeToString(data);
	}

	public static String decompressString(String text) {
		if (text == null) throw new IllegalArgumentException("text");

		// Base64-decode compressed data
		byte[] data = Base64.getDecoder().decode(text);

		// decompress data
		data = decompress(data);

		// convert bytes to string
		return new String(data, StandardCharsets.UTF_8);
	}

	private static byte[] compress(byte[] data) {
		ByteArrayOutputStream out = new ByteArrayOutputStream(data.length / 4);
		try (GZIPOutputStream gzip = new GZIPOutputStream(out)) {
			gzip.write(data);
		} catch (IOException e) {
			throw new JarHcException(e);
		}
		return out.toByteArray();
	}

	private static byte[] decompress(byte[] data) {
		ByteArrayInputStream in = new ByteArrayInputStream(data);
		try (GZIPInputStream gzip = new GZIPInputStream(in)) {
			data = IOUtils.toByteArray(gzip);
		} catch (IOException e) {
			throw new JarHcException(e);
		}
		return data;
	}

}
