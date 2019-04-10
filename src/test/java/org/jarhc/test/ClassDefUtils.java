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

package org.jarhc.test;

import org.jarhc.model.*;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class ClassDefUtils {

	static ClassDef read(DataInputStream stream) throws IOException {

		String className = stream.readUTF();
		String superName = stream.readBoolean() ? stream.readUTF() : null;
		int numInterfaceNames = stream.readInt();
		List<String> interfaceNames = new ArrayList<>(numInterfaceNames);
		if (numInterfaceNames > 0) {
			for (int i = 0; i < numInterfaceNames; i++) {
				interfaceNames.add(stream.readUTF());
			}
		}

		// read meta data
		int classAccess = stream.readInt();
		String classLoader = stream.readUTF();
		String classFileChecksum = stream.readUTF();
		int majorClassVersion = stream.readInt();
		int minorClassVersion = stream.readInt();

		ClassDef classDef = ClassDef.forClassName(className)
				.setClassLoader(classLoader)
				.setClassFileChecksum(classFileChecksum)
				.setMajorClassVersion(majorClassVersion)
				.setMinorClassVersion(minorClassVersion)
				.setSuperName(superName)
				.addInterfaceNames(interfaceNames);
		classDef.setAccess(classAccess);

		// read class annotations
		readAnnotationRefs(stream, classDef);

		// read field definitions
		int numFieldDefs = stream.readInt();
		for (int f = 0; f < numFieldDefs; f++) {
			int access = stream.readInt();
			String fieldName = stream.readUTF();
			String fieldType = stream.readUTF();
			FieldDef fieldDef = new FieldDef(access, fieldName, fieldType);

			// read field annotations
			readAnnotationRefs(stream, fieldDef);

			classDef.addFieldDef(fieldDef);
		}

		// read method definitions
		int numMethodDefs = stream.readInt();
		for (int m = 0; m < numMethodDefs; m++) {
			int access = stream.readInt();
			String methodName = stream.readUTF();
			String methodDescriptor = stream.readUTF();
			MethodDef methodDef = new MethodDef(access, methodName, methodDescriptor);

			// read method annotations
			readAnnotationRefs(stream, methodDef);

			classDef.addMethodDef(methodDef);
		}

		return classDef;
	}

	private static void readAnnotationRefs(DataInputStream stream, Def def) throws IOException {
		int num = stream.readInt();
		for (int a = 0; a < num; a++) {
			String className = stream.readUTF();
			AnnotationRef annotationRef = new AnnotationRef(className);
			def.addAnnotationRef(annotationRef);
		}
	}

	static void write(ClassDef classDef, DataOutputStream stream) throws IOException {

		String className = classDef.getClassName();
		String superName = classDef.getSuperName();
		List<String> interfaceNames = classDef.getInterfaceNames();
		List<FieldDef> fieldDefs = classDef.getFieldDefs();
		List<MethodDef> methodDefs = classDef.getMethodDefs();

		// write class name, superclass, and interfaces
		stream.writeUTF(className);
		if (superName != null) {
			stream.writeBoolean(true);
			stream.writeUTF(superName);
		} else {
			stream.writeBoolean(false);
		}
		stream.writeInt(interfaceNames.size());
		for (String interfaceName : interfaceNames) {
			stream.writeUTF(interfaceName);
		}

		// write meta data
		stream.writeInt(classDef.getAccess());
		stream.writeUTF(classDef.getClassLoader());
		stream.writeUTF(classDef.getClassFileChecksum());
		stream.writeInt(classDef.getMajorClassVersion());
		stream.writeInt(classDef.getMinorClassVersion());

		// write class annotations
		writeAnnotationRefs(stream, classDef);

		// write field definitions
		stream.writeInt(fieldDefs.size());
		for (FieldDef fieldDef : fieldDefs) {
			stream.writeInt(fieldDef.getAccess());
			stream.writeUTF(fieldDef.getFieldName());
			stream.writeUTF(fieldDef.getFieldType());

			// write field annotations
			writeAnnotationRefs(stream, fieldDef);
		}

		// write method definitions
		stream.writeInt(methodDefs.size());
		for (MethodDef methodDef : methodDefs) {
			stream.writeInt(methodDef.getAccess());
			stream.writeUTF(methodDef.getMethodName());
			stream.writeUTF(methodDef.getMethodDescriptor());

			// write field annotations
			writeAnnotationRefs(stream, methodDef);
		}

	}

	private static void writeAnnotationRefs(DataOutputStream stream, Def def) throws IOException {
		List<AnnotationRef> annotationRefs = def.getAnnotationRefs();
		stream.writeInt(annotationRefs.size());
		for (AnnotationRef annotationRef : annotationRefs) {
			stream.writeUTF(annotationRef.getClassName());
		}
	}

}
