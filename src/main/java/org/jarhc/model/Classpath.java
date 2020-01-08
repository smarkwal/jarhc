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

package org.jarhc.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import org.jarhc.java.ClassLoader;
import org.jarhc.java.ClassLoaderStrategy;
import org.jarhc.utils.MultiMap;

/**
 * Represents a set of JAR files found on a Java classpath.
 */
public class Classpath extends ClassLoader {

	/**
	 * List of JAR files on the classpath.
	 */
	private final List<JarFile> jarFiles;

	/**
	 * Fast lookup map for JAR files given the file name.
	 */
	private final Map<String, JarFile> jarFilesMap = new HashMap<>();

	/**
	 * Fast lookup map for class definitions given the class name.
	 */
	private final MultiMap<String, ClassDef> classDefsMap = new MultiMap<>(false);

	/**
	 * Create a new classpath with the given JAR files.
	 *
	 * @param jarFiles          JAR files
	 * @param parentClassLoader Parent class loader
	 * @param strategy          Class loader strategy
	 * @throws IllegalArgumentException If <code>jarFiles</code> is <code>null</code>.
	 */
	public Classpath(List<JarFile> jarFiles, ClassLoader parentClassLoader, ClassLoaderStrategy strategy) {
		super("Classpath", parentClassLoader, strategy);
		if (jarFiles == null) throw new IllegalArgumentException("jarFiles");
		this.jarFiles = new ArrayList<>(jarFiles);

		// for every JAR file ...
		this.jarFiles.forEach(jarFile -> {

			// add JAR file to fast lookup map
			jarFilesMap.put(jarFile.getFileName(), jarFile);

			// for every class definition in this JAR file ...
			jarFile.getClassDefs().forEach(classDef -> {

				// add class definition to fast lookup map
				classDefsMap.add(classDef.getClassName(), classDef);
			});
		});
	}

	/**
	 * Returns an unmodifiable list of JAR files.
	 *
	 * @return JAR files
	 */
	public List<JarFile> getJarFiles() {
		return Collections.unmodifiableList(jarFiles);
	}

	/**
	 * Get the JAR file with the given file name,
	 * or <code>null</code> if the JAR file is not found in this classpath.
	 *
	 * @param fileName File name
	 * @return JAR file, or <code>null</code>
	 */
	public JarFile getJarFile(String fileName) {
		return jarFilesMap.get(fileName);
	}

	@Override
	public Optional<JarFile> findJarFile(Predicate<JarFile> predicate) {
		return jarFiles.stream().filter(predicate).findFirst();
	}

	@Override
	protected boolean findPackage(String packageName) {
		return jarFiles.stream().anyMatch(f -> f.containsPackage(packageName));
	}

	/**
	 * Get the class definitions with the given class name,
	 * or <code>null</code> if the class is not found in any JAR file.
	 *
	 * @param className Class name
	 * @return Class definitions, or <code>null</code>
	 */
	public Set<ClassDef> getClassDefs(String className) {
		return classDefsMap.getValues(className);
	}

	protected Optional<ClassDef> findClassDef(String className) {
		Set<ClassDef> set = getClassDefs(className);
		if (set == null || set.isEmpty()) {
			return Optional.empty();
		}
		ClassDef classDef = set.iterator().next();
		return Optional.of(classDef);
	}

	@Override
	public String toString() {
		return String.format("Classpath[%d]", jarFiles.size());
	}

}
