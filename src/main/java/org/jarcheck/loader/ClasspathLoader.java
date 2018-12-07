package org.jarcheck.loader;

import org.jarcheck.model.Classpath;
import org.jarcheck.model.JarFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * Loader for a classpath.
 * This class is thread-safe and can be used in parallel or multiple times in sequence.
 */
public class ClasspathLoader {

	private final JarFileLoader jarFileLoader = new JarFileLoader();

	/**
	 * Create a classpath by finding all *.jar files in the given directory.
	 * If <code>recursive</code> is <code>true</code>, subdirectories will also be scanned.
	 *
	 * @param directory Directory
	 * @param recursive <code>true</code> for scanning subdirectories.
	 * @return Classpath
	 * @throws IllegalArgumentException If <code>directory</code> is <code>null</code>.
	 * @throws FileNotFoundException    If the directory does not exist.
	 * @throws IOException              If a JAR file cannot be parsed.
	 */
	public Classpath load(File directory, boolean recursive) throws IOException {
		if (directory == null) throw new IllegalArgumentException("directory");
		if (!directory.isDirectory()) throw new FileNotFoundException(directory.getAbsolutePath());

		// find all *.jar files
		List<File> files = new ArrayList<>();
		collectFiles(directory, files, recursive);

		return load(files);
	}

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
		List<JarFile> jarFiles = new ArrayList<>();
		for (File file : files) {
			JarFile jarFile;
			try {
				jarFile = jarFileLoader.load(file);
			} catch (IOException e) {
				throw new IOException(String.format("Unable to parse JAR file: %s", file.getAbsolutePath()), e);
			}
			jarFiles.add(jarFile);
		}

		return new Classpath(jarFiles);
	}

	/**
	 * Find all *.jar files in the given directory.
	 *
	 * @param directory Directory
	 * @param files     List of collected *.jar files
	 * @param recursive <code>true</code> for scanning subdirectories.
	 */
	private static void collectFiles(File directory, List<File> files, boolean recursive) {
		File[] array = directory.listFiles();
		if (array == null) return;

		// sort files alphabetically and case-sensitive by name
		// TODO: is there a better order?
		Arrays.sort(array, Comparator.comparing(File::getName));

		for (File file : array) {
			if (file.isDirectory()) {
				if (recursive) {
					collectFiles(file, files, true);
				}
			} else if (file.isFile()) {
				if (file.getName().endsWith(".jar")) {
					files.add(file);
				}
			}
		}
	}

}
