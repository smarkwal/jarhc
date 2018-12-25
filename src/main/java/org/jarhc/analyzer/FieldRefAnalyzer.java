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

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.jarhc.utils.StringUtils.joinLines;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;

public class FieldRefAnalyzer extends Analyzer {

	private final JavaRuntime javaRuntime;
	private final boolean reportOwnerClassNotFound;

	public FieldRefAnalyzer(JavaRuntime javaRuntime, boolean reportOwnerClassNotFound) {
		if (javaRuntime == null) throw new IllegalArgumentException("javaRuntime");
		this.javaRuntime = javaRuntime;
		this.reportOwnerClassNotFound = reportOwnerClassNotFound;
	}

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
					SearchResult result = validateFieldRef(classDef, fieldRef, classpath);
					if (!result.isIgnoreResult()) {
						String text = result.getResult();
						if (text != null) {
							errorMessages.add(text);
						}
					}
				}
			}

			if (!errorMessages.isEmpty()) {
				table.addRow(jarFile.getFileName(), joinLines(errorMessages));
			}
		}

		return table;
	}

	private SearchResult validateFieldRef(ClassDef classDef, FieldRef fieldRef, Classpath classpath) {

		SearchResult searchResult = new SearchResult();

		// find target field definition
		String targetClassName = fieldRef.getFieldOwner();
		Optional<FieldDef> fieldDef = findFieldDef(fieldRef, targetClassName, classpath, searchResult);
		if (!fieldDef.isPresent()) {
			searchResult.addErrorMessage("Field not found: " + fieldRef.getDisplayName());
			return searchResult;
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
			searchResult.addErrorMessage("Incompatible field type: " + fieldRef.getDisplayName() + " -> " + field.getDisplayName());
		}

		// check static/instance
		if (field.isStatic()) {
			if (!fieldRef.isStaticAccess()) {
				searchResult.addErrorMessage("Instance access to static field: " + fieldRef.getDisplayName() + " -> " + field.getDisplayName());
			}
		} else {
			if (fieldRef.isStaticAccess()) {
				searchResult.addErrorMessage("Static access to instance field: " + fieldRef.getDisplayName() + " -> " + field.getDisplayName());
			}
		}

		// check access to final fields
		if (field.isFinal()) {
			if (fieldRef.isWriteAccess()) {
				searchResult.addErrorMessage("Write access to final field: " + fieldRef.getDisplayName() + " -> " + field.getDisplayName());
			}
		}

		// TODO: more checks ...?

		return searchResult;
	}

	private Optional<FieldDef> findFieldDef(FieldRef fieldRef, String targetClassName, Classpath classpath, SearchResult searchResult) {

		// TODO: use a cache for field definitions like System.out, System.err, ...

		String fieldName = fieldRef.getFieldName();
		String realClassName = targetClassName.replace('/', '.');

		Set<ClassDef> targetClassDefs = classpath.getClassDefs(targetClassName);
		if (targetClassDefs == null || targetClassDefs.isEmpty()) {
			// target class not found in classpath

			// TODO: remove this hack
			if (targetClassName.equals("java/lang/Object")) {
				// assumption: java.lang.Object anyway does not contain any fields
				searchResult.addSearchInfo("- " + realClassName + " (field not found)");
				return Optional.empty();
			}

			// if class is a JDK/JRE class ...
			Optional<String> classLoaderName = javaRuntime.getClassLoaderName(realClassName);
			if (classLoaderName.isPresent()) {
				// TODO: search for field in Java class
				//  if field is found, create and return a FieldDef for this field
				//  if field is not found, search in superclass and superinterfaces

				// TODO: rethink: create a fake-field to avoid an error
				int access = fieldRef.isStaticAccess() ? ACC_PUBLIC + ACC_STATIC : ACC_PUBLIC;
				return Optional.of(new FieldDef(access, fieldName, fieldRef.getFieldDescriptor()));

			}

			if (targetClassName.equals(fieldRef.getFieldOwner())) {
				// owner class not found
				if (reportOwnerClassNotFound) {
					searchResult.addSearchInfo("- " + realClassName + " (owner class not found)");
				} else {
					// ignore result if owner class is not found
					// (already reported in missing classes)
					searchResult.setIgnoreResult();
				}
			} else {
				// superclass or superinterface of owner class not found
				searchResult.addSearchInfo("- " + realClassName + " (class not found)");
			}

			return Optional.empty();
		}

		for (ClassDef targetClassDef : targetClassDefs) {

			Optional<FieldDef> fieldDef = targetClassDef.getFieldDef(fieldName);
			if (fieldDef.isPresent()) {
				searchResult.addSearchInfo("- " + realClassName + " (field found)");
				return fieldDef;
			}

			searchResult.addSearchInfo("- " + realClassName + " (field not found)");

			// try to find field in superclass
			String superName = targetClassDef.getSuperName();
			if (superName != null) {
				fieldDef = findFieldDef(fieldRef, superName, classpath, searchResult);
				if (fieldDef.isPresent()) {
					return fieldDef;
				}
			}

			// try to find field in interfaces
			// TODO: scan interfaces before superclass?
			List<String> interfaceNames = targetClassDef.getInterfaceNames();
			if (interfaceNames != null) {
				for (String interfaceName : interfaceNames) {
					fieldDef = findFieldDef(fieldRef, interfaceName, classpath, searchResult);
					if (fieldDef.isPresent()) {
						return fieldDef;
					}
				}
			}

		}

		// field not found
		return Optional.empty();

	}

	private class SearchResult {

		private StringBuilder errorMessages;
		private StringBuilder searchInfos;
		private boolean ignoreResult = false;

		void addErrorMessage(String message) {
			if (errorMessages == null) {
				errorMessages = new StringBuilder();
			} else {
				errorMessages.append(System.lineSeparator());
			}
			errorMessages.append(message);
		}

		void addSearchInfo(String info) {
			if (searchInfos == null) {
				searchInfos = new StringBuilder();
			} else {
				searchInfos.append(System.lineSeparator());
			}
			searchInfos.append(info);
		}

		String getResult() {
			if (errorMessages == null) {
				return null;
			} else {
				if (searchInfos != null) {
					errorMessages.append(System.lineSeparator()).append(searchInfos);
				}
				return errorMessages.toString();
			}
		}

		boolean isIgnoreResult() {
			return ignoreResult;
		}

		void setIgnoreResult() {
			this.ignoreResult = true;
		}

	}

}
