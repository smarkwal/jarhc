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
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.jarhc.app.FileSource;
import org.jarhc.app.JarSource;
import org.jarhc.java.ClassLoader;
import org.jarhc.java.ClassLoaderStrategy;
import org.jarhc.model.Classpath;
import org.jarhc.model.JarFile;
import org.slf4j.Logger;

/**
 * Loader for a classpath.
 * This class is thread-safe and can be used in parallel or multiple times in sequence.
 */
public class ClasspathLoader {

	private final JarFileLoader jarFileLoader;
	private final JmodFileLoader jmodFileLoader;
	private final WarFileLoader warFileLoader;
	private final ClassLoader parentClassLoader;
	private final ClassLoaderStrategy strategy;
	private final Logger logger;

	ClasspathLoader(JarFileLoader jarFileLoader, JmodFileLoader jmodFileLoader, WarFileLoader warFileLoader, ClassLoader parentClassLoader, ClassLoaderStrategy strategy, Logger logger) {
		this.jarFileLoader = jarFileLoader;
		this.jmodFileLoader = jmodFileLoader;
		this.warFileLoader = warFileLoader;
		this.parentClassLoader = parentClassLoader;
		this.strategy = strategy;
		this.logger = logger;
	}

	public Classpath load(Collection<File> files) {
		if (files == null) throw new IllegalArgumentException("files");

		List<JarSource> sources = files.stream().map(FileSource::new).collect(Collectors.toList());
		return load(sources);
	}

	/**
	 * Create a classpath with the given JAR files.
	 *
	 * @param sources List of JAR files.
	 * @return Classpath
	 * @throws IllegalArgumentException If <code>files</code> is <code>null</code>.
	 */
	public Classpath load(List<JarSource> sources) {
		if (sources == null) throw new IllegalArgumentException("files");

		long totalTime = System.nanoTime();

		// temporary map to remember input file -> JAR file relation
		Map<JarSource, List<JarFile>> filesMap = new ConcurrentHashMap<>();

		// load all files in parallel
		sources.parallelStream().forEach(source -> {

			long time = System.nanoTime();

			List<JarFile> jarFiles;
			try {
				String fileName = source.getName().toLowerCase();
				if (fileName.endsWith(".jar")) {
					jarFiles = jarFileLoader.load(source);
				} else if (fileName.endsWith(".jmod")) {
					jarFiles = jmodFileLoader.load(source);
				} else if (fileName.endsWith(".war")) {
					jarFiles = warFileLoader.load(source);
				} else {
					logger.warn("Unsupported file extension: {}", source.getName());
					return;
				}
			} catch (IOException e) {
				logger.warn("Unable to parse file: {}", source.getName(), e);
				return;
			}

			if (logger.isDebugEnabled()) {
				time = System.nanoTime() - time;
				logger.debug("{}: {} ms", source.getName(), time / 1000 / 1000);
			}

			filesMap.put(source, jarFiles);
		});

		// create list of JAR files (same order as list of input files)
		List<JarFile> jarFiles = sources.stream().map(filesMap::get).filter(Objects::nonNull).flatMap(Collection::stream).collect(Collectors.toList());

		if (logger.isDebugEnabled()) {
			totalTime = System.nanoTime() - totalTime;
			logger.debug("Total: {} ms", totalTime / 1000 / 1000);
		}

		return new Classpath(jarFiles, parentClassLoader, strategy);
	}

}
