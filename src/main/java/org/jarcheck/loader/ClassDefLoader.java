package org.jarcheck.loader;

import org.jarcheck.model.ClassDef;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

import java.io.*;

/**
 * Loader for class definitions, using a file or a stream as source.
 * This class is thread-safe and can be used in parallel or multiple times in sequence.
 */
class ClassDefLoader {

	/**
	 * Load a class definition from the given file.
	 * This method does not check whether the given file
	 * has a correct Java class file name.
	 *
	 * @param file File
	 * @return Class definition
	 * @throws IllegalArgumentException If <code>file</code> is <code>null</code>.
	 * @throws FileNotFoundException    If the file does not exist.
	 * @throws IOException              If the file cannot be parsed.
	 */
	ClassDef load(File file) throws IOException {
		if (file == null) throw new IllegalArgumentException("file");
		if (!file.isFile()) throw new FileNotFoundException(file.getAbsolutePath());

		try (InputStream stream = new FileInputStream(file)) {
			return load(stream);
		}
	}

	/**
	 * Load a class definition from the given input stream.
	 * This method does not close the input stream.
	 *
	 * @param stream Input stream
	 * @return Class definition
	 * @throws IllegalArgumentException If <code>stream</code> is <code>null</code>.
	 * @throws IOException              If the stream cannot be parsed.
	 */
	ClassDef load(InputStream stream) throws IOException {
		if (stream == null) throw new IllegalArgumentException("stream");

		// parse class file with ASM
		ClassNode classNode = new ClassNode();
		ClassReader classReader = new ClassReader(stream);
		classReader.accept(classNode, 0);

		// create class definition
		return new ClassDef(classNode.name, classNode.version);
	}

}
