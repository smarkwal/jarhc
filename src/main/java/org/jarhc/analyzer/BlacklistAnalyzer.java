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

import org.jarhc.model.*;
import org.jarhc.report.ReportSection;
import org.jarhc.report.ReportTable;
import org.jarhc.utils.JavaUtils;
import org.jarhc.utils.ResourceUtils;
import org.jarhc.utils.StringUtils;

import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

public class BlacklistAnalyzer extends Analyzer {

	private final List<Rule> rules = new ArrayList<>();
	private final Set<String> annotations = new HashSet<>();

	public BlacklistAnalyzer() {

		// load rules from file
		String resource = "/blacklist-patterns.txt";
		try {
			init(resource);
		} catch (IOException e) {
			e.printStackTrace();
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
				annotations.add(annotation);
			} else {
				rules.add(new Rule(line));
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

			List<ClassDef> classDefs = jarFile.getClassDefs();
			classDefs.parallelStream().forEach(classDef -> {

				Set<String> classIssues = new TreeSet<>();

				checkRules(classDef, classIssues);
				checkAnnotations(classDef, classpath, classIssues);

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

	// --------------------------------------------------------------------------------------------------
	// descriptor patterns

	private void checkRules(ClassDef classDef, Set<String> classIssues) {

		// collect all class, field and method descriptors
		List<String> descriptors = new ArrayList<>();
		classDef.getClassRefs().forEach(classRef -> descriptors.add(classRef.getDisplayName()));
		classDef.getFieldRefs().forEach(fieldRef -> descriptors.add(fieldRef.getDisplayName()));
		classDef.getMethodRefs().forEach(methodRef -> descriptors.add(methodRef.getDisplayName()));

		// match every descriptor against all blacklist patterns
		for (String descriptor : descriptors) {
			Rule rule = checkRules(descriptor);
			if (rule != null) {
				classIssues.add(descriptor);
			}
		}

	}

	private Rule checkRules(String descriptor) {
		for (Rule rule : rules) {
			if (rule.matches(descriptor)) {
				return rule;
			}
		}
		return null;
	}

	private static class Rule {

		private final Pattern regex;
		private final String name;

		private Rule(String pattern) {
			this(pattern, pattern);
		}

		private Rule(String name, String pattern) {
			this.regex = createPattern(pattern);
			this.name = name;
		}

		private boolean matches(String descriptor) {
			return regex.matcher(descriptor).matches();
		}

		private String getName() {
			return name;
		}

		private static Pattern createPattern(String pattern) {

			StringBuilder regex = new StringBuilder(pattern.length() + 10);

			int end = 0;
			while (true) {
				int pos = pattern.indexOf('*', end);
				if (pos < 0) {
					regex.append(Pattern.quote(pattern.substring(end)));
					break;
				} else {
					if (pos > end) {
						regex.append(Pattern.quote(pattern.substring(end, pos)));
					}
					regex.append(".*");
					end = pos + 1;
				}
			}

			return Pattern.compile("^" + regex.toString() + "$");
		}

	}

	// --------------------------------------------------------------------------------------------------
	// annotations

	private void checkAnnotations(ClassDef classDef, Classpath classpath, Set<String> classIssues) {

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
		for (String className : annotations) {
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
