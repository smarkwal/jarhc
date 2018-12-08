package org.jarcheck.loader;

import org.jarcheck.model.Classpath;
import org.jarcheck.model.JarFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Loader for a classpath.
 * This class is thread-safe and can be used in parallel or multiple times in sequence.
 */
public class ClasspathLoader {

	private final JarFileLoader jarFileLoader = new JarFileLoader();

	/**
	 * Create a classpath with the given JAR files.
	 *
	 * @param files List of JAR files.
	 * @return Classpath
	 * @throws IllegalArgumentException If <code>files</code> is <code>null</code>.
	 * @throws FileNotFoundException    If any of the files does not exist.
	 * @throws IOException              If a JAR file cannot be parsed.
	 */
	public Classpath load(List<File> files) throws IOException {
		if (files == null) throw new IllegalArgumentException("files");

		// load all JAR files
		List<JarFile> jarFiles = new ArrayList<>(files.size());
		for (File file : files) {
			JarFile jarFile;
			try {
				jarFile = jarFileLoader.load(file);
			} catch (IOException e) {
				String message = String.format("Unable to parse JAR file: %s", file.getAbsolutePath());
				throw new IOException(message, e);
			}
			jarFiles.add(jarFile);
		}

		return new Classpath(jarFiles);
	}

}
