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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.jarhc.model.AnnotationRef;
import org.jarhc.model.ClassDef;
import org.jarhc.model.ClassRef;
import org.jarhc.model.Classpath;
import org.jarhc.model.Def;
import org.jarhc.model.FieldDef;
import org.jarhc.model.FieldRef;
import org.jarhc.model.JarFile;
import org.jarhc.model.MethodDef;
import org.jarhc.model.MethodRef;
import org.jarhc.model.ResourceDef;
import org.jarhc.report.ReportSection;
import org.jarhc.report.ReportTable;
import org.jarhc.utils.JavaUtils;
import org.jarhc.utils.ResourceUtils;
import org.jarhc.utils.StringPattern;
import org.jarhc.utils.StringUtils;
import org.slf4j.Logger;

public class BlacklistAnalyzer implements Analyzer {

	private final List<StringPattern> codePatterns = new ArrayList<>();
	private final List<StringPattern> annotationPatterns = new ArrayList<>();
	private final List<StringPattern> resourcePatterns = new ArrayList<>();

	public BlacklistAnalyzer(Logger logger) {

		// load rules from file
		String resource = "/blacklist-patterns.txt";
		try {
			init(resource);
		} catch (IOException e) {
			logger.warn("Failed to load blacklist patterns from resource.", e);
		}

	}

	private void init(String resource) throws IOException {

		// read lines from configuration resource
		List<String> lines = ResourceUtils.getResourceAsLines(resource, "UTF-8");

		for (String line : lines) {
			if (line.startsWith("#")) continue; // ignore comments
			if (line.trim().isEmpty()) continue; // ignore empty lines

			if (line.startsWith("@")) {
				String annotation = line.substring(1);
				StringPattern pattern = new StringPattern(annotation, false);
				annotationPatterns.add(pattern);
			} else if (line.startsWith("resource:")) {
				String path = line.substring(9);
				StringPattern pattern = new StringPattern(path, true);
				resourcePatterns.add(pattern);
			} else {
				StringPattern pattern = new StringPattern(line, false);
				codePatterns.add(pattern);
			}

		}
	}

	@Override
	public ReportSection analyze(Classpath classpath) {

		ReportTable table = buildTable(classpath);

		ReportSection section = new ReportSection("Blacklist", "Use of dangerous, unsafe, unstable, or deprecated classes and methods.");
		section.add(table);
		return section;
	}

	private ReportTable buildTable(Classpath classpath) {

		ReportTable table = new ReportTable("JAR file", "Issues");

		List<JarFile> jarFiles = classpath.getJarFiles();
		for (JarFile jarFile : jarFiles) {

			Set<String> jarIssues = Collections.synchronizedSet(new TreeSet<>());

			validateClassDefs(jarFile, classpath, jarIssues);
			validateResources(jarFile, jarIssues);

			if (!jarIssues.isEmpty()) {
				String lines = StringUtils.joinLines(jarIssues).trim();
				table.addRow(jarFile.getFileName(), lines);
			}

		}

		return table;
	}

	private void validateClassDefs(JarFile jarFile, Classpath classpath, Set<String> jarIssues) {

		List<ClassDef> classDefs = jarFile.getClassDefs();
		classDefs.parallelStream().forEach(classDef -> {

			Set<String> classIssues = new TreeSet<>();

			validateClassDef(classDef, classIssues);
			validateAnnotations(classDef, classpath, classIssues);

			if (!classIssues.isEmpty()) {
				String issue = createJarIssue(classDef, classIssues);
				jarIssues.add(issue);
			}

		});

	}

