package org.jarcheck.model;

import org.jarcheck.utils.JavaVersion;

/**
 * Class definition representing a single Java class file.
 */
public class ClassDef {

	/**
	 * Class name in the form "org/something/MyClass".
	 */
	private final String className;

	/**
	 * Major version number of the class file format.
	 * See https://en.wikipedia.org/wiki/Java_class_file
	 */
	private final int classVersion;

	/**
	 * Create a class definition for the given class name and class version.
	 *
	 * @param className    Class name
	 * @param classVersion Class version
	 * @throws IllegalArgumentException If <code>className</code> is <code>null</code>
	 *                                  or <code>classVersion</code> is less than {@link JavaVersion#MIN_CLASS_VERSION 45}.
	 */
	public ClassDef(String className, int classVersion) {
		if (className == null) throw new IllegalArgumentException("className");
		if (classVersion < JavaVersion.MIN_CLASS_VERSION) throw new IllegalArgumentException("classVersion");
		this.className = className;
		this.classVersion = classVersion;
	}

	public String getClassName() {
		return className;
	}

	public int getClassVersion() {
		return classVersion;
	}

	/**
	 * Get a human readable Java version string based on the class version.
	 *
	 * @return Java version string (examples: "Java 1.4", "Java 8")
	 * @see JavaVersion#fromClassVersion(int)
	 */
	public String getJavaVersion() {
		return JavaVersion.fromClassVersion(classVersion);
	}

	@Override
	public String toString() {
		return String.format("ClassDef[%s,%d]", className, classVersion);
	}

}
