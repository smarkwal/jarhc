/*
 * Copyright 2018 Stephan Markwalder
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

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.jarhc.model.*;
import org.objectweb.asm.tree.ClassNode;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Loader for class definitions, using a file or a stream as source.
 * This class is thread-safe and can be used in parallel or multiple times in sequence.
 */
class ClassDefLoader {

	private final ClassFileParser classFileParser = new ClassFileParser();

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

		// read data, calculate SHA-1 checksum, and re-create stream
		byte[] data = IOUtils.toByteArray(stream);
		String classFileChecksum = DigestUtils.sha1Hex(data);
		stream = new ByteArrayInputStream(data);

		ClassNode classNode = classFileParser.parse(stream);

		// find all fields, methods and references to other classes
		ClassScanner scanner = new ClassScanner();
		scanner.scan(classNode);

		List<FieldDef> fieldDefs = scanner.getFieldDefs();
		List<MethodDef> methodDefs = scanner.getMethodDefs();
		List<ClassRef> classRefs = new ArrayList<>(scanner.getClassRefs());
		List<FieldRef> fieldRefs = new ArrayList<>(scanner.getFieldRefs());
		List<MethodRef> methodRefs = new ArrayList<>(scanner.getMethodRefs());

		// create class definition
		return ClassDef.forClassNode(classNode)
				.withClassFileChecksum(classFileChecksum)
				.withFieldDefs(fieldDefs)
				.withMethodDefs(methodDefs)
				.withClassRefs(classRefs)
				.withFieldRefs(fieldRefs)
				.withMethodRefs(methodRefs)
				.build();
	}

}
