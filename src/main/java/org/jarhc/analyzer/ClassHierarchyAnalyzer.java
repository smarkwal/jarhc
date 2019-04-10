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

import org.jarhc.model.ClassDef;
import org.jarhc.model.Classpath;
import org.jarhc.model.JarFile;
import org.jarhc.report.ReportSection;
import org.jarhc.report.ReportTable;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.jarhc.utils.StringUtils.joinLines;

public class ClassHierarchyAnalyzer extends Analyzer {

	@Override
	public ReportSection analyze(Classpath classpath) {

		ReportTable table = buildTable(classpath);

		ReportSection section = new ReportSection("Class Hierarchy", "Problems with class hierarchy.");
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

				// TODO: group errors by class

				// if class has a superclass ...
				String superName = classDef.getSuperName();
				if (superName != null) {
					// check if superclass exists
					Optional<ClassDef> superClassDef = classpath.getClassDef(superName);
					if (!superClassDef.isPresent()) {
						// errorMessages.add("Superclass not found: " + superName);
					} else {
						ClassDef superClass = superClassDef.get();
						validateSuperclass(superClass, classDef, errorMessages);
					}
				}

				// for every interface ...
				List<String> interfaceNames = classDef.getInterfaceNames();
				for (String interfaceName : interfaceNames) {
					// check if interface exists
					Optional<ClassDef> interfaceClassDef = classpath.getClassDef(interfaceName);
					if (!interfaceClassDef.isPresent()) {
						// errorMessages.add("Interface not found: " + interfaceName);
					} else {
						ClassDef interfaceDef = interfaceClassDef.get();
						validateInterface(interfaceDef, classDef, errorMessages);
					}
				}

				// TODO: check if class is abstract OR implements all abstract methods

			}

			if (!errorMessages.isEmpty()) {
				table.addRow(jarFile.getFileName(), joinLines(errorMessages).trim());
			}
		}

		return table;
	}

	private void validateSuperclass(ClassDef superClass, ClassDef classDef, Set<String> errorMessages) {

		// TODO: skip checks if superclass is from same JAR file
		// if (superClass.isFromSameJarFileAs(classDef)) {
		// 	return;
		// }

		// check if class is final
		if (superClass.isFinal()) {
			errorMessages.add("Superclass is final: " + superClass.getDisplayName());
		}

		// check if class is an annotation, interface, or enum
		if (superClass.isAnnotation()) {
			errorMessages.add("Superclass is an annotation: " + superClass.getDisplayName());
		} else if (superClass.isInterface()) {
			errorMessages.add("Superclass is an interface: " + superClass.getDisplayName());
		} else if (superClass.isEnum()) {
			if (classDef.isEnum() && classDef.getClassName().startsWith(superClass.getClassName() + "$")) {
				// TODO: this check is not needed anymore if classes in same JAR file are skipped
				// superclass is outer enum class,
				// current class is inner anonymous class implementing the abstract enum class
			} else {
				errorMessages.add("Superclass is an enum: " + superClass.getDisplayName());
			}
		} else {
			// OK (regular class or abstract class)
		}

		// TODO: check if superclass is accessible
	}

	private void validateInterface(ClassDef interfaceClass, ClassDef classDef, Set<String> errorMessages) {

		// TODO: skip checks if interface is from same JAR file
		// if (interfaceClass.isFromSameJarFileAs(classDef)) {
		// 	return;
		// }

		// check if class is an interface
		if (interfaceClass.isAnnotation()) {
			errorMessages.add("Interface is an annotation: " + interfaceClass.getDisplayName());
		} else if (interfaceClass.isInterface()) {
			// OK (regular interface)
		} else if (interfaceClass.isEnum()) {
			errorMessages.add("Interface is an enum: " + interfaceClass.getDisplayName());
		} else if (interfaceClass.isAbstract()) {
			errorMessages.add("Interface is an abstract class: " + interfaceClass.getDisplayName());
		} else {
			errorMessages.add("Interface is a class: " + interfaceClass.getDisplayName());
		}

		// TODO: check if interface is accessible
	}

}
