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

package org.jarhc.utils;

public class ByteBuffer {

	private final ByteStream stream;

	ByteBuffer(ByteStream stream) {
		this.stream = stream;
	}

	public byte[] getBytes() {
		return stream.getBuf();
	}

	public int getLength() {
		return stream.getCount();
	}

	public void release() {
		IOUtils.returnToPool(stream);
	}

}
