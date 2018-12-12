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
import org.jarhc.model.ClassDef;
import org.jarhc.model.Classpath;
import org.jarhc.model.JarFile;
import org.jarhc.report.ReportSection;
import org.jarhc.report.ReportTable;

import java.util.List;

public class ShadowedClassesAnalyzer extends Analyzer {

	private final JavaRuntime javaRuntime;

	public ShadowedClassesAnalyzer(JavaRuntime javaRuntime) {
		if (javaRuntime == null) throw new IllegalArgumentException("javaRuntime");
		this.javaRuntime = javaRuntime;
	}

	@Override
	public ReportSection analyze(Classpath classpath) {

		ReportTable table = buildTable(classpath);

		StringBuilder description = new StringBuilder("Classes shadowing JRE/JDK classes.").append(System.lineSeparator());

		// print information about JRE/JDK in description
		description.append("Java home   : ").append(javaRuntime.getJavaHome()).append(System.lineSeparator());
		description.append("Java runtime: ").append(javaRuntime.getName()).append(System.lineSeparator());
		description.append("Java version: ").append(javaRuntime.getJavaVersion()).append(System.lineSeparator());
		description.append("Java vendor : ").append(javaRuntime.getJavaVendor());

		ReportSection section = new ReportSection("Shadowed Classes", description.toString());
		section.add(table);
		return section;
	}

	private ReportTable buildTable(Classpath classpath) {

		ReportTable table = new ReportTable("Class name", "JAR file", "ClassLoader");

		// for every JAR file ...
		List<JarFile> jarFiles = classpath.getJarFiles();
		for (JarFile jarFile : jarFiles) {

			// for every class definition ...
			List<ClassDef> classDefs = jarFile.getClassDefs();
			for (ClassDef classDef : classDefs) {

				String className = classDef.getClassName();

				String realClassName = formatClassName(className);
				String classLoader = javaRuntime.getClassLoaderName(realClassName);
				if (classLoader == null) {
					continue;
				}

				table.addRow(realClassName, jarFile.getFileName(), classLoader);

			}
		}

		return table;
	}

	private static String formatClassName(String name) {
		return name.replaceAll("/", ".");
	}

}