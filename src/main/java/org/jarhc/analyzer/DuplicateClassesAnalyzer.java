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

import org.jarhc.model.ClassDef;
import org.jarhc.model.Classpath;
import org.jarhc.model.JarFile;
import org.jarhc.report.ReportSection;
import org.jarhc.report.ReportTable;

import java.util.*;
import java.util.stream.Collectors;

public class DuplicateClassesAnalyzer extends Analyzer {

	@Override
	public ReportSection analyze(Classpath classpath) {

		ReportTable table = buildTable(classpath);

		ReportSection section = new ReportSection("Duplicate Classes", "Classes found in multiple JAR files.");
		section.add(table);
		return section;
	}

	private ReportTable buildTable(Classpath classpath) {

		// map from class name to JAR file names
		Map<String, Set<String>> map = new TreeMap<>();

		// for every JAR file ...
		List<JarFile> jarFiles = classpath.getJarFiles();
		for (JarFile jarFile : jarFiles) {

			String fileName = jarFile.getFileName();

			// for every class definition ...
			List<ClassDef> classDefs = jarFile.getClassDefs();
			for (ClassDef classDef : classDefs) {

				String className = classDef.getClassName();
				if (className.equals("module-info")) continue;

				// remember JAR files for class name
				Set<String> fileNames = map.computeIfAbsent(className, k -> new TreeSet<>());
				fileNames.add(fileName);
			}
		}

		ReportTable table = new ReportTable("Class name", "JAR files");

		// for every package ...
		for (String className : map.keySet()) {
			Set<String> fileNames = map.get(className);
			// if class has been found in more than one JAR file ...
			if (fileNames.size() > 1) {
				table.addRow(formatClassName(className), fileNames.stream().collect(Collectors.joining(System.lineSeparator())));
			}
		}

		return table;
	}

	private static String formatClassName(String name) {
		return name.replaceAll("/", ".");
	}

}
