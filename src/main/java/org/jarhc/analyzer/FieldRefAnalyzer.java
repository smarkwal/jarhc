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

import static org.jarhc.utils.StringUtils.joinLines;

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
		Set<String> scannedClasses = new HashSet<>();

		// find target field definition
		String targetClassName = fieldRef.getFieldOwner();
		Optional<FieldDef> fieldDef = findFieldDef(fieldRef, targetClassName, classpath, searchResult, scannedClasses);
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

	private Optional<FieldDef> findFieldDef(FieldRef fieldRef, String targetClassName, Classpath classpath, SearchResult searchResult, Set<String> scannedClasses) {

		// TODO: use a cache for field definitions like System.out, System.err, ...

		String realClassName = targetClassName.replace('/', '.');

		// if class has already been scanned ...
		if (!scannedClasses.add(targetClassName)) {
			// field not found
			// searchResult.addSearchInfo("- " + realClassName + " (already scanned)");
			return Optional.empty();
		}

		// try to find target class in classpath or Java runtime
		ClassDef targetClassDef = findClassDef(classpath, targetClassName).orElse(null);

		// if class has not been found ...
		if (targetClassDef == null) {

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

			// class not found -> field not found
			return Optional.empty();
		}

		// try to find field in target class
		String fieldName = fieldRef.getFieldName();
		Optional<FieldDef> fieldDef = targetClassDef.getFieldDef(fieldName);
		if (fieldDef.isPresent()) {
			searchResult.addSearchInfo("- " + realClassName + " (field found)");
			return fieldDef;
		}

		// field not found in target class
		searchResult.addSearchInfo("- " + realClassName + " (field not found)");

		// try to find field in interfaces first
		// (see: https://docs.oracle.com/javase/specs/jvms/se11/html/jvms-5.html#jvms-5.4.3.2)
		List<String> interfaceNames = targetClassDef.getInterfaceNames();
		if (interfaceNames != null) {
			for (String interfaceName : interfaceNames) {
				fieldDef = findFieldDef(fieldRef, interfaceName, classpath, searchResult, scannedClasses);
				if (fieldDef.isPresent()) {
					return fieldDef;
				}
			}
		}

		// try to find field in superclass
		String superName = targetClassDef.getSuperName();
		if (superName != null) {
			fieldDef = findFieldDef(fieldRef, superName, classpath, searchResult, scannedClasses);
			if (fieldDef.isPresent()) {
				return fieldDef;
			}
		}

		// field not found
		return Optional.empty();

	}

	private Optional<ClassDef> findClassDef(Classpath classpath, String className) {

		// try to find class on classpath
		Set<ClassDef> targetClassDefs = classpath.getClassDefs(className);
		if (targetClassDefs != null && !targetClassDefs.isEmpty()) {
			// use the first class definition (ignore duplicate classes)
			ClassDef classDef = targetClassDefs.iterator().next();
			return Optional.of(classDef);
		}

		// try to find class in JDK/JRE implementation
		String realClassName = className.replace('/', '.');
		return javaRuntime.getClassDef(realClassName);

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
