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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.jarhc.app.FileSource;
import org.jarhc.app.JarSource;
import org.jarhc.artifacts.Repository;
import org.jarhc.loader.archive.Archive;
import org.jarhc.loader.archive.ZipFileArchive;
import org.jarhc.model.JarFile;
import org.slf4j.Logger;

/**
 * Loader for a JMOD file, using a file as source.
 */
class JmodFileLoader extends AbstractFileLoader {

	JmodFileLoader(String classLoader, int maxRelease, ClassDefLoader classDefLoader, ModuleInfoLoader moduleInfoLoader, FileNameNormalizer fileNameNormalizer, Repository repository, Logger logger) {
		super(classLoader, maxRelease, classDefLoader, moduleInfoLoader, fileNameNormalizer, repository, logger);
	}

	List<JarFile> load(JarSource source) throws IOException {
		if (source instanceof FileSource) {
			FileSource fileSource = (FileSource) source;
			return load(fileSource.getFile());
		} else {
			throw new IOException("JMOD can only be loaded from a file: " + source.getName());
		}
	}

	List<JarFile> load(File file) throws IOException {
		if (file == null) throw new IllegalArgumentException("file");
		if (!file.isFile()) throw new FileNotFoundException(file.getAbsolutePath());

		List<JarFile> jarFiles = new ArrayList<>();
		try (Archive archive = new ZipFileArchive(file)) {
			load(file.getName(), archive, jarFiles);
		}
		return jarFiles;
	}

	@Override
	protected String filter(String name) {
		// accept only files in "classes/" directory
		if (name.startsWith("classes/")) {
			return name.substring(8);
		} else {
			return null;
		}
	}

}
