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
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.function.Predicate;
import org.jarhc.artifacts.Artifact;

/**
 * Represents the content of a JAR file.
 */
public class JarFile {

	/**
	 * Unique identifier for this JAR file.
	 * Note that this UUID is not deterministic or persisted
	 * and will be different for each run.
	 */
	private final String uuid = UUID.randomUUID().toString();

	/**
	 * Artifact group ID (from Maven coordinates)
	 */
	private final String artifactGroupId;

	/**
	 * Artifact name (from Maven coordinates or file name)
	 */
	private final String artifactName;

	/**
	 * Artifact version (from Maven coordinates or file name)
	 */
	private final String artifactVersion;

	/**
	 * JAR file name
	 */
	private final String fileName;

	/**
	 * Unique display name based on artifact name, version, group ID, and checksum.
	 */
	private String displayName;

	/**
	 * JAR file size in bytes
	 */
	private final long fileSize;

	/**
	 * SHA-1 checksum of the JAR file
	 */
	private final String checksum;

	/**
	 * Maven coordinates for the JAR file specified as argument on the command line.
	 * Is {@code null} if the JAR file was not loaded from a Maven repository.
	 */
	private final String coordinates;

	/**
	 * Maven coordinates of the JAR file as found by Maven Search API.
	 */
	private final List<Artifact> artifacts;

	/**
	 * Name of the class loader used to load this JAR file.
	 */
	private final String classLoader;

	/**
	 * Manifest attributes found in META-INF/MANIFEST.MF.
	 */
	private final Map<String, String> manifestAttributes;

	/**
	 * Releases supported if this is a multi-release JAR file
	 */
	private final Set<Integer> releases;

	/**
	 * Module information (for modular JAR files)
	 */
	private final ModuleInfo moduleInfo;

	/**
	 * OSGi bundle information
	 */
	private final OSGiBundleInfo osgiBundleInfo;

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
	private final Set<String> packageNames = new TreeSet<>();

