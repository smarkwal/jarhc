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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
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

	private final List<ClassPattern> classPatterns = new ArrayList<>();
	private final List<MethodPattern> methodPatterns = new ArrayList<>();
	private final List<FieldPattern> fieldPatterns = new ArrayList<>();
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
			} else if (line.contains("(")) {
				MethodPattern pattern = new MethodPattern(line);
				methodPatterns.add(pattern);
			} else if (line.contains(" ")) {
				FieldPattern pattern = new FieldPattern(line);
				fieldPatterns.add(pattern);
			} else {
				ClassPattern pattern = new ClassPattern(line);
				classPatterns.add(pattern);
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

		if (!classPatterns.isEmpty()) {
			List<ClassRef> classRefs = classDef.getClassRefs();
			//noinspection ForLoopReplaceableByForEach (performance)
			for (int i = 0; i < classRefs.size(); i++) {
				ClassRef classRef = classRefs.get(i);
				//noinspection ForLoopReplaceableByForEach (performance)
				for (int j = 0; j < classPatterns.size(); j++) {
					ClassPattern pattern = classPatterns.get(j);
					if (pattern.matches(classRef)) {
						classIssues.add(classRef.getDisplayName());
						break;
					}
				}
			}
		}

		if (!fieldPatterns.isEmpty()) {
			List<FieldRef> fieldRefs = classDef.getFieldRefs();
			//noinspection ForLoopReplaceableByForEach (performance)
			for (int i = 0; i < fieldRefs.size(); i++) {
				FieldRef fieldRef = fieldRefs.get(i);
				//noinspection ForLoopReplaceableByForEach (performance)
				for (int j = 0; j < fieldPatterns.size(); j++) {
					FieldPattern pattern = fieldPatterns.get(j);
					if (pattern.matches(fieldRef)) {
						classIssues.add(fieldRef.getDisplayName());
						break;
					}
				}
			}
		}

		if (!methodPatterns.isEmpty()) {
			List<MethodRef> methodRefs = classDef.getMethodRefs();
			//noinspection ForLoopReplaceableByForEach (performance)
			for (int i = 0; i < methodRefs.size(); i++) {
				MethodRef methodRef = methodRefs.get(i);
				//noinspection ForLoopReplaceableByForEach (performance)
				for (int j = 0; j < methodPatterns.size(); j++) {
					MethodPattern pattern = methodPatterns.get(j);
					if (pattern.matches(methodRef)) {
						classIssues.add(methodRef.getDisplayName());
						break;
					}
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
		//noinspection ForLoopReplaceableByForEach (performance)
		for (int i = 0; i < annotationRefs.size(); i++) {
			AnnotationRef annotationRef = annotationRefs.get(i);
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
		//noinspection ForLoopReplaceableByForEach (performance)
		for (int i = 0; i < annotationPatterns.size(); i++) {
			StringPattern pattern = annotationPatterns.get(i);
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
		//noinspection ForLoopReplaceableByForEach (performance)
		for (int i = 0; i < resourceDefs.size(); i++) {
			ResourceDef resourceDef = resourceDefs.get(i);
			String name = resourceDef.getPath();

			// check if resource matches any pattern ...
			//noinspection ForLoopReplaceableByForEach (performance)
			for (int j = 0; j < resourcePatterns.size(); j++) {
				StringPattern pattern = resourcePatterns.get(j);
				if (pattern.matches(name)) {
					jarIssues.add(name);
					break;
				}
			}
		}

	}

	private static class ClassPattern {

		private final String className;

		public ClassPattern(String pattern) {
			this.className = pattern;
		}

		public boolean matches(ClassRef classRef) {
			// TODO: support wildcard only for short class name
			return className.equals(classRef.getClassName());
		}

	}

	private static class FieldPattern {

		private final String fieldOwner;
		private final String fieldName;
		private final String fieldType;

		public FieldPattern(String pattern) {
			int pos1 = pattern.indexOf(' ');
			int pos2 = pattern.lastIndexOf('.');

			this.fieldType = checkForWildcard(pattern.substring(0, pos1));
			this.fieldOwner = checkForWildcard(pattern.substring(pos1 + 1, pos2));
			this.fieldName = checkForWildcard(pattern.substring(pos2 + 1));
		}

		public boolean matches(FieldRef fieldRef) {
			return (fieldOwner == null || fieldOwner.equals(fieldRef.getFieldOwner()))
					&& (fieldName == null || fieldName.equals(fieldRef.getFieldName()))
					&& (fieldType == null || fieldType.equals(fieldRef.getFieldType()));
		}
	}

	private static class MethodPattern {

		private final String methodOwner;
		private final String methodName;
		private final Pattern methodDescriptor;

		public MethodPattern(String pattern) {
			int pos1 = pattern.indexOf(' ');
			int pos3 = pattern.indexOf('(', pos1 + 1);
			int pos4 = pattern.indexOf(')', pos3 + 1);
			int pos2 = pattern.lastIndexOf('.', pos3);

			this.methodOwner = checkForWildcard(pattern.substring(pos1 + 1, pos2));
			this.methodName = checkForWildcard(pattern.substring(pos2 + 1, pos3));

			String returnType = pattern.substring(0, pos1);
			String[] parameterTypes = pattern.substring(pos3 + 1, pos4).split(",");
			String descriptor = "(" + Arrays.stream(parameterTypes).map(BlacklistAnalyzer::toInternalType).collect(Collectors.joining()) + ")" + toInternalType(returnType);
			if (descriptor.equals("(*)*")) {
				this.methodDescriptor = null;
			} else {
				// convert to regular expression
				descriptor = "^" + Arrays.stream(descriptor.split("\\*")).map(Pattern::quote).collect(Collectors.joining("(.*)")) + "$";
				this.methodDescriptor = Pattern.compile(descriptor);
			}
		}

		public boolean matches(MethodRef methodRef) {
			return (methodOwner == null || methodOwner.equals(methodRef.getMethodOwner()))
					&& (methodName == null || methodName.equals(methodRef.getMethodName()))
					&& (methodDescriptor == null || methodDescriptor.matcher(methodRef.getMethodDescriptor()).matches());
		}

	}

	private static String toInternalType(String type) {

		if (type.equals("*")) {
			return "*";
		}

		String prefix = "";
		while (type.endsWith("[]")) {
			type = type.substring(0, type.length() - 2);
			prefix += "[";
		}

		switch (type) {
			case "void":
				return prefix + "V";
			case "int":
				return prefix + "I";
			case "long":
				return prefix + "J";
			case "boolean":
				return prefix + "Z";
			case "double":
				return prefix + "D";
			case "byte":
				return prefix + "B";
			case "char":
				return prefix + "C";
			case "float":
				return prefix + "F";
			case "short":
				return prefix + "S";
			default:
				return prefix + "L" + type.replace('.', '/') + ";";
		}

	}

	private static String checkForWildcard(String value) {
		if (value.equals("*")) {
			return null;
		}
		return value;
	}

}
