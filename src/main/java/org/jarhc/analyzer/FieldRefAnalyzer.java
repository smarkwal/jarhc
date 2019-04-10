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
import org.jarhc.model.*;
import org.jarhc.report.ReportSection;
import org.jarhc.report.ReportTable;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.jarhc.utils.StringUtils.joinLines;

public class FieldRefAnalyzer extends Analyzer {

	private final boolean reportOwnerClassNotFound;

	public FieldRefAnalyzer() {
		this(false);
	}

	public FieldRefAnalyzer(boolean reportOwnerClassNotFound) {
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
							errorMessages.add(text + System.lineSeparator());
						}
					}
				}
			}

			if (!errorMessages.isEmpty()) {
				table.addRow(jarFile.getFileName(), joinLines(errorMessages).trim());
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
				searchResult.addSearchInfo("\u2022 " + targetClassName + " (owner class not found)");
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

		// find target field definition
		Optional<FieldDef> fieldDef = classLoader.getFieldDef(fieldRef, searchResult);
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

	private class SearchResult implements ClassLoader.Callback {

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

		@Override
		public void classDefNotFound(String className) {
			addSearchInfo("\u2022 " + className + " (class not found)");
		}

		@Override
		public void fieldDefFound(String className) {
			addSearchInfo("\u2022 " + className + " (field found)");
		}

		@Override
		public void fieldDefNotFound(String className) {
			addSearchInfo("\u2022 " + className + " (field not found)");
		}

	}

}
