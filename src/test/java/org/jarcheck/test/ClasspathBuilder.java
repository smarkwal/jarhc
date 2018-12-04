package org.jarcheck.test;

import org.jarcheck.model.ClassDef;
import org.jarcheck.model.Classpath;
import org.jarcheck.model.JarFile;

import java.util.ArrayList;
import java.util.List;

public class ClasspathBuilder {

	// Classpath properties
	private final List<JarFile> jarFiles = new ArrayList<>();

	// JarFile properties
	private String fileName;
	private long fileSize;
	private List<ClassDef> classDefs;

	// ClassDef properties
	private String className;
	private int majorClassVersion;
	private int minorClassVersion;

	private ClasspathBuilder() {
	}

	public static ClasspathBuilder create() {
		return new ClasspathBuilder();
	}

	public ClasspathBuilder addJarFile(String fileName) {
		return addJarFile(fileName, 1024);
	}

	public ClasspathBuilder addJarFile(String fileName, long fileSize) {
		closeClassDef();
		closeJarFile();
		openJarFile(fileName, fileSize);
		return this;
	}

	public ClasspathBuilder addClassDef(String className) {
		return addClassDef(className, 52, 0);
	}

	public ClasspathBuilder addClassDef(String className, int majorClassVersion, int minorClassVersion) {
		closeClassDef();
		openClassDef(className, majorClassVersion, minorClassVersion);
		return this;
	}

	public Classpath build() {
		closeClassDef();
		closeJarFile();
		return new Classpath(jarFiles);
	}

	private void openJarFile(String fileName, long fileSize) {
		this.fileName = fileName;
		this.fileSize = fileSize;
		this.classDefs = new ArrayList<>();
	}

	private void closeJarFile() {
		if (fileName != null) {
			jarFiles.add(new JarFile(fileName, fileSize, classDefs));
			fileName = null;
		}
	}

	private void openClassDef(String className, int majorClassVersion, int minorClassVersion) {
		this.className = className;
		this.majorClassVersion = majorClassVersion;
		this.minorClassVersion = minorClassVersion;
	}

	private void closeClassDef() {
		if (className != null) {
			classDefs.add(new ClassDef(className, majorClassVersion, minorClassVersion));
			className = null;
		}
	}

}
