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

import org.jarhc.model.*;
import org.jarhc.report.ReportSection;
import org.jarhc.report.ReportTable;
import org.jarhc.utils.JavaUtils;
import org.jarhc.utils.StringUtils;

import java.util.*;

public class UnstableAPIsAnalyzer extends Analyzer {

	private final List<String> unstableAnnotations = new ArrayList<>();

	public UnstableAPIsAnalyzer() {
		unstableAnnotations.add("java.lang.Deprecated");
		unstableAnnotations.add("com.google.common.annotations.VisibleForTesting");
		unstableAnnotations.add("com.google.common.annotations.Beta");
		unstableAnnotations.add("com.google.errorprone.annotations.DoNotCall");
	}

	@Override
	public ReportSection analyze(Classpath classpath) {

		ReportTable table = buildTable(classpath);

		ReportSection section = new ReportSection("Unstable APIs", "Use of unstable or deprecated classes, methods, and fields.");
		section.add(table);
		return section;
	}

	private ReportTable buildTable(Classpath classpath) {

		ReportTable table = new ReportTable("JAR file", "Issues");

		List<JarFile> jarFiles = classpath.getJarFiles();
		for (JarFile jarFile : jarFiles) {

			Set<String> jarIssues = Collections.synchronizedSet(new TreeSet<>());

			List<ClassDef> classDefs = jarFile.getClassDefs();
			classDefs.parallelStream().forEach(classDef -> {

				Set<String> classIssues = new TreeSet<>();

				// check class references
				classDef.getClassRefs()
						.forEach(
								classRef -> classpath.getClassDef(classRef)
										.ifPresent(
												def -> findUnstableAnnotations(classDef, def, classIssues)
										)
						);

				// check field references
				classDef.getFieldRefs()
						.forEach(
								fieldRef -> classpath.getFieldDef(fieldRef)
										.ifPresent(
												def -> findUnstableAnnotations(classDef, def, classIssues)
										)
						);

				// check method references
				classDef.getMethodRefs()
						.forEach(
								methodRef -> classpath.getMethodDef(methodRef)
										.ifPresent(
												def -> findUnstableAnnotations(classDef, def, classIssues)
										)
						);

				// TODO: report usage of deprecated/unstable annotation fields
				// TODO: report overriding of deprecated/unstable methods
				// TODO: report implementation of deprecated/unstable methods

				if (!classIssues.isEmpty()) {
					String issue = createJarIssue(classDef, classIssues);
					jarIssues.add(issue);
				}

			});

			if (!jarIssues.isEmpty()) {
				String lines = StringUtils.joinLines(jarIssues).trim();
				table.addRow(jarFile.getFileName(), lines);
			}

		}

		return table;
	}

	private String createJarIssue(ClassDef classDef, Set<String> classIssues) {
		String className = classDef.getClassName();
		String lines = classIssues.stream().map(i -> "\u2022 " + i).collect(StringUtils.joinLines());
		return className + System.lineSeparator() + lines + System.lineSeparator();
	}

	private void findUnstableAnnotations(ClassDef classDef, Def def, Set<String> classIssues) {

		if (def.isFromSameJarFileAs(classDef)) {
			// skip if caller and target are in the same JAR file
			return;
		}

		// for every annotation ...
		List<AnnotationRef> annotationRefs = def.getAnnotationRefs();
		for (AnnotationRef annotationRef : annotationRefs) {
			String annotationClassName = annotationRef.getClassName();

			// check if annotation is a marker for an unstable API
			boolean unstable = isUnstableAnnotation(annotationClassName);
			if (unstable) {
				String issue = createClassIssue(annotationClassName, def);
				classIssues.add(issue);
			}
		}
	}

	private boolean isUnstableAnnotation(String annotationClassName) {
		for (String className : unstableAnnotations) {
			if (className.equals(annotationClassName)) {
				return true;
			}
		}
		return false;
	}

	private String createClassIssue(String annotationClassName, Def def) {
		String annotation = "@" + JavaUtils.getSimpleClassName(annotationClassName);
		String displayName = def.getDisplayName().replace(" @Deprecated ", " ");
		return annotation + ": " + displayName;
	}

}
