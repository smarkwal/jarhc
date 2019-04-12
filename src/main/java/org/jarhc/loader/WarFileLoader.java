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

import org.jarhc.app.JarSource;
import org.jarhc.model.JarFile;
import org.jarhc.utils.FileUtils;
import org.jarhc.utils.IOUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class WarFileLoader {

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
					byte[] fileData = IOUtils.toByteArray(zip);
					List<JarFile> files = jarFileLoader.load(fileName, fileData);
					jarFiles.addAll(files);
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
