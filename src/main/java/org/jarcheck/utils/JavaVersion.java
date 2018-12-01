package org.jarcheck.utils;

public class JavaVersion {

	/**
	 * Minimum class version (file format) 45 for Java 1.1.
	 */
	public static final int MIN_CLASS_VERSION = 45;

	/**
	 * Get a human readable Java version string for the given class version.
	 *
	 * @param classVersion Class version
	 * @return Java version string
	 */
	public static String fromClassVersion(int classVersion) {
		int version = getJavaVersionNumber(classVersion);
		if (version < 1) {
			return String.format("[unknown:%d]", classVersion);
		} else if (version < 5) {
			return String.format("Java 1.%d", version);
		} else {
			return String.format("Java %d", version);
		}
	}

	private static int getJavaVersionNumber(int classVersion) {
		return classVersion - MIN_CLASS_VERSION + 1;
	}

}
