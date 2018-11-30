package net.markwalder.jarcc.loader;

import net.markwalder.jarcc.model.ClassDef;
import net.markwalder.jarcc.model.JarFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;

/**
 * Loader for a JAR file, using a file as source.
 * This class is thread-safe and can be used in parallel or multiple times in sequence.
 */
public class JarFileLoader {

	private final ClassDefLoader classDefLoader = new ClassDefLoader();

	/**
	 * Load a JAR file from the given file.
	 * This method does not check whether the given file
	 * has a correct JAR file name.
	 *
	 * @param file File
	 * @return JAR file
	 * @throws IllegalArgumentException If <code>file</code> is <code>null</code>.
	 * @throws FileNotFoundException    If the file does not exist.
	 * @throws IOException              If the file cannot be parsed.
	 */
	public JarFile load(File file) throws IOException {
		if (file == null) throw new IllegalArgumentException("file");
		if (!file.isFile()) throw new FileNotFoundException(file.getAbsolutePath());

		List<ClassDef> classDefs = new ArrayList<>();

		// open JAR file for reading
		try (java.util.jar.JarFile jarFile = new java.util.jar.JarFile(file)) {

			// for every entry in the JAR file ...
			Enumeration<JarEntry> entries = jarFile.entries();
			while (entries.hasMoreElements()) {
				JarEntry entry = entries.nextElement();

				// skip directories
				if (entry.isDirectory()) {
					continue;
				}

				// only accept *.class files
				if (!entry.getName().endsWith(".class")) {
					continue;
				}

				// load class file
				InputStream stream = jarFile.getInputStream(entry);
				ClassDef classDef = classDefLoader.load(stream);

				classDefs.add(classDef);
			}
		}

		return new JarFile(file.getName(), classDefs);
	}

}
