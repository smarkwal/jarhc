package org.jarcheck.analyzer;

import org.jarcheck.model.ClassDef;
import org.jarcheck.model.Classpath;
import org.jarcheck.model.JarFile;
import org.jarcheck.report.ReportSection;
import org.jarcheck.report.ReportTable;
import org.jarcheck.utils.JavaVersion;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class ClassVersionAnalyzer extends Analyzer {

	@Override
	public ReportSection analyze(Classpath classpath) {

		ReportTable table = buildTable(classpath);

		ReportSection section = new ReportSection("Class Versions", "Java class file format information.");
		section.add(table);
		return section;
	}

	private ReportTable buildTable(Classpath classpath) {

		ReportTable table = new ReportTable("JAR file", "Java version");

		ClassVersionsCounter classpathCounter = new ClassVersionsCounter();

		// for every JAR file ...
		List<JarFile> jarFiles = classpath.getJarFiles();
		for (JarFile jarFile : jarFiles) {

			ClassVersionsCounter jarFileCounter = new ClassVersionsCounter();

			// for every class definition ...
			List<ClassDef> classDefs = jarFile.getClassDefs();
			for (ClassDef classDef : classDefs) {
				int classVersion = classDef.getMajorClassVersion();

				jarFileCounter.count(classVersion);
				classpathCounter.count(classVersion);
			}

			// add row for JAR file
			String javaVersions = jarFileCounter.toString();
			table.addRow(jarFile.getFileName(), javaVersions);
		}

		// add row with summary
		String javaVersions = classpathCounter.toString();
		table.addRow("Classpath", javaVersions);

		return table;
	}

	private static class ClassVersionsCounter {

		// map from major class version to number of classes
		private final Map<Integer, Integer> map = new TreeMap<>();

		void count(int majorClassVersion) {
			map.merge(majorClassVersion, 1, (a, b) -> a + b);
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
