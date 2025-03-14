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

package org.jarhc.test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.jarhc.artifacts.Artifact;
import org.jarhc.java.ClassLoader;
import org.jarhc.java.ClassLoaderStrategy;
import org.jarhc.model.ClassDef;
import org.jarhc.model.ClassRef;
import org.jarhc.model.Classpath;
import org.jarhc.model.JarFile;
import org.jarhc.model.MethodDef;
import org.jarhc.model.ModuleInfo;
import org.jarhc.model.OSGiBundleInfo;
import org.jarhc.model.ResourceDef;
import org.jarhc.utils.DigestUtils;

public class ClasspathBuilder {

	// Classpath properties
	private final ClassLoader parentClassLoader;
	private ClassLoaderStrategy strategy = ClassLoaderStrategy.ParentLast;
	private final String classLoader;
	private final List<JarFile> jarFiles = new ArrayList<>();

	// JarFile properties
	private String fileName;
	private long fileSize;
	private String coordinates;
	private Map<String, String> manifestAttributes;
	private Set<Integer> releases;
	private ModuleInfo moduleInfo;
	private OSGiBundleInfo osGiBundleInfo;
	private List<ClassDef> classDefs;
	private List<ResourceDef> resourceDefs;

	// ClassDef properties
	private String className;
	private int release;
	private int majorClassVersion;
	private int minorClassVersion;
	private List<ClassRef> classRefs;
	private List<MethodDef> methodDefs;

	private ClasspathBuilder(String classLoader, ClassLoader parentClassLoader) {
		this.parentClassLoader = parentClassLoader;
		this.classLoader = classLoader;
	}

	public static ClasspathBuilder create(ClassLoader parentClassLoader) {
		return create("Classpath", parentClassLoader);
	}

	public static ClasspathBuilder create(String classLoader, ClassLoader parentClassLoader) {
		return new ClasspathBuilder(classLoader, parentClassLoader);
	}

	public ClasspathBuilder withClassLoaderStrategy(ClassLoaderStrategy strategy) {
		this.strategy = strategy;
		return this;
	}

	public ClasspathBuilder addJarFile(String fileName) {
		return addJarFile(fileName, null, 1024);
	}

	public ClasspathBuilder addJarFile(String fileName, String coordinates, long fileSize) {
		closeClassDef();
		closeJarFile();
		openJarFile(fileName, coordinates, fileSize);
		return this;
	}

	public ClasspathBuilder addRelease(int release) {
		if (releases == null) throw new IllegalStateException();
		releases.add(release);
		return this;
	}

	public ClasspathBuilder addModuleInfo(ModuleInfo moduleInfo) {
		if (moduleInfo == null) throw new IllegalArgumentException("moduleInfo == null");
		this.moduleInfo = moduleInfo;
		return this;
	}

	public ClasspathBuilder addOSGiBundleInfo(OSGiBundleInfo osGiBundleInfo) {
		this.osGiBundleInfo = osGiBundleInfo;
		return this;
	}

	public ClasspathBuilder addManifestAttribute(String name, String value) {
		if (manifestAttributes == null) {
			manifestAttributes = new LinkedHashMap<>();
		}
		manifestAttributes.put(name, value);
		return this;
	}

	public ClasspathBuilder addClassDef(String className) {
		return addClassDef(className, 8, 52, 0);
	}

	public ClasspathBuilder addClassDef(String className, int majorClassVersion, int minorClassVersion) {
		closeClassDef();
		openClassDef(className, 8, majorClassVersion, minorClassVersion);
		return this;
	}

	public ClasspathBuilder addClassDef(String className, int release, int majorClassVersion, int minorClassVersion) {
		closeClassDef();
		openClassDef(className, release, majorClassVersion, minorClassVersion);
		return this;
	}

	public ClasspathBuilder addClassDef(ClassDef classDef) {
		closeClassDef();
		classDefs.add(classDef);
		return this;
	}

	public ClasspathBuilder addResourceDef(String resourcePath) {
		String checksum = DigestUtils.sha1Hex(resourcePath); // fake checksum
		return addResourceDef(resourcePath, checksum);
	}

	public ClasspathBuilder addResourceDef(String resourcePath, String checksum) {
		resourceDefs.add(new ResourceDef(resourcePath, checksum));
		return this;
	}

	public ClasspathBuilder addClassRef(String className) {
		if (classRefs == null) throw new IllegalStateException();
		classRefs.add(new ClassRef(className));
		return this;
	}

	public ClasspathBuilder addMethodDef(int access, String methodName, String methodDescriptor) {
		if (methodDefs == null) throw new IllegalStateException();
		methodDefs.add(new MethodDef(access, methodName, methodDescriptor));
		return this;
	}

	public Classpath build() {
		closeClassDef();
		closeJarFile();
		return new Classpath(jarFiles, parentClassLoader, strategy);
	}

	private void openJarFile(String fileName, String coordinates, long fileSize) {
		this.fileName = fileName;
		this.fileSize = fileSize;
		this.coordinates = coordinates;
		this.manifestAttributes = null;
		this.releases = new TreeSet<>();
		this.moduleInfo = ModuleInfo.UNNAMED;
		this.osGiBundleInfo = null;
		this.classDefs = new ArrayList<>();
		this.resourceDefs = new ArrayList<>();
	}

	private void closeJarFile() {
		if (fileName != null) {
			String checksum = DigestUtils.sha1Hex(fileName + fileSize); // fake checksum based on file name and size
			JarFile jarFile = JarFile.withName(fileName)
					.withFileSize(fileSize)
					.withChecksum(checksum)
					.withCoordinates(coordinates)
					.withArtifacts(coordinates != null ? List.of(new Artifact(coordinates)) : List.of())
					.withManifestAttributes(manifestAttributes)
					.withReleases(releases)
					.withModuleInfo(moduleInfo)
					.withOSGiBundleInfo(osGiBundleInfo)
					.withClassDefs(classDefs)
					.withResourceDefs(resourceDefs)
					.build();
			jarFiles.add(jarFile);
			fileName = null;
		}
	}

	private void openClassDef(String className, int release, int majorClassVersion, int minorClassVersion) {
		this.className = className;
		this.release = release;
		this.majorClassVersion = majorClassVersion;
		this.minorClassVersion = minorClassVersion;
		this.classRefs = new ArrayList<>();
		this.methodDefs = new ArrayList<>();
	}

	private void closeClassDef() {
		if (className != null) {
			String classFileChecksum = DigestUtils.sha1Hex(className); // fake checksum
			ClassDef classDef = ClassDef.forClassName(className)
					.setClassFileChecksum(classFileChecksum)
					.setClassLoader(classLoader)
					.setRelease(release)
					.setMajorClassVersion(majorClassVersion)
					.setMinorClassVersion(minorClassVersion);
			classRefs.forEach(classDef::addClassRef);
			methodDefs.forEach(classDef::addMethodDef);
			classDefs.add(classDef);
			className = null;
			classRefs = null;
			methodDefs = null;
		}
	}

}
