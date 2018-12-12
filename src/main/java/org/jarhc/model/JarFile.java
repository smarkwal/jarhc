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

import java.util.*;

/**
 * Represents the content of a JAR file.
 */
public class JarFile {

	/**
	 * JAR file name
	 */
	private final String fileName;

	/**
	 * JAR file size in bytes
	 */
	private final long fileSize;

	/**
	 * List of class definitions for classes found in the JAR file.
	 */
	private final List<ClassDef> classDefs;

	/**
	 * Fast lookup map for class definition given the class name.
	 */
	private final Map<String, ClassDef> classDefsMap = new HashMap<>();

	/**
	 * Create a new JAR file given the file name and the list of class definitions.
	 *
	 * @param fileName  JAR file name
	 * @param fileSize  JAR file size in bytes
	 * @param classDefs Class definitions
	 * @throws IllegalArgumentException If <code>fileName</code> or <code>classDefs</code> is <code>null</code>.
	 */
	public JarFile(String fileName, long fileSize, List<ClassDef> classDefs) {
		if (fileName == null) throw new IllegalArgumentException("fileName");
		if (classDefs == null) throw new IllegalArgumentException("classDefs");
		this.fileName = fileName;
		this.fileSize = fileSize;
		this.classDefs = new ArrayList<>(classDefs);

		// sort class definitions by class name (case-sensitive)
		this.classDefs.sort(Comparator.comparing(ClassDef::getClassName));

		// for every class definition ...
		this.classDefs.forEach(classDef -> {

			// set reference to this JAR file in class definition
			classDef.setJarFile(this);

			// add class definition to fast lookup map
			classDefsMap.put(classDef.getClassName(), classDef);

		});
	}

	public String getFileName() {
		return fileName;
	}

	public long getFileSize() {
		return fileSize;
	}

	/**
	 * Returns an unmodifiable list of class definitions.
	 *
	 * @return Class definitions
	 */
	public List<ClassDef> getClassDefs() {
		return Collections.unmodifiableList(classDefs);
	}

	/**
	 * Get the class definition with the given class name,
	 * or <code>null</code> if the class is not found in this JAR file.
	 *
	 * @param className Class name
	 * @return Class definition, or <code>null</code>
	 */
	public ClassDef getClassDef(String className) {
		return classDefsMap.get(className);
	}

	@Override
	public String toString() {
		return String.format("JarFile[%s,%d]", fileName, classDefs.size());
	}

}
