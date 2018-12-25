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
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Loader for class definitions, using a file or a stream as source.
 * This class is thread-safe and can be used in parallel or multiple times in sequence.
 */
public class ClassDefLoader {

	private final ClassFileParser classFileParser = new ClassFileParser();

	/**
	 * Flag used to control whether the Java class should be scanned for
	 * references to other classes, methods and fields.
	 */
	private final boolean scanForReferences;

	/**
	 * Creates a new class definition loader.
	 *
	 * @param scanForReferences Set to <code>true</code> to have this loader find
	 *                          references to other classes, methods and fields.
	 */
	public ClassDefLoader(boolean scanForReferences) {
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

		ClassNode classNode = classFileParser.parse(stream);

		// find all field and method definitions
		List<FieldDef> fieldDefs = getFieldDefs(classNode);
		List<MethodDef> methodDefs = getMethodDefs(classNode);

		List<ClassRef> classRefs = new ArrayList<>();
		List<FieldRef> fieldRefs = new ArrayList<>();
		List<MethodRef> methodRefs = new ArrayList<>();
		if (scanForReferences) {
			// find all references to other classes, fields and methods
			ClassScanner scanner = new ClassScanner();
			scanner.scan(classNode);
			classRefs.addAll(scanner.getClassRefs());
			fieldRefs.addAll(scanner.getFieldRefs());
			methodRefs.addAll(scanner.getMethodRefs());
		}

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

	private List<FieldDef> getFieldDefs(ClassNode classNode) {
		// for every field in the class ...
		List<FieldDef> fieldDefs = new ArrayList<>();
		for (FieldNode fieldNode : classNode.fields) {
			// create field definition
			FieldDef fieldDef = new FieldDef(fieldNode.access, fieldNode.name, fieldNode.desc);
			fieldDefs.add(fieldDef);
		}
		return fieldDefs;
	}

	private List<MethodDef> getMethodDefs(ClassNode classNode) {
		// for every method in the class ...
		List<MethodDef> methodDefs = new ArrayList<>();
		for (MethodNode methodNode : classNode.methods) {
			// create method definition
			MethodDef methodDef = new MethodDef(methodNode.access, methodNode.name, methodNode.desc);
			methodDefs.add(methodDef);
		}
		return methodDefs;
	}

}
