/*
 * Copyright 2021 Stephan Markwalder
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class LimitedInputStreamTest {

	private static final byte[] data = "Hello World!".getBytes(StandardCharsets.UTF_8);

	// input stream with 12 bytes of data
	private final InputStream in = new ByteArrayInputStream(data);

	@Test
	void read() throws IOException {

		// prepare: limited to 8 bytes
		LimitedInputStream stream = new LimitedInputStream(in, 16);

		// test: can read 12 bytes
		for (int i = 0; i < 12; i++) {
			int result = stream.read();
			assertEquals(data[i], result);
		}

		// returns -1 on 13th byte
		int result = stream.read();
		assertEquals(-1, result);
	}

	@Test
	void read_throwsIOException_ifLimitIsExceeded() throws IOException {

		// prepare: limited to 8 bytes
		LimitedInputStream stream = new LimitedInputStream(in, 8);

		// test: can read 8 bytes
		for (int i = 0; i < 8; i++) {
			int result = stream.read();
			assertEquals(data[i], result);
		}

		// assert: throws IOException on 9th byte
		assertThrows(IOException.class, stream::read);
	}

	@Test
	void read_buffer() throws IOException {

		// prepare: limited to 8 bytes
		LimitedInputStream stream = new LimitedInputStream(in, 16);

		// test: can read all bytes
		byte[] buffer = new byte[16];
		int result = stream.read(buffer);

		// assert: 12 bytes read
		assertEquals(12, result);
	}

	@Test
	void read_buffer_throwsIOException_ifLimitIsExceeded() throws IOException {

		// prepare: limited to 8 bytes
		LimitedInputStream stream = new LimitedInputStream(in, 8);

		// test: can read 2 x 4 bytes
		byte[] buffer = new byte[4];
		for (int i = 0; i < 2; i++) {
			int result = stream.read(buffer);
			assertEquals(4, result);
		}

		// assert: throws IOException on 9th byte
		assertThrows(IOException.class, () -> stream.read(buffer));
	}

	@Test
	void read_buffer_off_len() throws IOException {

		// prepare: limited to 8 bytes
		LimitedInputStream stream = new LimitedInputStream(in, 16);

		// test: can read all bytes
		byte[] buffer = new byte[16];
		int result = stream.read(buffer, 0, 16);

		// assert: 12 bytes read
		assertEquals(12, result);
	}

	@Test
	void read_buffer_off_len_throwsIOException_ifLimitIsExceeded() throws IOException {

		// prepare: limited to 8 bytes
		LimitedInputStream stream = new LimitedInputStream(in, 8);

		// test: can read 2 x 4 bytes
		byte[] buffer = new byte[4];
		for (int i = 0; i < 2; i++) {
			int result = stream.read(buffer, 0, 4);
			assertEquals(4, result);
		}

		// assert: throws IOException on 9th byte
		assertThrows(IOException.class, () -> stream.read(buffer, 0, 4));
	}

	@Test
	void markSupported() {

		// prepare
		LimitedInputStream stream = new LimitedInputStream(in, 1000);

		// test
		boolean result = stream.markSupported();

		// assert
		assertFalse(result);

	}

}