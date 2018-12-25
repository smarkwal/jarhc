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
import org.jarhc.utils.StringUtils;

import java.util.*;
import java.util.regex.Pattern;

public class BlacklistAnalyzer extends Analyzer {

	private final List<Rule> rules = new ArrayList<>();

	public BlacklistAnalyzer() {

		// TODO: load rules from file
		// ClassLoader classLoader = this.getClass().getClassLoader();
		// InputStream stream = classLoader.getResourceAsStream("/blacklist-patterns.txt");

		// unsafe
		rules.add(new Rule("* sun.misc.Unsafe.*(*)"));

		// JVM shutdown
		rules.add(new Rule("static void java.lang.System.exit(int)"));
		rules.add(new Rule("void java.lang.Runtime.exit(int)"));
		rules.add(new Rule("void java.lang.Runtime.halt(int)"));

		// loading of native libraries
		rules.add(new Rule("static void java.lang.System.load(java.lang.String)"));
		rules.add(new Rule("static void java.lang.System.loadLibrary(java.lang.String)"));
		rules.add(new Rule("void java.lang.Runtime.load(java.lang.String)"));
		rules.add(new Rule("void java.lang.Runtime.loadLibrary(java.lang.String)"));

		// execution of system commands
		rules.add(new Rule("java.lang.Process java.lang.Runtime.exec(*)"));

		// deprecated com.sun.image.codec.jpeg API (removed in Java 9)
		rules.add(new Rule("* com.sun.image.codec.jpeg.*"));

	}

	@Override
	public ReportSection analyze(Classpath classpath) {

		ReportTable table = buildTable(classpath);

		ReportSection section = new ReportSection("Blacklist", "Use of unsafe or dangerous classes and methods.");
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

				List<String> descriptors = new ArrayList<>();
				classDef.getClassRefs().forEach(classRef -> descriptors.add(classRef.getClassName()));
				classDef.getFieldRefs().forEach(fieldRef -> descriptors.add(fieldRef.getDisplayName()));
				classDef.getMethodRefs().forEach(methodRef -> descriptors.add(methodRef.getDisplayName()));

				for (String descriptor : descriptors) {
					Rule rule = checkRules(descriptor);
					if (rule != null) {
						classIssues.add(descriptor);
					}
				}

				if (!classIssues.isEmpty()) {
					String className = classDef.getClassName().replace('/', '.');
					jarIssues.add(className + System.lineSeparator() + classIssues.stream().map(i -> "- " + i).collect(StringUtils.joinLines()));
				}

			});

			if (!jarIssues.isEmpty()) {
				table.addRow(jarFile.getFileName(), StringUtils.joinLines(jarIssues));
			}

		}

		return table;
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

}
