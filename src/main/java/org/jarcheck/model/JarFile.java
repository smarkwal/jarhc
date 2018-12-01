package org.jarcheck.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Represents the content of a JAR file.
 */
public class JarFile {

	/**
	 * JAR file name
	 */
	private final String fileName;

	/**
	 * JAR file size in bytes
	 */
	private final long fileSize;

	/**
	 * List of class definitions for classes found in the JAR file.
	 */
	private final List<ClassDef> classDefs;

	/**
	 * Create a new JAR file given the file name and the list of class definitions.
	 *
	 * @param fileName  JAR file name
	 * @param classDefs Class definitions
	 * @throws IllegalArgumentException If <code>fileName</code> or <code>classDefs</code> is <code>null</code>.
	 */
	public JarFile(String fileName, long fileSize, List<ClassDef> classDefs) {
		if (fileName == null) throw new IllegalArgumentException("fileName");
		if (classDefs == null) throw new IllegalArgumentException("classDefs");
		this.fileName = fileName;
		this.fileSize = fileSize;
		this.classDefs = new ArrayList<>(classDefs);

		// sort class definitions by class name (case-sensitive)
		this.classDefs.sort(Comparator.comparing(ClassDef::getClassName));
	}

	public String getFileName() {
		return fileName;
	}

	public long getFileSize() {
		return fileSize;
	}

	/**
	 * Returns an unmodifiable list of class definitions.
	 *
	 * @return Class definitions
	 */
	public List<ClassDef> getClassDefs() {
		return Collections.unmodifiableList(classDefs);
	}

	@Override
	public String toString() {
		return String.format("JarFile[%s,%d]", fileName, classDefs.size());
	}

}
