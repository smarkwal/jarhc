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

import org.jarhc.java.ClassLoader;
import org.jarhc.model.ClassDef;
import org.jarhc.model.Classpath;
import org.jarhc.model.JarFile;
import org.jarhc.report.ReportSection;
import org.jarhc.report.ReportTable;

import java.util.*;

public class ShadowedClassesAnalyzer extends Analyzer {

	public ShadowedClassesAnalyzer() {
	}

	@Override
	public ReportSection analyze(Classpath classpath) {

		ReportTable table = buildTable(classpath);

		ReportSection section = new ReportSection("Shadowed Classes", "Classes shadowing JRE/JDK classes.");
		section.add(table);
		return section;
	}

	private ReportTable buildTable(Classpath classpath) {

		ReportTable table = new ReportTable("Class name", "JAR file", "Class loader", "Similarity");

		ClassLoader parentClassLoader = classpath.getParent();
		if (parentClassLoader == null) {
			return table;
		}

		// for every JAR file ...
		List<JarFile> jarFiles = classpath.getJarFiles();
		for (JarFile jarFile : jarFiles) {

			// map with shadowed classes (class name -> class definition)
			Map<String, String[]> shadowed = Collections.synchronizedMap(new TreeMap<>());

			// for every class definition (in parallel) ...
			List<ClassDef> classDefs = jarFile.getClassDefs();
			classDefs.parallelStream().forEach(classDef -> {

				String className = classDef.getClassName();

				// check if class is shadowing a runtime class
				String realClassName = formatClassName(className);
				Optional<ClassDef> jvmClassDef = parentClassLoader.getClassDef(realClassName);

				//noinspection OptionalIsPresent
				if (jvmClassDef.isPresent()) {
					// shadowed class found
					String classLoader = jvmClassDef.get().getClassLoader();
					String similarity = getSimilarity(classDef, jvmClassDef.get());
					String[] cells = new String[]{classLoader, similarity};
					shadowed.put(realClassName, cells);
				}

			});

			// add a table row for every shadowed class
			for (Map.Entry<String, String[]> duplicate : shadowed.entrySet()) {
				String className = duplicate.getKey();
				String[] cells = duplicate.getValue();
				table.addRow(className, jarFile.getFileName(), cells[0], cells[1]);
			}

		}

		return table;
	}

	/**
	 * Compares two class definitions by class file checksum and API checksum.
	 *
	 * @param classDef    Class definition from classpath
	 * @param jvmClassDef Class definition from Java runtime
	 * @return Similarity
	 */
	private String getSimilarity(ClassDef classDef, ClassDef jvmClassDef) {

		if (Objects.equals(classDef.getClassFileChecksum(), jvmClassDef.getClassFileChecksum())) {
			return "Exact copy";
		}

		if (Objects.equals(classDef.getApiChecksum(), jvmClassDef.getApiChecksum())) {
			return "Same API";
		}

		return "Different API";
	}

	private static String formatClassName(String name) {
		return name.replaceAll("/", ".");
	}

}
