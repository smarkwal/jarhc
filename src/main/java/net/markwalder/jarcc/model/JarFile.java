package net.markwalder.jarcc.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class JarFile {

	private final String fileName;
	private final List<ClassDef> classDefs;

	public JarFile(String fileName, List<ClassDef> classDefs) {
		if (fileName == null) throw new IllegalArgumentException("fileName");
		if (classDefs == null) throw new IllegalArgumentException("classDefs");
		this.fileName = fileName;
		this.classDefs = new ArrayList<>(classDefs);
	}

	public String getFileName() {
		return fileName;
	}

	public List<ClassDef> getClassDefs() {
		return Collections.unmodifiableList(classDefs);
	}

}
