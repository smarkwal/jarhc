/*
 * Copyright 2018 Stephan Markwalder
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
import org.jarhc.app.JarSource;
import org.jarhc.artifacts.Repository;
import org.jarhc.loader.archive.JarStreamArchive;
import org.jarhc.model.JarFile;
import org.slf4j.Logger;

/**
 * Loader for a JAR file, using a file as source.
 * This class is thread-safe and can be used in parallel or multiple times in sequence.
 */
class JarFileLoader extends AbstractFileLoader {

	public JarFileLoader(String classLoader, int maxRelease, ClassDefLoader classDefLoader, ModuleInfoLoader moduleInfoLoader, FileNameNormalizer fileNameNormalizer, Repository repository, Logger logger) {
		super(classLoader, maxRelease, classDefLoader, moduleInfoLoader, fileNameNormalizer, repository, logger);
	}

	List<JarFile> load(JarSource source) throws IOException {
		if (source == null) throw new IllegalArgumentException("source");

		try (InputStream inputStream = source.getInputStream()) {
			return load(source.getName(), inputStream);
		}
	}

	/**
	 * Load a JAR file from the given file.
	 * This method does not check whether the given file
	 * has a correct JAR file name.
	 *
	 * @param file File
	 * @return JAR file
	 * @throws IllegalArgumentException If <code>file</code> is <code>null</code>.
	 * @throws FileNotFoundException    If the file does not exist.
	 * @throws IOException              If the file cannot be parsed.
	 */
	List<JarFile> load(File file) throws IOException {
		if (file == null) throw new IllegalArgumentException("file");
		if (!file.isFile()) throw new FileNotFoundException(file.getAbsolutePath());

		try (InputStream inputStream = new FileInputStream(file)) {
			return load(file.getName(), inputStream);
		}
	}

	List<JarFile> load(String fileName, InputStream inputStream) throws IOException {
		if (fileName == null) throw new IllegalArgumentException("fileName");
		if (inputStream == null) throw new IllegalArgumentException("inputStream");

		List<JarFile> jarFiles = new ArrayList<>();
		try (JarStreamArchive archive = new JarStreamArchive(inputStream)) {
			load(fileName, archive, jarFiles);
		}

		// sort JAR files by name
		jarFiles.sort((f1, f2) -> String.CASE_INSENSITIVE_ORDER.compare(f1.getFileName(), f2.getFileName()));

		return jarFiles;
	}

}
