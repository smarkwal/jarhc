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

package org.jarhc.loader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.jarhc.app.JarSource;
import org.jarhc.model.JarFile;
import org.jarhc.utils.FileUtils;
import org.jarhc.utils.IOUtils;
import org.jarhc.utils.LimitedInputStream;

public class WarFileLoader {

	// limits to prevent Zip Bomb attacks
	private static final long MAX_TOTAL_SIZE = 1024 * 1024 * 1024L; // 1 GB
	private static final long MAX_ENTRY_SIZE = 100 * 1024 * 1024L; // 100 MB
	private static final long MAX_ENTRY_COUNT = 10000;

	private final JarFileLoader jarFileLoader;

	WarFileLoader(JarFileLoader jarFileLoader) {
		this.jarFileLoader = jarFileLoader;
	}

	public List<JarFile> load(File file) throws IOException {
		if (file == null) throw new IllegalArgumentException("file");
		if (!file.isFile()) throw new FileNotFoundException(file.getAbsolutePath());
		try (InputStream stream = new FileInputStream(file)) {
			return load(stream);
		}
	}

	public List<JarFile> load(JarSource source) throws IOException {
		if (source == null) throw new IllegalArgumentException("source");
		try (InputStream stream = source.getData()) {
			return load(stream);
		}
	}

	public List<JarFile> load(InputStream stream) throws IOException {
		if (stream == null) throw new IllegalArgumentException("stream");

		long totalSize = 0;
		List<JarFile> jarFiles = new ArrayList<>();

		try (ZipInputStream zip = new ZipInputStream(stream)) {

			// for every entry in the WAR file ...
			while (true) {
				ZipEntry entry = zip.getNextEntry();
				if (entry == null) break;

				// ignore directories
				if (entry.isDirectory()) continue;

				String entryName = entry.getName();
				if (entryName.startsWith("WEB-INF/lib/") && entryName.endsWith(".jar")) {
					String fileName = FileUtils.getFilename(entryName);

					// limit max JAR file size (prevent Zip Bomb attack)
					InputStream in = new LimitedInputStream(zip, MAX_ENTRY_SIZE);

					// read content of JAR file
					byte[] fileData = IOUtils.toByteArray(in);

					// check if max total size has been reached (prevent Zip Bomb attack)
					totalSize += fileData.length;
					if (totalSize > MAX_TOTAL_SIZE) {
						throw new IOException("Maximum total size exceeded.");
					}

					List<JarFile> files = jarFileLoader.load(fileName, fileData);
					jarFiles.addAll(files);

					// check if max number of JAR files has been reached (prevent Zip Bomb attack)
					if (jarFiles.size() > MAX_ENTRY_COUNT) {
						throw new IOException("Maximum number of entries exceeded.");
					}

				} else if (entryName.startsWith("WEB-INF/classes/")) {
					// TODO: add all files (classes and resources) to an artificial JAR file
					//  String jarFileName = file.getName() + "-classes.jar";
				}

			}
		}

		// sort JAR files by name (case-insensitive)
		jarFiles.sort((f1, f2) -> f1.getFileName().compareToIgnoreCase(f2.getFileName()));

		return jarFiles;
	}

}
