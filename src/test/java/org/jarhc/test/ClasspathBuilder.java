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

import org.jarhc.model.ClassDef;
import org.jarhc.model.ClassRef;
import org.jarhc.model.Classpath;
import org.jarhc.model.JarFile;

import java.util.ArrayList;
import java.util.List;

public class ClasspathBuilder {

	// Classpath properties
	private final List<JarFile> jarFiles = new ArrayList<>();

	// JarFile properties
	private String fileName;
	private long fileSize;
	private List<ClassDef> classDefs;

	// ClassDef properties
	private String className;
	private int majorClassVersion;
	private int minorClassVersion;
	private List<ClassRef> classRefs;

	private ClasspathBuilder() {
	}

	public static ClasspathBuilder create() {
		return new ClasspathBuilder();
	}

	public ClasspathBuilder addJarFile(String fileName) {
		return addJarFile(fileName, 1024);
	}

	public ClasspathBuilder addJarFile(String fileName, long fileSize) {
		closeClassDef();
		closeJarFile();
		openJarFile(fileName, fileSize);
		return this;
	}

	public ClasspathBuilder addClassDef(String className) {
		return addClassDef(className, 52, 0);
	}

	public ClasspathBuilder addClassDef(String className, int majorClassVersion, int minorClassVersion) {
		closeClassDef();
		openClassDef(className, majorClassVersion, minorClassVersion);
		return this;
	}

	public ClasspathBuilder addClassRef(String className) {
		if (classRefs == null) throw new IllegalStateException();
		classRefs.add(new ClassRef(className));
		return this;
	}

	public Classpath build() {
		closeClassDef();
		closeJarFile();
		return new Classpath(jarFiles);
	}

	private void openJarFile(String fileName, long fileSize) {
		this.fileName = fileName;
		this.fileSize = fileSize;
		this.classDefs = new ArrayList<>();
	}

	private void closeJarFile() {
		if (fileName != null) {
			jarFiles.add(new JarFile(fileName, fileSize, classDefs));
			fileName = null;
		}
	}

	private void openClassDef(String className, int majorClassVersion, int minorClassVersion) {
		this.className = className;
		this.majorClassVersion = majorClassVersion;
		this.minorClassVersion = minorClassVersion;
		this.classRefs = new ArrayList<>();
	}

	private void closeClassDef() {
		if (className != null) {
			classDefs.add(new ClassDef(className, majorClassVersion, minorClassVersion, classRefs));
			className = null;
			classRefs = null;
		}
	}

}