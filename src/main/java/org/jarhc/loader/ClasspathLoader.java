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
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Loader for a classpath.
 * This class is thread-safe and can be used in parallel or multiple times in sequence.
 */
public class ClasspathLoader {

	private final JarFileLoader jarFileLoader;

	public ClasspathLoader(JarFileLoader jarFileLoader) {
		this.jarFileLoader = jarFileLoader;
	}

	/**
	 * Create a classpath with the given JAR files.
	 *
	 * @param files List of JAR files.
	 * @return Classpath
	 * @throws IllegalArgumentException If <code>files</code> is <code>null</code>.
	 */
	public Classpath load(List<File> files) {
		if (files == null) throw new IllegalArgumentException("files");

		// long totalTime = System.nanoTime();

		// temporary map to remember input file -> JAR file relation
		Map<File, JarFile> filesMap = new ConcurrentHashMap<>();

		// load all JAR files in parallel
		files.parallelStream().forEach(file -> {

			// long time = System.nanoTime();

			JarFile jarFile;
			try {
				jarFile = jarFileLoader.load(file);
			} catch (IOException e) {
				String message = String.format("Unable to parse JAR file: %s", file.getAbsolutePath());
				System.err.println(message);
				e.printStackTrace();
				return;
			}

			// time = System.nanoTime() - time;
			// System.out.println("\t" + jarFile.getFileName() + ": " + (time / 1000 / 1000) + " ms");

			filesMap.put(file, jarFile);
		});

		// create list of JAR files (same order as list of input files)
		List<JarFile> jarFiles = files.stream().map(filesMap::get).filter(Objects::nonNull).collect(Collectors.toList());

		// totalTime = System.nanoTime() - totalTime;
		// System.out.println("\tTotal: " + (totalTime / 1000 / 1000) + " ms");

		return new Classpath(jarFiles);
	}

}
