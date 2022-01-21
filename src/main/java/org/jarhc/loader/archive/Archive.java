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

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import org.jarhc.utils.IOUtils;
import org.jarhc.utils.LimitedInputStream;

public abstract class Archive implements Closeable {

	// limits to prevent Zip Bomb attacks
	static final long MAX_TOTAL_SIZE = 1024 * 1024 * 1024L; // 1 GB
	static final long MAX_ENTRY_SIZE = 100 * 1024 * 1024L; // 100 MB
	static final long MAX_ENTRY_COUNT = 25000;

	private int entriesCount = 0;
	private long totalSize = 0;

	public abstract long getFileSize() throws IOException;

	public abstract String getFileChecksum() throws IOException;

	public boolean isMultiRelease() {
		return false;
	}

	public String getAutomaticModuleName() {
		return null;
	}

	public abstract ArchiveEntry getNextEntry() throws IOException;

	protected byte[] loadData(InputStream inputStream) throws IOException {

		// check if max number of files has been reached (prevent Zip Bomb attack)
		entriesCount++;
		if (entriesCount > MAX_ENTRY_COUNT) {
			String message = "Maximum number of entries exceeded: " + MAX_ENTRY_COUNT;
			throw new IOException(message);
		}

		// limit max file size (prevent Zip Bomb attack)
		InputStream in = new LimitedInputStream(inputStream, MAX_ENTRY_SIZE);

		byte[] data = IOUtils.toByteArray(in);

		// check if max total size has been reached (prevent Zip Bomb attack)
		totalSize += data.length;
		if (totalSize > MAX_TOTAL_SIZE) {
			String message = "Maximum total size exceeded: " + MAX_TOTAL_SIZE;
			throw new IOException(message);
		}

		return data;
	}
}
