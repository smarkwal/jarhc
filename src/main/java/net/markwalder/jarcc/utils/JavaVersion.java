package net.markwalder.jarcc.utils;

public class JavaVersion {

	public static String fromClassVersion(int classVersion) {
		if (classVersion < 45) {
			return String.format("[unknown:%d]", classVersion);
		} else if (classVersion < 49) {
			return String.format("Java 1.%d", classVersion - 44);
		} else {
			return String.format("Java %d", classVersion - 44);
		}
	}

}
