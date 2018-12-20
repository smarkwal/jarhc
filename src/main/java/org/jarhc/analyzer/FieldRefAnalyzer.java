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

package org.jarhc.analyzer;

import org.jarhc.env.JavaRuntime;
import org.jarhc.model.*;
import org.jarhc.report.ReportSection;
import org.jarhc.report.ReportTable;

import java.util.*;

import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;

public class FieldRefAnalyzer extends Analyzer {

	@Override
	public ReportSection analyze(Classpath classpath) {

		ReportTable table = buildTable(classpath);

		ReportSection section = new ReportSection("Field References", "Problems with field access.");
		section.add(table);
		return section;
	}

	private ReportTable buildTable(Classpath classpath) {

		ReportTable table = new ReportTable("JAR File", "Errors");

		// for every JAR file ...
		List<JarFile> jarFiles = classpath.getJarFiles();
		for (JarFile jarFile : jarFiles) {

			Set<String> errorMessages = new LinkedHashSet<>();

			// for every class definition ...
			List<ClassDef> classDefs = jarFile.getClassDefs();
			for (ClassDef classDef : classDefs) {

				// for every field reference ...
				List<FieldRef> fieldRefs = classDef.getFieldRefs();
				for (FieldRef fieldRef : fieldRefs) {

					// validate field reference
					errorMessages.addAll(validateFieldRef(classDef, fieldRef, classpath));
				}
			}

			if (!errorMessages.isEmpty()) {
				table.addRow(jarFile.getFileName(), String.join(System.lineSeparator(), errorMessages));
			}
		}

		return table;
	}

	private List<String> validateFieldRef(ClassDef classDef, FieldRef fieldRef, Classpath classpath) {

		List<String> errorMessages = new ArrayList<>();

		// find target field definition
		String targetClassName = fieldRef.getFieldOwner();
		Optional<FieldDef> fieldDef = findFieldDef(fieldRef, targetClassName, classpath);
		if (!fieldDef.isPresent()) {
			errorMessages.add("Field not found: " + fieldRef.getDisplayName());
			return errorMessages;
		}

		// TODO: add reference from FieldDef to ClassDef (declaring class)

		// check access level
		// TODO: handle inner classes
		FieldDef field = fieldDef.get();
		if (field.isPublic()) {
			// OK
		} else if (field.isPrivate()) {
			// TODO: access only inside same class
		} else if (field.isProtected()) {
			// TODO: access only in same package or subclass
		} else {
			// TODO: access only in same package
		}

		// check field type
		if (!field.getFieldDescriptor().equals(fieldRef.getFieldDescriptor())) {
			errorMessages.add("Incompatible field type: " + fieldRef.getDisplayName() + " -> " + field.getDisplayName());
		}

		// check static/instance
		if (field.isStatic()) {
			if (!fieldRef.isStaticAccess()) {
				errorMessages.add("Instance access to static field: " + fieldRef.getDisplayName() + " -> " + field.getDisplayName());
			}
		} else {
			if (fieldRef.isStaticAccess()) {
				errorMessages.add("Static access to instance field: " + fieldRef.getDisplayName() + " -> " + field.getDisplayName());
			}
		}

		// check access to final fields
		// TODO: this check generate false-positives for when the final field is initialized
		/*
		if (field.isFinal()) {
			if (fieldRef.isWriteAccess()) {
				errorMessages.add("Write access to final field: " + fieldRef.getDisplayName() + " -> " + field.getDisplayName());
			}
		}
		*/

		// TODO: more checks ...?

		return errorMessages;
	}

	private Optional<FieldDef> findFieldDef(FieldRef fieldRef, String targetClassName, Classpath classpath) {

		// TODO: use a cache for field definitions like System.out, System.err, ...

		String fieldName = fieldRef.getFieldName();

		Set<ClassDef> targetClassDefs = classpath.getClassDefs(targetClassName);
		if (targetClassDefs == null || targetClassDefs.isEmpty()) {
			// target class not found in classpath

			// TODO: remove this hack
			if (targetClassName.equals("java/lang/Object")) {
				return Optional.empty();
			}

			// if class is a JDK/JRE class ...
			String javaClassName = targetClassName.replace('/', '.');
			String classLoaderName = JavaRuntime.getDefault().getClassLoaderName(javaClassName);
			if (classLoaderName != null) {
				// TODO: search for field in Java class
				//  if field is found, create and return a FieldDef for this field
			} else {
				if (targetClassName.equals(fieldRef.getFieldOwner())) {
					// owner class not found
					// TODO: return a special value so that this fieldRef is not validated
					//  since the missing class has already been reported
				} else {
					// superclass or superinterface not found
					// -> field not found in owner class
					return Optional.empty();
				}
			}

			// TODO: rethink: create a fake-field to avoid an error
			int access = fieldRef.isStaticAccess() ? ACC_PUBLIC + ACC_STATIC : ACC_PUBLIC;
			return Optional.of(new FieldDef(access, fieldName, fieldRef.getFieldDescriptor()));
		}

		for (ClassDef targetClassDef : targetClassDefs) {

			Optional<FieldDef> fieldDef = targetClassDef.getFieldDef(fieldName);
			if (fieldDef.isPresent()) return fieldDef;

			// try to find field in superclass
			String superName = targetClassDef.getSuperName();
			if (superName != null) {
				fieldDef = findFieldDef(fieldRef, superName, classpath);
				if (fieldDef.isPresent()) return fieldDef;
			}

			// try to find field in superinterfaces
			List<String> interfaceNames = targetClassDef.getInterfaceNames();
			if (interfaceNames != null) {
				for (String interfaceName : interfaceNames) {
					fieldDef = findFieldDef(fieldRef, interfaceName, classpath);
					if (fieldDef.isPresent()) {
						return fieldDef;
					}
				}
			}

		}

		// field not found
		return Optional.empty();

	}

}
