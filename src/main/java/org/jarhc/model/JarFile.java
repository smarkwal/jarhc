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
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.jarhc.utils.JavaUtils;

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
	 * List of resources found in the JAR file.
	 */
	private final List<ResourceDef> resourceDefs;

	/**
	 * Fast lookup map for resources given the resource path.
	 */
	private final Map<String, ResourceDef> resourceDefsMap = new HashMap<>();

	/**
	 * Set of Java packages in this JAR file.
	 */
	private final Set<String> packageNames = new HashSet<>();

	/**
	 * Create a new JAR file given the file name and the list of class definitions.
	 *
	 * @param fileName     JAR file name
	 * @param fileSize     JAR file size in bytes
	 * @param checksum     JAR file SHA-1 checksum
	 * @param releases     List of releases supported by this JAR file (for multi-release JAR files)
	 * @param moduleInfo   Module information (for modular JAR files)
	 * @param classDefs    Class definitions
	 * @param resourceDefs Resources
	 * @throws IllegalArgumentException If <code>fileName</code> or <code>classDefs</code> is <code>null</code>.
	 */
	private JarFile(String fileName, long fileSize, String checksum, Set<Integer> releases, ModuleInfo moduleInfo, List<ClassDef> classDefs, List<ResourceDef> resourceDefs) {
		if (fileName == null) throw new IllegalArgumentException("fileName");
		if (releases == null) throw new IllegalArgumentException("releases");
		if (classDefs == null) throw new IllegalArgumentException("classDefs");
		this.fileName = fileName;
		this.fileSize = fileSize;
		this.checksum = checksum;
		this.releases = new TreeSet<>(releases);
		this.moduleInfo = moduleInfo;
		this.classDefs = new ArrayList<>(classDefs);
		this.resourceDefs = new ArrayList<>(resourceDefs);

		// sort class definitions by class name (case-sensitive)
		this.classDefs.sort(Comparator.comparing(ClassDef::getClassName));

		// for every class definition ...
		this.classDefs.forEach(classDef -> {

			// set reference to this JAR file in class definition
			classDef.setJarFile(this);

			// add class definition to fast lookup map
			String className = classDef.getClassName();
			classDefsMap.put(className, classDef);

			// add package name to package list
			String packageName = JavaUtils.getPackageName(className);
			packageNames.add(packageName);

		});

		// sort resources by path (case-sensitive)
		this.resourceDefs.sort(Comparator.comparing(ResourceDef::getPath));

		// for every resource ...
		this.resourceDefs.forEach(resourceDef -> {

			// set reference to this JAR file in resource
			resourceDef.setJarFile(this);

			// add resource to fast lookup map
			resourceDefsMap.put(resourceDef.getPath(), resourceDef);

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
	 * Check if this JAR file contains Java classes from the given package.
	 *
	 * @param packageName Java package name.
	 * @return <code>true</code> if package exists in this JAR file,
	 * <code>false</code> otherwise.
	 */
	public boolean containsPackage(String packageName) {
		return packageNames.contains(packageName);
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

	/**
	 * Returns an unmodifiable list of resources.
	 *
	 * @return Resources
	 */
	public List<ResourceDef> getResourceDefs() {
		return Collections.unmodifiableList(resourceDefs);
	}

	/**
	 * Get the resource with the given path,
	 * or <code>null</code> if the resource is not found in this JAR file.
	 *
	 * @param path Resource path
	 * @return Resource, or <code>null</code>
	 */
	public ResourceDef getResourceDef(String path) {
		return resourceDefsMap.get(path);
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
		private List<ResourceDef> resourceDefs = new ArrayList<>();

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

		public Builder withResourceDefs(List<ResourceDef> resourceDefs) {
			this.resourceDefs.addAll(resourceDefs);
			return this;
		}

		public JarFile build() {
			return new JarFile(fileName, fileSize, checksum, releases, moduleInfo, classDefs, resourceDefs);
		}

	}

}
