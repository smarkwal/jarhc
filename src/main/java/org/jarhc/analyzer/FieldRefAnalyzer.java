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

import org.jarhc.java.AccessCheck;
import org.jarhc.java.ClassLoader;
import org.jarhc.java.ClasspathClassLoader;
import org.jarhc.model.*;
import org.jarhc.report.ReportSection;
import org.jarhc.report.ReportTable;

import java.util.*;

import static org.jarhc.utils.StringUtils.joinLines;

public class FieldRefAnalyzer extends Analyzer {

	private final ClassLoader parentClassLoader;
	private final boolean reportOwnerClassNotFound;

	public FieldRefAnalyzer(ClassLoader parentClassLoader) {
		this(parentClassLoader, false);
	}

	public FieldRefAnalyzer(ClassLoader parentClassLoader, boolean reportOwnerClassNotFound) {
		if (parentClassLoader == null) throw new IllegalArgumentException("parentClassLoader");
		this.parentClassLoader = parentClassLoader;
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

		ClassLoader classLoader = new ClasspathClassLoader(classpath, "Classpath", parentClassLoader);

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
					SearchResult result = validateFieldRef(classDef, fieldRef, classLoader);
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

	private SearchResult validateFieldRef(ClassDef classDef, FieldRef fieldRef, ClassLoader classLoader) {

		SearchResult searchResult = new SearchResult();

		// try to find owner class
		String targetClassName = fieldRef.getFieldOwner();
		ClassDef ownerClassDef = classLoader.getClassDef(targetClassName).orElse(null);
		if (ownerClassDef == null) {
			// owner class not found
			searchResult.addErrorMessage("Field not found: " + fieldRef.getDisplayName());
			if (reportOwnerClassNotFound) {
				searchResult.addSearchInfo("- " + targetClassName + " (owner class not found)");
			} else {
				// ignore result if owner class is not found
				// (already reported in missing classes)
				searchResult.setIgnoreResult();
			}
			return searchResult;
		}

		AccessCheck accessCheck = new AccessCheck(classLoader);

		// check access to owner class
		boolean access = accessCheck.hasAccess(classDef, ownerClassDef);
		if (!access) {
			String className = classDef.getClassName();
			searchResult.addErrorMessage("Illegal access from " + className + " to class: " + targetClassName);
			return searchResult;
		}

		Set<String> scannedClasses = new HashSet<>();

		// find target field definition
		Optional<FieldDef> fieldDef = findFieldDef(fieldRef, targetClassName, classLoader, searchResult, scannedClasses);
		if (!fieldDef.isPresent()) {
			searchResult.addErrorMessage("Field not found: " + fieldRef.getDisplayName());
			return searchResult;
		}

		FieldDef field = fieldDef.get();

		// check access to field
		access = accessCheck.hasAccess(classDef, field);
		if (!access) {
			String className = classDef.getClassName();
			searchResult.addErrorMessage("Illegal access from " + className + ": " + fieldRef.getDisplayName() + " -> " + field.getDisplayName());
		}

		// check field type
		if (!field.getFieldType().equals(fieldRef.getFieldType())) {
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

	private Optional<FieldDef> findFieldDef(FieldRef fieldRef, String targetClassName, ClassLoader classLoader, SearchResult searchResult, Set<String> scannedClasses) {

		// TODO: use a cache for field definitions like System.out, System.err, ...

		// if class has already been scanned ...
		if (!scannedClasses.add(targetClassName)) {
			// field not found
			// searchResult.addSearchInfo("- " + realClassName + " (already scanned)");
			return Optional.empty();
		}

		// try to find target class in classpath or Java runtime
		ClassDef targetClassDef = classLoader.getClassDef(targetClassName).orElse(null);

		// if class has not been found ...
		if (targetClassDef == null) {
			searchResult.addSearchInfo("- " + targetClassName + " (class not found)");
			// class not found -> field not found
			return Optional.empty();
		}

		// try to find field in target class
		String fieldName = fieldRef.getFieldName();
		Optional<FieldDef> fieldDef = targetClassDef.getFieldDef(fieldName);
		if (fieldDef.isPresent()) {
			searchResult.addSearchInfo("- " + targetClassName + " (field found)");
			return fieldDef;
		}

		// field not found in target class
		searchResult.addSearchInfo("- " + targetClassName + " (field not found)");

		// try to find field in interfaces first
		// (see: https://docs.oracle.com/javase/specs/jvms/se11/html/jvms-5.html#jvms-5.4.3.2)
		List<String> interfaceNames = targetClassDef.getInterfaceNames();
		for (String interfaceName : interfaceNames) {
			fieldDef = findFieldDef(fieldRef, interfaceName, classLoader, searchResult, scannedClasses);
			if (fieldDef.isPresent()) {
				return fieldDef;
			}
		}

		// try to find field in superclass
		String superName = targetClassDef.getSuperName();
		if (superName != null) {
			fieldDef = findFieldDef(fieldRef, superName, classLoader, searchResult, scannedClasses);
			if (fieldDef.isPresent()) {
				return fieldDef;
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
