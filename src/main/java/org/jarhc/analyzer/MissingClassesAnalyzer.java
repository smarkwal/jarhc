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
import org.jarhc.model.ClassRef;
import org.jarhc.model.Classpath;
import org.jarhc.model.JarFile;
import org.jarhc.report.ReportSection;
import org.jarhc.report.ReportTable;
import org.jarhc.utils.JavaUtils;
import org.jarhc.utils.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static org.jarhc.utils.StringUtils.joinLines;

public class MissingClassesAnalyzer extends Analyzer {

	@Override
	public ReportSection analyze(Classpath classpath) {

		ReportTable table = buildTable(classpath);

		ReportSection section = new ReportSection("Missing Classes", "References to classes not found on the classpath.");
		section.add(table);
		return section;
	}

	private ReportTable buildTable(Classpath classpath) {
		ReportTable table = new ReportTable("JAR file", "Missing classes");

		// for every JAR file ...
		List<JarFile> jarFiles = classpath.getJarFiles();
		for (JarFile jarFile : jarFiles) {

			// find all missing classes
			Set<String> jarIssues = findMissingClasses(jarFile, classpath);

			if (!jarIssues.isEmpty()) {
				String lines = joinLines(jarIssues).trim();
				table.addRow(jarFile.getFileName(), lines);
			}
		}

		return table;
	}

	private Set<String> findMissingClasses(JarFile jarFile, ClassLoader classLoader) {
		Set<String> jarIssues = Collections.synchronizedSet(new TreeSet<>());

		// for every class definition (in parallel) ...
		List<ClassDef> classDefs = jarFile.getClassDefs();
		classDefs.parallelStream().forEach(classDef -> {

			Set<String> classIssues = new TreeSet<>();

			// for every class reference ...
			List<ClassRef> classRefs = classDef.getClassRefs();
			for (ClassRef classRef : classRefs) {
				String className = classRef.getClassName();

				// check if class exists
				boolean exists = classLoader.getClassDef(className).isPresent();
				if (!exists) {

					String packageName = JavaUtils.getPackageName(className);
					boolean found = classLoader.containsPackage(packageName);
					if (!found) {
						classIssues.add(className + " (package not found)");
					} else {
						classIssues.add(className + " (class not found)");
					}

				}
			}

			if (!classIssues.isEmpty()) {
				String issue = createJarIssue(classDef, classIssues);
				jarIssues.add(issue);
			}

		});

		return jarIssues;
	}

	private String createJarIssue(ClassDef classDef, Set<String> classIssues) {
		String className = classDef.getClassName();
		String lines = classIssues.stream().map(i -> "\u2022 " + i).collect(StringUtils.joinLines());
		return className + System.lineSeparator() + lines + System.lineSeparator();
	}

}
