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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Class definition representing a single Java class file.
 */
public class ClassDef implements Comparable<ClassDef> {

	/**
	 * Class name in the form "org/something/MyClass".
	 */
	private final String className;

	/**
	 * Major version number of the class file format.
	 * See https://en.wikipedia.org/wiki/Java_class_file
	 */
	private final int majorClassVersion;

	/**
	 * Minor version number of the class file format.
	 * See https://en.wikipedia.org/wiki/Java_class_file
	 */
	private final int minorClassVersion;

	/**
	 * List with references to other classes.
	 */
	private final List<ClassRef> classRefs;

	/**
	 * Reference to parent JAR file.
	 */
	private JarFile jarFile;

	/**
	 * Create a class definition for the given class name and class version.
	 *
	 * @param className         Class name
	 * @param majorClassVersion Major class version
	 * @param minorClassVersion Minor class version
	 * @param classRefs         References to other classes
	 * @throws IllegalArgumentException If <code>className</code> is <code>null</code>
	 *                                  or <code>majorClassVersion</code> is less than {@link JavaVersion#MIN_CLASS_VERSION 45}
	 *                                  or <code>minorClassVersion</code> is less than 0
	 *                                  or <code>classRefs</code> is <code>null</code>.
	 */
	public ClassDef(String className, int majorClassVersion, int minorClassVersion, List<ClassRef> classRefs) {
		if (className == null) throw new IllegalArgumentException("className");
		if (majorClassVersion < JavaVersion.MIN_CLASS_VERSION) throw new IllegalArgumentException("majorClassVersion");
		if (minorClassVersion < 0) throw new IllegalArgumentException("minorClassVersion");
		if (classRefs == null) throw new IllegalArgumentException("classRefs");
		this.className = className;
		this.majorClassVersion = majorClassVersion;
		this.minorClassVersion = minorClassVersion;
		this.classRefs = new ArrayList<>(classRefs);
	}

	public String getClassName() {
		return className;
	}

	public int getMajorClassVersion() {
		return majorClassVersion;
	}

	public int getMinorClassVersion() {
		return minorClassVersion;
	}

	/**
	 * Get a human readable Java version string based on the class version.
	 *
	 * @return Java version string (examples: "Java 1.4", "Java 8")
	 * @see JavaVersion#fromClassVersion(int)
	 */
	public String getJavaVersion() {
		return JavaVersion.fromClassVersion(majorClassVersion);
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
		return String.format("ClassDef[%s,%d.%d]", className, majorClassVersion, minorClassVersion);
	}

	@Override
	public int compareTo(ClassDef classDef) {
		int diff = this.className.compareTo(classDef.className);
		if (diff != 0) return diff;
		return System.identityHashCode(this) - System.identityHashCode(classDef);
	}

}
