package net.markwalder.jarcc.model;

import net.markwalder.jarcc.utils.JavaVersion;

public class ClassDef {

	private final String name;
	private final int version;

	public ClassDef(String name, int version) {
		this.name = name;
		this.version = version;
	}

	public String getName() {
		return name;
	}

	public int getVersion() {
		return version;
	}

	public String getJavaVersion() {
		return JavaVersion.fromClassVersion(version);
	}

}
