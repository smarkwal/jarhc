package net.markwalder.jarcc.utils;

public class JavaVersion {

	public static final int MIN_CLASS_VERSION = 45;

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
