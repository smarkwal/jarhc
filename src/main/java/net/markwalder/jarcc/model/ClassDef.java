package net.markwalder.jarcc.model;

import net.markwalder.jarcc.utils.JavaVersion;

public class ClassDef {

	private final String className;
	private final int classVersion;

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

	public String getJavaVersion() {
		return JavaVersion.fromClassVersion(classVersion);
	}

}
