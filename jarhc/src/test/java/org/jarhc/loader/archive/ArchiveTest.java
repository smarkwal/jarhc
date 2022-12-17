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

import static org.jarhc.loader.archive.Archive.MAX_ENTRY_COUNT;
import static org.jarhc.loader.archive.Archive.MAX_TOTAL_SIZE;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.junit.jupiter.api.Test;

class ArchiveTest {

	@Test
	void test_maxTotalSize() throws IOException {

		// prepare
		final byte[] data = new byte[1024 * 1024];
		Archive archive = new TestArchive() {
			@Override
			public ArchiveEntry getNextEntry() throws IOException {
				ByteArrayInputStream stream = new ByteArrayInputStream(data);
				loadData(stream);
				return null;
			}
		};

		// read N times without expecting a problem
		long N = MAX_TOTAL_SIZE / data.length;
		for (int i = 0; i < N; i++) {
			archive.getNextEntry();
		}

		// next read operation must fail
		assertThrows(IOException.class, archive::getNextEntry, "Maximum total size exceeded: " + MAX_TOTAL_SIZE);
	}

	@Test
	void test_maxNumberOfEntries() throws IOException {

		// prepare
		final byte[] data = new byte[16];
		Archive archive = new TestArchive() {
			@Override
			public ArchiveEntry getNextEntry() throws IOException {
				ByteArrayInputStream stream = new ByteArrayInputStream(data);
				loadData(stream);
				return null;
			}
		};

		// read MAX_ENTRY_COUNT times without expecting a problem
		for (int i = 0; i < MAX_ENTRY_COUNT; i++) {
			archive.getNextEntry();
		}

		// next read operation must fail
		assertThrows(IOException.class, archive::getNextEntry, "Maximum number of entries exceeded: " + MAX_ENTRY_COUNT);
	}

	@Test
	void test_maxLengthOfStream() {

		// prepare
		InputStream stream = new InputStream() {
			long count = MAX_TOTAL_SIZE + 1;

			@Override
			public int read() {
				if (count > 0) {
					count--;
					return 32;
				} else {
					return -1;
				}
			}
		};

		Archive archive = new TestArchive() {
			@Override
			public ArchiveEntry getNextEntry() throws IOException {
				loadData(stream);
				return null;
			}
		};

		// read operation must fail
		assertThrows(IOException.class, archive::getNextEntry, "Maximum length of stream exceeded: " + MAX_TOTAL_SIZE);
	}

	static class TestArchive extends Archive {

		@Override
		public long getFileSize() {
			return 0;
		}

		@Override
		public String getFileChecksum() {
			return null;
		}

		@Override
		public ArchiveEntry getNextEntry() throws IOException {
			return null;
		}

		@Override
		public void close() {
		}

	}

}