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

package org.jarhc.loader.archive;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Random;
import org.jarhc.utils.DigestUtils;
import org.jarhc.utils.IOUtils;
import org.junit.jupiter.api.Test;

class ChecksumInputStreamTest {

	@Test
	void test_read_all() throws IOException {

		// prepare
		Random random = new Random();
		byte[] data = new byte[128];
		random.nextBytes(data);

		// test
		ChecksumInputStream stream = new ChecksumInputStream(new ByteArrayInputStream(data));
		byte[] data2 = IOUtils.toByteArray(stream);

		// assert
		assertEquals(data.length, data2.length);
		assertEquals(data.length, stream.getSize());
		assertArrayEquals(data, data2);
		assertEquals(DigestUtils.sha1Hex(data), stream.getChecksum());
	}

	@Test
	void test_read_all_close() throws IOException {

		// prepare
		Random random = new Random();
		byte[] data = new byte[128];
		random.nextBytes(data);

		// test
		ChecksumInputStream stream = new ChecksumInputStream(new ByteArrayInputStream(data));
		byte[] data2 = IOUtils.toByteArray(stream);
		stream.close();

		// assert
		assertEquals(data.length, data2.length);
		assertEquals(data.length, stream.getSize());
		assertArrayEquals(data, data2);
		assertEquals(DigestUtils.sha1Hex(data), stream.getChecksum());
	}

	@Test
	void test_read_nothing() throws IOException {

		// prepare
		Random random = new Random();
		byte[] data = new byte[128];
		random.nextBytes(data);

		// test
		ChecksumInputStream stream = new ChecksumInputStream(new ByteArrayInputStream(data));

		// assert
		assertEquals(data.length, stream.getSize());
		assertEquals(DigestUtils.sha1Hex(data), stream.getChecksum());
	}

	@Test
	void test_read_nothing_close() throws IOException {

		// prepare
		Random random = new Random();
		byte[] data = new byte[128];
		random.nextBytes(data);

		// test
		ChecksumInputStream stream = new ChecksumInputStream(new ByteArrayInputStream(data));
		stream.close();

		// assert
		assertEquals(data.length, stream.getSize());
		assertEquals(DigestUtils.sha1Hex(data), stream.getChecksum());
	}

	@Test
	void test_read_single_bytes() throws IOException {

		// prepare
		Random random = new Random();
		byte[] data = new byte[128];
		random.nextBytes(data);

		ByteArrayOutputStream buffer = new ByteArrayOutputStream(data.length);

		// test
		ChecksumInputStream stream = new ChecksumInputStream(new ByteArrayInputStream(data));
		while (true) {
			int value = stream.read();
			if (value < 0) break;
			buffer.write(value);
		}
		byte[] data2 = buffer.toByteArray();

		// assert
		assertEquals(data.length, data2.length);
		assertEquals(data.length, stream.getSize());
		assertEquals(DigestUtils.sha1Hex(data), stream.getChecksum());
	}

}