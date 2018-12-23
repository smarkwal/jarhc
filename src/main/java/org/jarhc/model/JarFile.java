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
	 * SHA-1 checksum of the JAR file
	 */
	private final String checksum;

	/**
	 * Releases supported if this is a multi-release JAR file
	 */
	private final Set<Integer> releases;

	/**
	 * Module information (for modular JAR files)
	 */
	private final ModuleInfo moduleInfo;

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
	 * @param fileName   JAR file name
	 * @param fileSize   JAR file size in bytes
	 * @param checksum   JAR file SHA-1 checksum
	 * @param releases   List of releases supported by this JAR file (for multi-release JAR files)
	 * @param moduleInfo Module information (for modular JAR files)
	 * @param classDefs  Class definitions
	 * @throws IllegalArgumentException If <code>fileName</code> or <code>classDefs</code> is <code>null</code>.
	 */
	private JarFile(String fileName, long fileSize, String checksum, Set<Integer> releases, ModuleInfo moduleInfo, List<ClassDef> classDefs) {
		if (fileName == null) throw new IllegalArgumentException("fileName");
		if (releases == null) throw new IllegalArgumentException("releases");
		if (classDefs == null) throw new IllegalArgumentException("classDefs");
		this.fileName = fileName;
		this.fileSize = fileSize;
		this.checksum = checksum;
		this.releases = new TreeSet<>(releases);
		this.moduleInfo = moduleInfo;
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

	public String getChecksum() {
		return checksum;
	}

	public boolean isMultiRelease() {
		return !releases.isEmpty();
	}

	public Set<Integer> getReleases() {
		return Collections.unmodifiableSet(releases);
	}

	public boolean isModule() {
		return moduleInfo != null;
	}

	public ModuleInfo getModuleInfo() {
		return moduleInfo;
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

	public static Builder withName(String fileName) {
		return new Builder(fileName);
	}

	public static class Builder {

		private String fileName;
		private long fileSize = -1;
		private String checksum;
		private Set<Integer> releases = new TreeSet<>();
		private ModuleInfo moduleInfo = null;
		private List<ClassDef> classDefs = new ArrayList<>();

		private Builder(String fileName) {
			this.fileName = fileName;
		}

		public Builder withFileSize(long fileSize) {
			this.fileSize = fileSize;
			return this;
		}

		public Builder withChecksum(String checksum) {
			this.checksum = checksum;
			return this;
		}

		public Builder withRelease(int release) {
			this.releases.add(release);
			return this;
		}

		public Builder withReleases(Set<Integer> releases) {
			this.releases.addAll(releases);
			return this;
		}

		public Builder withModuleInfo(ModuleInfo moduleInfo) {
			this.moduleInfo = moduleInfo;
			return this;
		}

		public Builder withClassDef(ClassDef classDef) {
			this.classDefs.add(classDef);
			return this;
		}

		public Builder withClassDefs(List<ClassDef> classDefs) {
			this.classDefs.addAll(classDefs);
			return this;
		}

		public JarFile build() {
			return new JarFile(fileName, fileSize, checksum, releases, moduleInfo, classDefs);
		}

	}

}
