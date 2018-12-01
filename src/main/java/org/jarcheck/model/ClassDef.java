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
	private final int majorClassVersion;

	/**
	 * Minor version number of the class file format.
	 * See https://en.wikipedia.org/wiki/Java_class_file
	 */
	private final int minorClassVersion;

	/**
	 * Create a class definition for the given class name and class version.
	 *
	 * @param className         Class name
	 * @param majorClassVersion Class version
	 * @throws IllegalArgumentException If <code>className</code> is <code>null</code>
	 *                                  or <code>majorClassVersion</code> is less than {@link JavaVersion#MIN_CLASS_VERSION 45}.
	 *                                  or <code>minorClassVersion</code> is less than 0.
	 */
	public ClassDef(String className, int majorClassVersion, int minorClassVersion) {
		if (className == null) throw new IllegalArgumentException("className");
		if (majorClassVersion < JavaVersion.MIN_CLASS_VERSION) throw new IllegalArgumentException("majorClassVersion");
		if (minorClassVersion < 0) throw new IllegalArgumentException("minorClassVersion");
		this.className = className;
		this.majorClassVersion = majorClassVersion;
		this.minorClassVersion = minorClassVersion;
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

	@Override
	public String toString() {
		return String.format("ClassDef[%s,%d.%d]", className, majorClassVersion, minorClassVersion);
	}

}
