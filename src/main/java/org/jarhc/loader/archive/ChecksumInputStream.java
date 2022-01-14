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

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import org.jarhc.utils.DigestUtils;

/**
 * Filter input stream used to calculate a SHA1 checksum on the input bytes and
 * get the total number of bytes read from the input stream.
 */
class ChecksumInputStream extends FilterInputStream {

	private final MessageDigest digest = DigestUtils.getDigest();

	private long size = 0;

	ChecksumInputStream(InputStream inputStream) {
		super(inputStream);
	}

	long getSize() throws IOException {
		readRemaining();
		return size;
	}

	String getChecksum() throws IOException {
		readRemaining();
		byte[] bytes = this.digest.digest();
		return DigestUtils.hex(bytes);
	}

	@Override
	public int read() throws IOException {
		int value = super.read();
		if (value >= 0) {
			digest.update((byte) value);
			size += 1;
		}
		return value;
	}

	@Override
	public int read(byte[] buffer, int off, int len) throws IOException {
		int bytes = super.read(buffer, off, len);
		if (bytes > 0) {
			digest.update(buffer, off, bytes);
			size += bytes;
		}
		return bytes;
	}

	@Override
	public void close() throws IOException {
		readRemaining();
		super.close();
	}

	/**
	 * Read all remaining bytes before so that checksum and size are accurate.
	 */
	private void readRemaining() throws IOException {
		byte[] buffer = new byte[1024];
		while (true) {
			int bytes = read(buffer, 0, buffer.length);
			if (bytes < 0) break;
		}
	}

}
