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

import org.jarhc.model.Classpath;
import org.jarhc.model.JarFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Loader for a classpath.
 * This class is thread-safe and can be used in parallel or multiple times in sequence.
 */
public class ClasspathLoader {

	private final JarFileLoader jarFileLoader = new JarFileLoader();

	/**
	 * Create a classpath with the given JAR files.
	 *
	 * @param files List of JAR files.
	 * @return Classpath
	 * @throws IllegalArgumentException If <code>files</code> is <code>null</code>.
	 * @throws FileNotFoundException    If any of the files does not exist.
	 * @throws IOException              If a JAR file cannot be parsed.
	 */
	public Classpath load(List<File> files) throws IOException {
		if (files == null) throw new IllegalArgumentException("files");

		// load all JAR files
		List<JarFile> jarFiles = new ArrayList<>(files.size());
		for (File file : files) {
			JarFile jarFile;
			try {
				jarFile = jarFileLoader.load(file);
			} catch (IOException e) {
				String message = String.format("Unable to parse JAR file: %s", file.getAbsolutePath());
				throw new IOException(message, e);
			}
			jarFiles.add(jarFile);
		}

		return new Classpath(jarFiles);
	}

}
