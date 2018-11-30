package net.markwalder.jarcc.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a set of JAR files found on a Java classpath.
 */
public class Classpath {

	/**
	 * List of JAR files on the classpath.
	 */
	private final List<JarFile> jarFiles;

	/**
	 * Create a new classpath with the given JAR files.
	 *
	 * @param jarFiles JAR files
	 * @throws IllegalArgumentException If <code>jarFiles</code> is <code>null</code>.
	 */
	public Classpath(List<JarFile> jarFiles) {
		if (jarFiles == null) throw new IllegalArgumentException("jarFiles");
		this.jarFiles = new ArrayList<>(jarFiles);
	}

	/**
	 * Returns an unmodifiable list of JAR files.
	 *
	 * @return JAR files
	 */
	public List<JarFile> getJarFiles() {
		return Collections.unmodifiableList(jarFiles);
	}

	@Override
	public String toString() {
		return String.format("Classpath[%d]", jarFiles.size());
	}

}
