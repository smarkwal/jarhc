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

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ZipStreamArchive extends Archive {

	private final ChecksumInputStream checksumInputStream;
	private final ZipInputStream zipInputStream;

	public ZipStreamArchive(InputStream inputStream) {
		this.checksumInputStream = new ChecksumInputStream(inputStream);
		this.zipInputStream = new ZipInputStream(checksumInputStream);
	}

	@Override
	public long getFileSize() throws IOException {
		return checksumInputStream.getSize();
	}

	@Override
	public String getFileChecksum() throws IOException {
		return checksumInputStream.getChecksum();
	}

	@Override
	public ArchiveEntry getNextEntry() throws IOException {

		while (true) {
			ZipEntry zipEntry = zipInputStream.getNextEntry();
			if (zipEntry == null) {
				return null;
			}

			// ignore directories
			if (zipEntry.isDirectory()) {
				continue;
			}

			return new ZipArchiveEntry(zipEntry);
		}
	}

	@Override
	public void close() throws IOException {
		zipInputStream.close();
	}

	private class ZipArchiveEntry implements ArchiveEntry {

		private final ZipEntry zipEntry;

		public ZipArchiveEntry(ZipEntry zipEntry) {
			this.zipEntry = zipEntry;
		}

		@Override
		public String getName() {
			return zipEntry.getName();
		}

		@Override
		public byte[] getData() throws IOException {
			return loadData(zipInputStream);
		}

	}

}


