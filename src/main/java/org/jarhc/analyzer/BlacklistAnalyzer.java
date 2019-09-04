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
import java.util.regex.Pattern;
import org.jarhc.model.AnnotationRef;
import org.jarhc.model.ClassDef;
import org.jarhc.model.Classpath;
import org.jarhc.model.Def;
import org.jarhc.model.JarFile;
import org.jarhc.model.ResourceDef;
import org.jarhc.report.ReportSection;
import org.jarhc.report.ReportTable;
import org.jarhc.utils.JavaUtils;
import org.jarhc.utils.ResourceUtils;
import org.jarhc.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BlacklistAnalyzer implements Analyzer {

	private static final Logger LOGGER = LoggerFactory.getLogger(BlacklistAnalyzer.class);

	private final List<Pattern> codePatterns = new ArrayList<>();
	private final List<Pattern> annotationPatterns = new ArrayList<>();
	private final List<Pattern> resourcePatterns = new ArrayList<>();

	public BlacklistAnalyzer() {

		// load rules from file
		String resource = "/blacklist-patterns.txt";
		try {
			init(resource);
		} catch (IOException e) {
			LOGGER.warn("Failed to load blacklist patterns from resource.", e);
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
				Pattern pattern = createPattern(annotation, false);
				annotationPatterns.add(pattern);
			} else if (line.startsWith("resource:")) {
				String path = line.substring(9);
				Pattern pattern = createPattern(path, true);
				resourcePatterns.add(pattern);
			} else {
				Pattern pattern = createPattern(line, false);
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

		// collect all class, field and method descriptors
		List<String> descriptors = new ArrayList<>();
		classDef.getClassRefs().forEach(classRef -> descriptors.add(classRef.getDisplayName()));
		classDef.getFieldRefs().forEach(fieldRef -> descriptors.add(fieldRef.getDisplayName()));
		classDef.getMethodRefs().forEach(methodRef -> descriptors.add(methodRef.getDisplayName()));

		// match every descriptor against all call patterns
		for (String descriptor : descriptors) {
			for (Pattern pattern : codePatterns) {
				if (pattern.matcher(descriptor).matches()) {
					classIssues.add(descriptor);
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
		for (Pattern pattern : annotationPatterns) {
			if (pattern.matcher(annotationClassName).matches()) {
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
			for (Pattern pattern : resourcePatterns) {
				if (pattern.matcher(name).matches()) {
					jarIssues.add(name);
					break;
				}
			}
		}

	}

	private static Pattern createPattern(String pattern, boolean caseInsensitive) {

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

		int flags = caseInsensitive ? Pattern.CASE_INSENSITIVE : 0;
		return Pattern.compile("^" + regex.toString() + "$", flags);
	}

}
