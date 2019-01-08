/*
 * Copyright 2019 Stephan Markwalder
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jarhc.loader;

import org.jarhc.model.ClassDef;
import org.jarhc.utils.DigestUtils;
import org.jarhc.utils.IOUtils;
import org.objectweb.asm.ClassReader;

import java.io.*;

/**
 * Loader for class definitions, using a file or a stream as source.
 * This class is thread-safe and can be used in parallel or multiple times in sequence.
 */
public class ClassDefLoader {

	/**
	 * Name of class loader (will be stored in class definition).
	 */
	private final String classLoader;

	/**
	 * Flag used to control whether the Java class should be scanned for
	 * references to other classes, methods and fields.
	 */
	private final boolean scanForReferences;

	/**
	 * Creates a new class definition loader.
	 *
	 * @param classLoader       Name of class loader, for example "Classpath", "Provided" or "Runtime"
	 * @param scanForReferences Set to <code>true</code> to have this loader find
	 */
	public ClassDefLoader(String classLoader, boolean scanForReferences) {
		this.classLoader = classLoader;
		this.scanForReferences = scanForReferences;
	}

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
	public ClassDef load(File file) throws IOException {
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
	public ClassDef load(InputStream stream) throws IOException {
		if (stream == null) throw new IllegalArgumentException("stream");

		// read data, calculate SHA-1 checksum, and re-create stream
		byte[] data = IOUtils.toByteArray(stream);
		String classFileChecksum = DigestUtils.sha1Hex(data);
		stream = new ByteArrayInputStream(data);

		ClassDefBuilder classDefBuilder = new ClassDefBuilder(scanForReferences);

		ClassReader classReader = new ClassReader(stream);
		classReader.accept(classDefBuilder, 0);

		ClassDef classDef = classDefBuilder.getClassDef();

		classDef.setClassLoader(classLoader);
		classDef.setClassFileChecksum(classFileChecksum);

		return classDef;
	}

}
