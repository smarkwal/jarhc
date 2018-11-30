package net.markwalder.jarcc.utils;

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
		if (classVersion < MIN_CLASS_VERSION) {
			return String.format("[unknown:%d]", classVersion);
		} else if (classVersion < 49) {
			return String.format("Java 1.%d", classVersion - MIN_CLASS_VERSION + 1);
		} else {
			return String.format("Java %d", classVersion - MIN_CLASS_VERSION + 1);
		}
	}

}
