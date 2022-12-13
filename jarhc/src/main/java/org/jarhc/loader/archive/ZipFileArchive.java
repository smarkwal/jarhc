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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.jarhc.utils.ByteBuffer;
import org.jarhc.utils.FileUtils;

public class ZipFileArchive extends Archive {

	private final File file;
	private final ZipFile zipFile;
	private final Enumeration<? extends ZipEntry> zipEntries;

	public ZipFileArchive(File file) throws IOException {
		this.file = file;
		this.zipFile = new ZipFile(file);
		this.zipEntries = zipFile.entries();
	}

	@Override
	public long getFileSize() {
		return file.length();
	}

	@Override
	public String getFileChecksum() throws IOException {
		return FileUtils.sha1Hex(file);
	}

	@Override
	public ArchiveEntry getNextEntry() {

		while (zipEntries.hasMoreElements()) {
			ZipEntry zipEntry = zipEntries.nextElement();

			// ignore directories
			if (zipEntry.isDirectory()) {
				continue;
			}

			return new ZipArchiveEntry(zipEntry);
		}

		return null;
	}

	@Override
	public void close() throws IOException {
		zipFile.close();
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
		public ByteBuffer getData() throws IOException {
			try (InputStream stream = zipFile.getInputStream(zipEntry)) {
				return loadData(stream);
			}
		}

	}

}


