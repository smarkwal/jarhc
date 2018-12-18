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

import org.jarhc.utils.JavaVersion;
import org.objectweb.asm.tree.ClassNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Class definition representing a single Java class file.
 */
public class ClassDef implements Comparable<ClassDef> {

	/**
	 * ASM class definition.
	 */
	private final ClassNode classNode;

	/**
	 * List with references to other classes.
	 */
	private final List<ClassRef> classRefs;

	/**
	 * Reference to parent JAR file.
	 */
	private JarFile jarFile;

	/**
	 * Create a class definition for the given class and class references.
	 *
	 * @param classNode ASM class definition
	 * @param classRefs References to other classes
	 * @throws IllegalArgumentException If <code>classNode</code> or <code>classRefs</code> is <code>null</code>
	 */
	public ClassDef(ClassNode classNode, List<ClassRef> classRefs) {
		if (classNode == null) throw new IllegalArgumentException("classNode");
		if (classRefs == null) throw new IllegalArgumentException("classRefs");
		this.classNode = classNode;
		this.classRefs = new ArrayList<>(classRefs);

		// TODO: remove unused information from class node?
	}

	public String getClassName() {
		return classNode.name;
	}

	public int getMajorClassVersion() {
		return classNode.version & 0xFF;
	}

	public int getMinorClassVersion() {
		return classNode.version >> 16;
	}

	/**
	 * Get a human readable Java version string based on the class version.
	 *
	 * @return Java version string (examples: "Java 1.4", "Java 8")
	 * @see JavaVersion#fromClassVersion(int)
	 */
	public String getJavaVersion() {
		return JavaVersion.fromClassVersion(getMajorClassVersion());
	}

	public List<ClassRef> getClassRefs() {
		return Collections.unmodifiableList(classRefs);
	}

	public JarFile getJarFile() {
		return jarFile;
	}

	void setJarFile(JarFile jarFile) {
		this.jarFile = jarFile;
	}

	@Override
	public String toString() {
		return String.format("ClassDef[%s,%d.%d]", getClassName(), getMajorClassVersion(), getMinorClassVersion());
	}

	@Override
	public int compareTo(ClassDef classDef) {
		int diff = this.getClassName().compareTo(classDef.getClassName());
		if (diff != 0) return diff;
		return System.identityHashCode(this) - System.identityHashCode(classDef);
	}

	public static Builder forClassName(String className) {
		return new Builder(className);
	}

	public static class Builder {

		private final ClassNode classNode = new ClassNode();
		private final List<ClassRef> classRefs = new ArrayList<>();

		private Builder(String className) {
			this.classNode.name = className;
			this.classNode.version = 52; // Java 8
		}

		public Builder withVersion(int majorClassVersion, int minorClassVersion) {
			this.classNode.version = majorClassVersion + (minorClassVersion << 16);
			return this;
		}

		public Builder withClassRef(ClassRef classRef) {
			this.classRefs.add(classRef);
			return this;
		}

		public Builder withClassRefs(List<ClassRef> classRefs) {
			this.classRefs.addAll(classRefs);
			return this;
		}

		public ClassDef build() {
			return new ClassDef(classNode, classRefs);
		}

	}

}