	private void validateClassDef(ClassDef classDef, Set<String> classIssues) {

		List<ClassRef> classRefs = classDef.getClassRefs();
		List<FieldRef> fieldRefs = classDef.getFieldRefs();
		List<MethodRef> methodRefs = classDef.getMethodRefs();

		// collect all class, field and method descriptors
		List<String> descriptors = new ArrayList<>(classRefs.size() + fieldRefs.size() + methodRefs.size());
		classRefs.forEach(classRef -> descriptors.add(classRef.getDisplayName()));
		fieldRefs.forEach(fieldRef -> descriptors.add(fieldRef.getDisplayName()));
		methodRefs.forEach(methodRef -> descriptors.add(methodRef.getDisplayName()));

		// match every descriptor against all call patterns
		for (String descriptor : descriptors) {
			for (StringPattern pattern : codePatterns) {
				if (pattern.matches(descriptor)) {
					classIssues.add(descriptor);
					break;
				}
			}
		}
	}

	private String createJarIssue(ClassDef classDef, Set<String> classIssues) {
		String className = classDef.getClassName();
		String lines = classIssues.stream().map(i -> "\u2022 " + i).collect(StringUtils.joinLines());
		return className + System.lineSeparator() + lines + System.lineSeparator();
	}

	// --------------------------------------------------------------------------------------------------
	// annotations

	private void validateAnnotations(ClassDef classDef, Classpath classpath, Set<String> classIssues) {

		// check class annotations
		classDef.getAnnotationRefs()
				.forEach(
						annotationRef -> {
							ClassDef def = classpath.getClassDef(annotationRef.getClassName());
							if (def != null) {
								findUnstableAnnotations(classDef, def, classIssues);
							}
						}
				);

		// check record component annotations
		classDef.getRecordComponentDefs().forEach(
				recordComponentDef -> recordComponentDef.getAnnotationRefs()
						.forEach(
								annotationRef -> {
									ClassDef def = classpath.getClassDef(annotationRef.getClassName());
									if (def != null) {
										findUnstableAnnotations(classDef, def, classIssues);
									}
								}
						)
		);

		// check method annotations
		classDef.getMethodDefs().forEach(
				methodDef -> methodDef.getAnnotationRefs()
						.forEach(
								annotationRef -> {
									ClassDef def = classpath.getClassDef(annotationRef.getClassName());
									if (def != null) {
										findUnstableAnnotations(classDef, def, classIssues);
									}
								}
						)
		);

		// check field annotations
		classDef.getFieldDefs().forEach(
				fieldDef -> fieldDef.getAnnotationRefs()
						.forEach(
								annotationRef -> {
									ClassDef def = classpath.getClassDef(annotationRef.getClassName());
									if (def != null) {
										findUnstableAnnotations(classDef, def, classIssues);
									}
								}
						)
		);

		// check class references
		classDef.getClassRefs()
				.forEach(
						classRef -> {
							ClassDef def = classpath.getClassDef(classRef);
							if (def != null) {
								findUnstableAnnotations(classDef, def, classIssues);
							}
						}
				);

		// check field references
		classDef.getFieldRefs()
				.forEach(
						fieldRef -> {
							FieldDef def = classpath.getFieldDef(fieldRef);
							if (def != null) {
								findUnstableAnnotations(classDef, def, classIssues);
							}
						}
				);

		// check method references
		classDef.getMethodRefs()
				.forEach(
						methodRef -> {
							MethodDef def = classpath.getMethodDef(methodRef);
							if (def != null) {
								findUnstableAnnotations(classDef, def, classIssues);
							}
						}
				);

		// TODO: report usage of deprecated/unstable annotation fields
		// TODO: report overriding of deprecated/unstable methods
		// TODO: report implementation of deprecated/unstable methods
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
		for (StringPattern pattern : annotationPatterns) {
			if (pattern.matches(annotationClassName)) {
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

	// --------------------------------------------------------------------------------------------------
	// resources

	private void validateResources(JarFile jarFile, Set<String> jarIssues) {

		// for every resource ...
		List<ResourceDef> resourceDefs = jarFile.getResourceDefs();
		for (ResourceDef resourceDef : resourceDefs) {
			String name = resourceDef.getPath();

			// check if resource matches any pattern ...
			for (StringPattern pattern : resourcePatterns) {
				if (pattern.matches(name)) {
					jarIssues.add(name);
					break;
				}
			}
		}

	}

}