	/**
	 * Create a new JAR file given the file name and the list of class definitions.
	 *
	 * @param fileName           JAR file name
	 * @param fileSize           JAR file size in bytes
	 * @param checksum           JAR file SHA-1 checksum
	 * @param coordinates        Maven coordinates given on command line
	 * @param artifacts          List of Maven coordinates found by Maven Search API
	 * @param classLoader        Class loader name
	 * @param manifestAttributes Manifest attributes
	 * @param releases           List of releases supported by this JAR file (for multi-release JAR files)
	 * @param moduleInfo         Module information (for modular JAR files)
	 * @param osgiBundleInfo     OSGi Bundle information
	 * @param classDefs          Class definitions
	 * @param resourceDefs       Resources
	 * @throws IllegalArgumentException If <code>fileName</code> or <code>classDefs</code> is <code>null</code>.
	 */
	@SuppressWarnings("java:S107") // Methods should not have too many parameters
	private JarFile(String fileName, long fileSize, String checksum, String coordinates, List<Artifact> artifacts, String classLoader, Map<String, String> manifestAttributes, Set<Integer> releases, ModuleInfo moduleInfo, OSGiBundleInfo osgiBundleInfo, List<ClassDef> classDefs, List<ResourceDef> resourceDefs) {
		if (fileName == null) throw new IllegalArgumentException("fileName");
		if (releases == null) throw new IllegalArgumentException("releases");
		if (moduleInfo == null) throw new IllegalArgumentException("moduleInfo");
		if (classDefs == null) throw new IllegalArgumentException("classDefs");
		this.fileName = fileName;
		this.fileSize = fileSize;
		this.checksum = checksum;
		this.coordinates = coordinates;
		this.artifacts = artifacts;
		this.classLoader = classLoader;
		this.manifestAttributes = manifestAttributes;
		this.releases = new TreeSet<>(releases);
		this.moduleInfo = moduleInfo;
		this.osgiBundleInfo = osgiBundleInfo;
		this.classDefs = new ArrayList<>(classDefs);
		this.resourceDefs = new ArrayList<>(resourceDefs);

		// sort class definitions by class name (case-sensitive)
		this.classDefs.sort(Comparator.comparing(ClassDef::getClassName));

		// for every class definition ...
		this.classDefs.forEach(classDef -> {

			// set reference to this JAR file in class definition
			classDef.setJarFile(this);

			// set reference to module info in class definition
			classDef.setModuleInfo(moduleInfo);

			// add class definition to fast lookup map
			String className = classDef.getClassName();
			classDefsMap.put(className, classDef);

			if (classDef.isRegularClass()) {
				// add package name to package list
				String packageName = classDef.getPackageName();
				packageNames.add(packageName);
			}

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

		// get artifact name and version from coordinates or file name
		Artifact artifact = findArtifact(coordinates, fileName, artifacts);
		String groupId = artifact.getGroupId();
		String artifactId = artifact.getArtifactId();
		String version = artifact.getVersion();
		assert artifactId != null;

		this.artifactGroupId = (groupId != null && !groupId.isEmpty()) ? groupId : null;
		this.artifactName = artifactId;
		this.artifactVersion = (version != null && !version.isEmpty()) ? version : null;
	}

	private static Artifact findArtifact(String coordinates, String fileName, List<Artifact> artifacts) {

		if (coordinates != null) {
			return new Artifact(coordinates);
		}

		// remove path from file name (only for nested JAR files)
		int pos = fileName.lastIndexOf('/');
		if (pos >= 0) {
			fileName = fileName.substring(pos + 1);
		}

		// if artifact has been found in Maven repository ...
		if (artifacts != null && !artifacts.isEmpty()) {

			// try to find an artifact with a matching file name
			for (Artifact artifact : artifacts) {
				if (artifact.getFileName().equals(fileName)) {
					return artifact;
				}
			}

			// use first artifact as fallback
			return artifacts.get(0);
		}

		// try to get artifact name and version from file name
		return Artifact.fromFileName(fileName);
	}

	public String getUUID() {
		return uuid;
	}

	/**
	 * Get the group ID of the artifact.
	 *
	 * @return Group ID, or <code>null</code> if unknown.
	 */
	public String getArtifactGroupId() {
		return artifactGroupId;
	}

	/**
	 * Get the name of the artifact.
	 * This is based on a Maven artifact ID or the file name.
	 *
	 * @return Artifact name.
	 */
	public String getArtifactName() {
		return artifactName;
	}

	/**
	 * Get the version of the artifact.
	 * This is based on a Maven version or the file name.
	 *
	 * @return Artifact version, or <code>null</code> if unknown.
	 */
	public String getArtifactVersion() {
		return artifactVersion;
	}

	public String getFileName() {
		return fileName;
	}

	public String getDisplayName() {
		if (displayName != null) {
			return displayName;
		}
		return artifactName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public long getFileSize() {
		return fileSize;
	}

	public String getChecksum() {
		return checksum;
	}

	public String getCoordinates() {
		return coordinates;
	}

	public List<Artifact> getArtifacts() {
		return artifacts;
	}

	/**
	 * Try to find an artifact for the given predicate.
	 *
	 * @param predicate Predicate
	 * @return Artifact, or <code>null</code> if not found.
	 */
	public Artifact getArtifact(Predicate<Artifact> predicate) {
		if (artifacts == null) {
			return null;
		}
		return artifacts.stream()
				.filter(predicate)
				.findFirst()
				.orElse(null);
	}

	public String getClassLoader() {
		return classLoader;
	}

	public Map<String, String> getManifestAttributes() {
		return manifestAttributes;
	}

	public boolean isMultiRelease() {
		return !releases.isEmpty();
	}

	public Set<Integer> getReleases() {
		return releases;
	}

	public ModuleInfo getModuleInfo() {
		return moduleInfo;
	}

	public OSGiBundleInfo getOSGiBundleInfo() {
		return osgiBundleInfo;
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

	public List<String> getPackageNames() {
		return new ArrayList<>(packageNames);
	}

	/**
	 * Returns a list of class definitions.
	 *
	 * @return Class definitions
	 */
	public List<ClassDef> getClassDefs() {
		return classDefs;
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
	 * Returns a list of resources.
	 *
	 * @return Resources
	 */
	public List<ResourceDef> getResourceDefs() {
		return resourceDefs;
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

	// intended for testing
	public static Builder forCoordinates(String coordinates) {
		Artifact artifact = new Artifact(coordinates);
		String fileName = artifact.getFileName();
		Builder builder = new Builder(fileName);
		builder.withCoordinates(coordinates);
		return builder;
	}

	// intended for testing
	public static Builder forArtifact(String coordinates) {
		Artifact artifact = new Artifact(coordinates);
		String fileName = artifact.getFileName();
		Builder builder = new Builder(fileName);
		builder.withArtifact(coordinates);
		builder.withCoordinates(coordinates);
		return builder;
	}

	public static class Builder {

		private final String fileName;
		private long fileSize = -1;
		private String checksum;
		private String coordinates;
		private List<Artifact> artifacts;
		private String classLoader;
		private Map<String, String> manifestAttributes;
		private final Set<Integer> releases = new TreeSet<>();
		private ModuleInfo moduleInfo = ModuleInfo.UNNAMED;
		private OSGiBundleInfo osgiBundleInfo;
		private final List<ClassDef> classDefs = new ArrayList<>();
		private final List<ResourceDef> resourceDefs = new ArrayList<>();

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

		public Builder withCoordinates(String coordinates) {
			this.coordinates = coordinates;
			return this;
		}

		public Builder withArtifacts(List<Artifact> artifacts) {
			this.artifacts = artifacts;
			return this;
		}

		public Builder withArtifact(String coordinates) {
			if (artifacts == null) {
				artifacts = new ArrayList<>();
			}
			artifacts.add(new Artifact(coordinates));
			return this;
		}

		public Builder withClassLoader(String classLoader) {
			this.classLoader = classLoader;
			return this;
		}

		public Builder withManifestAttributes(Map<String, String> manifestAttributes) {
			this.manifestAttributes = manifestAttributes;
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
			if (moduleInfo == null) throw new IllegalArgumentException("moduleInfo == null");
			this.moduleInfo = moduleInfo;
			return this;
		}

		public Builder withOSGiBundleInfo(OSGiBundleInfo osgiBundleInfo) {
			this.osgiBundleInfo = osgiBundleInfo;
			return this;
		}

		public Builder withClassDef(ClassDef classDef) {
			this.classDefs.add(classDef);
			return this;
		}

		public Builder withClassDefs(Collection<ClassDef> classDefs) {
			this.classDefs.addAll(classDefs);
			return this;
		}

		public Builder withResourceDefs(Collection<ResourceDef> resourceDefs) {
			this.resourceDefs.addAll(resourceDefs);
			return this;
		}

		public JarFile build() {
			return new JarFile(fileName, fileSize, checksum, coordinates, artifacts, classLoader, manifestAttributes, releases, moduleInfo, osgiBundleInfo, classDefs, resourceDefs);
		}

	}

}
