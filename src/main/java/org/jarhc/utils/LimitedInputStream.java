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

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public class LimitedInputStream extends FilterInputStream {

	private final long maxLength;
	private long length = 0;

	public LimitedInputStream(InputStream in, long maxLength) {
		super(in);
		if (in == null) throw new IllegalArgumentException("in == null");
		if (maxLength < 0) throw new IllegalArgumentException("maxLength: " + maxLength + " < 0");
		this.maxLength = maxLength;
	}

	@Override
	public int read() throws IOException {
		int result = super.read();
		if (result > -1) {
			countBytes(1);
		}
		return result;
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		int result = super.read(b, off, len);
		if (result > -1) {
			countBytes(result);
		}
		return result;
	}

	private void countBytes(int bytes) throws IOException {
		length += bytes;
		if (length > maxLength) {
			throw new IOException("Maximum length of stream exceeded.");
		}
	}

	@Override
	public boolean markSupported() {
		return false;
	}

}
