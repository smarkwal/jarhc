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

import static org.jarhc.utils.StringUtils.wrapList;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import org.jarhc.model.ClassDef;
import org.jarhc.model.Classpath;
import org.jarhc.model.JarFile;
import org.jarhc.report.ReportSection;
import org.jarhc.report.ReportTable;
import org.jarhc.utils.JavaVersion;

public class ClassVersionsAnalyzer implements Analyzer {

	@Override
	public ReportSection analyze(Classpath classpath) {

		ReportTable table = buildTable(classpath);

		ReportSection section = new ReportSection("Class Versions", "Java class file format information.");
		section.add(table);
		return section;
	}

	private ReportTable buildTable(Classpath classpath) {

		ReportTable table = new ReportTable("Artifact", "Multi-release", "Class files by Java version");

		ClassVersionsCounter classpathCounter = new ClassVersionsCounter();

		// for every JAR file ...
		List<JarFile> jarFiles = classpath.getJarFiles();
		for (JarFile jarFile : jarFiles) {

			ClassVersionsCounter jarFileCounter = new ClassVersionsCounter();
			String multiReleaseInfo = getMultiReleaseInfo(jarFile);

			// for every class definition ...
			List<ClassDef> classDefs = jarFile.getClassDefs();
			for (ClassDef classDef : classDefs) {

				// TODO: make this configurable ?
				if (!classDef.isRegularClass()) {
					// ignore package-info and module-info classes
					continue;
				}

				int classVersion = classDef.getMajorClassVersion();

				jarFileCounter.count(classVersion);
				classpathCounter.count(classVersion);
			}

			// add row for JAR file
			String javaVersions = jarFileCounter.toString();
			table.addRow(jarFile.getArtifactName(), multiReleaseInfo, wrapList(javaVersions, 60));
		}

		// add row with summary
		String javaVersions = classpathCounter.toString();
		javaVersions = wrapList(javaVersions, 60);
		table.addRow("Classpath", "-", javaVersions);

		return table;
	}

	private String getMultiReleaseInfo(JarFile jarFile) {
		if (jarFile.isMultiRelease()) {
			String releases = jarFile.getReleases().stream().map(r -> "Java " + r).collect(Collectors.joining(", "));
			return "Yes (" + releases + ")";
		} else {
			return "No";
		}
	}

	private static class ClassVersionsCounter {

		// map from major class version to number of classes
		private final Map<Integer, Integer> map = new TreeMap<>();

		void count(int majorClassVersion) {
			map.merge(majorClassVersion, 1, Integer::sum);
		}

		@Override
		public String toString() {
			int size = map.size();
			if (size == 0) {
				return "[no class files]";
			}
			return map.entrySet().stream()
					.sorted(this::sortByClassVersion)
					.map(this::formatJavaVersion)
					.collect(Collectors.joining(", "));
		}

		private int sortByClassVersion(Map.Entry<Integer, Integer> entry1, Map.Entry<Integer, Integer> entry2) {
			return entry2.getKey() - entry1.getKey();
		}

		private String formatJavaVersion(Map.Entry<Integer, Integer> entry) {
			return JavaVersion.fromClassVersion(entry.getKey()) + " (" + entry.getValue() + ")";
		}

	}

}
