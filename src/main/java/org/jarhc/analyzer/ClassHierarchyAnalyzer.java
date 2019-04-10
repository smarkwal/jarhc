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
import org.jarhc.model.ClassDef;
import org.jarhc.model.Classpath;
import org.jarhc.model.JarFile;
import org.jarhc.report.ReportSection;
import org.jarhc.report.ReportTable;
import org.jarhc.utils.StringUtils;

import java.util.*;

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

		AccessCheck accessCheck = new AccessCheck(classpath);

		// for every JAR file ...
		List<JarFile> jarFiles = classpath.getJarFiles();
		for (JarFile jarFile : jarFiles) {

			Set<String> jarIssues = Collections.synchronizedSet(new TreeSet<>());

			// for every class definition ...
			List<ClassDef> classDefs = jarFile.getClassDefs();
			for (ClassDef classDef : classDefs) {

				Set<String> classIssues = new TreeSet<>();

				// if class has a superclass ...
				String superName = classDef.getSuperName();
				if (superName != null) {
					// check if superclass exists
					Optional<ClassDef> superClassDef = classpath.getClassDef(superName);
					if (!superClassDef.isPresent()) {
						classIssues.add("Superclass not found: " + superName);
					} else {
						ClassDef superClass = superClassDef.get();
						validateSuperclass(superClass, classDef, accessCheck, classIssues);
					}
				}

				// for every interface ...
				List<String> interfaceNames = classDef.getInterfaceNames();
				for (String interfaceName : interfaceNames) {
					// check if interface exists
					Optional<ClassDef> interfaceClassDef = classpath.getClassDef(interfaceName);
					if (!interfaceClassDef.isPresent()) {
						classIssues.add("Interface not found: " + interfaceName);
					} else {
						ClassDef interfaceDef = interfaceClassDef.get();
						validateInterface(interfaceDef, classDef, accessCheck, classIssues);
					}
				}

				// TODO: check if class is abstract OR implements all abstract methods

				if (!classIssues.isEmpty()) {
					String issue = createJarIssue(classDef, classIssues);
					jarIssues.add(issue);
				}

			}

			if (!jarIssues.isEmpty()) {
				String lines = joinLines(jarIssues).trim();
				table.addRow(jarFile.getFileName(), lines);
			}
		}

		return table;
	}

	private void validateSuperclass(ClassDef superClass, ClassDef classDef, AccessCheck accessCheck, Set<String> classIssues) {

		// TODO: skip checks if superclass is from same JAR file
		// if (superClass.isFromSameJarFileAs(classDef)) {
		// 	return;
		// }

		// check if class is final
		if (superClass.isFinal()) {
			classIssues.add("Superclass is final: " + superClass.getDisplayName());
		}

		// check if class is an annotation, interface, or enum
		if (superClass.isAnnotation()) {
			classIssues.add("Superclass is an annotation: " + superClass.getDisplayName());
		} else if (superClass.isInterface()) {
			classIssues.add("Superclass is an interface: " + superClass.getDisplayName());
		} else if (superClass.isEnum()) {
			if (classDef.isEnum() && classDef.getClassName().startsWith(superClass.getClassName() + "$")) {
				// TODO: this check is not needed anymore if classes in same JAR file are skipped
				// superclass is outer enum class,
				// current class is inner anonymous class implementing the abstract enum class
			} else {
				classIssues.add("Superclass is an enum: " + superClass.getDisplayName());
			}
		} else {
			// OK (regular class or abstract class)
		}

		// check access to superclass
		boolean access = accessCheck.hasAccess(classDef, superClass);
		if (!access) {
			classIssues.add("Superclass is not accessible: " + superClass.getDisplayName());
		}

	}

	private void validateInterface(ClassDef interfaceClass, ClassDef classDef, AccessCheck accessCheck, Set<String> classIssues) {

		// TODO: skip checks if interface is from same JAR file
		// if (interfaceClass.isFromSameJarFileAs(classDef)) {
		// 	return;
		// }

		// check if class is an interface
		if (interfaceClass.isAnnotation()) {
			classIssues.add("Interface is an annotation: " + interfaceClass.getDisplayName());
		} else if (interfaceClass.isInterface()) {
			// OK (regular interface)
		} else if (interfaceClass.isEnum()) {
			classIssues.add("Interface is an enum: " + interfaceClass.getDisplayName());
		} else if (interfaceClass.isAbstract()) {
			classIssues.add("Interface is an abstract class: " + interfaceClass.getDisplayName());
		} else {
			classIssues.add("Interface is a class: " + interfaceClass.getDisplayName());
		}

		// check access to interface class
		boolean access = accessCheck.hasAccess(classDef, interfaceClass);
		if (!access) {
			classIssues.add("Interface is not accessible: " + interfaceClass.getDisplayName());
		}

	}

	private String createJarIssue(ClassDef classDef, Set<String> classIssues) {
		String className = classDef.getClassName();
		String lines = classIssues.stream().map(i -> "\u2022 " + i).collect(StringUtils.joinLines());
		return className + System.lineSeparator() + lines + System.lineSeparator();
	}

}
