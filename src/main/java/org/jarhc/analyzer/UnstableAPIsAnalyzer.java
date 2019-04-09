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
				classDef.getClassRefs().forEach(classRef -> {
					classpath.getClassDef(classRef).ifPresent(targetClassDef -> {
						findUnstableAnnotations(classDef, targetClassDef, classIssues);
					});
				});

				// check field references
				classDef.getFieldRefs().forEach(fieldRef -> {
					classpath.getFieldDef(fieldRef).ifPresent(targetFieldDef -> {
						findUnstableAnnotations(classDef, targetFieldDef, classIssues);
					});
				});

				// check method references
				classDef.getMethodRefs().forEach(methodRef -> {
					classpath.getMethodDef(methodRef).ifPresent(targetMethodDef -> {
						findUnstableAnnotations(classDef, targetMethodDef, classIssues);
					});
				});

				if (!classIssues.isEmpty()) {
					String className = classDef.getClassName();
					jarIssues.add(className + System.lineSeparator() + classIssues.stream().map(i -> "\u2022 " + i).collect(StringUtils.joinLines()) + System.lineSeparator());
				}

			});

			if (!jarIssues.isEmpty()) {
				table.addRow(jarFile.getFileName(), StringUtils.joinLines(jarIssues).trim());
			}

		}

		return table;
	}

	private void findUnstableAnnotations(ClassDef classDef, AnnotationHolder annotationHolder, Set<String> classIssues) {

		// skip if caller and target are in the same JAR file
		ClassDef targetClassDef = annotationHolder.getClassDef();
		JarFile jarFile = classDef.getJarFile();
		JarFile targetJarFile = targetClassDef.getJarFile();
		if (jarFile == targetJarFile) {
			return;
		}

		List<AnnotationRef> annotationRefs = annotationHolder.getAnnotationRefs();
		for (AnnotationRef annotationRef : annotationRefs) {
			String className = annotationRef.getClassName();
			boolean unstable = isUnstableAnnotation(className);
			if (unstable) {
				String annotation = "@" + JavaUtils.getSimpleClassName(className);
				String displayName = annotationHolder.getDisplayName().replace(" @Deprecated ", " ");
				// TODO: add owner class if annotation holder is a method or field
				classIssues.add(annotation + ": " + displayName);
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

}
