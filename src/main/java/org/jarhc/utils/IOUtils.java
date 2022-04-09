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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class IOUtils {

	private static final int INITIAL_BUFFER_SIZE = 256 * 1024;
	private static final int MAX_BUFFER_SIZE = 4 * 1024 * 1024;

	private static final Pool<ByteStream> BYTE_STREAM_POOL = new Pool<>(() -> new ByteStream(INITIAL_BUFFER_SIZE), ByteStream::reset);
	private static final Pool<byte[]> BYTE_ARRAY_POOL = new Pool<>(() -> new byte[4096]);

	private IOUtils() {
		throw new IllegalStateException("utility class");
	}

	public static ByteBuffer toByteBuffer(InputStream stream) throws IOException {
		if (stream == null) throw new IllegalArgumentException("stream");
		ByteStream out = toByteStream(stream);
		return new ByteBuffer(out);
	}

	static void returnToPool(ByteStream stream) {
		if (stream.getBuf().length <= IOUtils.MAX_BUFFER_SIZE) {
			BYTE_STREAM_POOL.doReturn(stream);
		}
	}

	public static byte[] toByteArray(InputStream stream) throws IOException {
		if (stream == null) throw new IllegalArgumentException("stream");
		ByteStream out = toByteStream(stream);
		byte[] bytes = out.toByteArray();
		returnToPool(out);
		return bytes;
	}

	private static ByteStream toByteStream(InputStream stream) throws IOException {
		ByteStream out = BYTE_STREAM_POOL.doBorrow();
		copy(stream, out);
		return out;
	}

	protected static void copy(InputStream in, OutputStream out) throws IOException {
		byte[] buffer = BYTE_ARRAY_POOL.doBorrow();
		while (true) {
			int len = in.read(buffer);
			if (len < 0) break;
			out.write(buffer, 0, len);
		}
		BYTE_ARRAY_POOL.doReturn(buffer);
	}

}
