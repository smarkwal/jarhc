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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.jarhc.app.JarSource;
import org.jarhc.loader.archive.Archive;
import org.jarhc.loader.archive.ArchiveEntry;
import org.jarhc.loader.archive.ZipStreamArchive;
import org.jarhc.model.JarFile;
import org.jarhc.utils.FileUtils;

public class WarFileLoader {

	private final JarFileLoader jarFileLoader;

	WarFileLoader(JarFileLoader jarFileLoader) {
		this.jarFileLoader = jarFileLoader;
	}

	public List<JarFile> load(JarSource source) throws IOException {
		if (source == null) throw new IllegalArgumentException("source");

		try (InputStream stream = source.getInputStream()) {
			return load(stream);
		}
	}

	public List<JarFile> load(File file) throws IOException {
		if (file == null) throw new IllegalArgumentException("file");
		if (!file.isFile()) throw new FileNotFoundException(file.getAbsolutePath());

		try (FileInputStream inputStream = new FileInputStream(file)) {
			return load(inputStream);
		}
	}

	public List<JarFile> load(InputStream inputStream) throws IOException {
		if (inputStream == null) throw new IllegalArgumentException("inputStream");

		try (Archive archive = new ZipStreamArchive(inputStream)) {
			return load(archive);
		}
	}

	private List<JarFile> load(Archive archive) throws IOException {
		if (archive == null) throw new IllegalArgumentException("archive");

		List<JarFile> jarFiles = new ArrayList<>();

		// for every entry in the WAR file ...
		while (true) {
			ArchiveEntry entry = archive.getNextEntry();
			if (entry == null) break;

			String entryName = entry.getName();
			if (entryName.startsWith("WEB-INF/lib/") && entryName.endsWith(".jar")) {
				String fileName = FileUtils.getFilename(entryName);

				byte[] fileData = entry.getData();

				InputStream inputStream = new ByteArrayInputStream(fileData);
				List<JarFile> files = jarFileLoader.load(fileName, inputStream);
				jarFiles.addAll(files);

			} else if (entryName.startsWith("WEB-INF/classes/")) {
				// TODO: add all files (classes and resources) to an artificial JAR file
				//  String jarFileName = file.getName() + "-classes.jar";
			}

		}

		// sort JAR files by name (case-insensitive)
		jarFiles.sort((f1, f2) -> f1.getFileName().compareToIgnoreCase(f2.getFileName()));

		return jarFiles;
	}

}
