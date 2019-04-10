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

public class MethodRefAnalyzer extends Analyzer {

	private final boolean reportOwnerClassNotFound;

	public MethodRefAnalyzer() {
		this(false);
	}

	public MethodRefAnalyzer(boolean reportOwnerClassNotFound) {
		this.reportOwnerClassNotFound = reportOwnerClassNotFound;
	}

	@Override
	public ReportSection analyze(Classpath classpath) {

		ReportTable table = buildTable(classpath);

		ReportSection section = new ReportSection("Method References", "Problems with method invocations.");
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

				// for every method reference ...
				List<MethodRef> methodRefs = classDef.getMethodRefs();
				for (MethodRef methodRef : methodRefs) {

					// validate field reference
					SearchResult result = validateMethodRef(classDef, methodRef, classpath);
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

	private SearchResult validateMethodRef(ClassDef classDef, MethodRef methodRef, ClassLoader classLoader) {

		SearchResult searchResult = new SearchResult();

		// try to find owner class
		String targetClassName = methodRef.getMethodOwner();
		ClassDef ownerClassDef = classLoader.getClassDef(targetClassName).orElse(null);
		if (ownerClassDef == null) {
			// owner class not found
			searchResult.addErrorMessage("Method not found: " + methodRef.getDisplayName());
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
		Optional<MethodDef> methodDef = classLoader.getMethodDef(methodRef, searchResult);
		if (!methodDef.isPresent()) {
			searchResult.addErrorMessage("Method not found: " + methodRef.getDisplayName());
			return searchResult;
		}

		MethodDef method = methodDef.get();

		// check access to method
		access = accessCheck.hasAccess(classDef, method);
		if (!access) {
			String className = classDef.getClassName();
			searchResult.addErrorMessage("Illegal access from " + className + ": " + methodRef.getDisplayName() + " -> " + method.getDisplayName());
		}

		// check method return type
		// TODO
		/*
		if (!method.getFieldType().equals(methodRef.getFieldType())) {
			searchResult.addErrorMessage("Incompatible field type: " + methodRef.getDisplayName() + " -> " + method.getDisplayName());
		}
		*/

		// check static/instance
		if (method.isStatic()) {
			if (!methodRef.isStaticAccess()) {
				searchResult.addErrorMessage("Instance access to static method: " + methodRef.getDisplayName() + " -> " + method.getDisplayName());
			}
		} else {
			if (methodRef.isStaticAccess()) {
				searchResult.addErrorMessage("Static access to instance method: " + methodRef.getDisplayName() + " -> " + method.getDisplayName());
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
		public void classNotFound(String className) {
			addSearchInfo("\u2022 " + className + " (class not found)");
		}

		@Override
		public void memberNotFound(String className) {
			addSearchInfo("\u2022 " + className + " (method not found)");
		}

		@Override
		public void memberFound(String className) {
			addSearchInfo("\u2022 " + className + " (method found)");
		}

	}

}
