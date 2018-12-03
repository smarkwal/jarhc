package org.jarcheck.analyzer;

import org.jarcheck.model.ClassDef;
import org.jarcheck.model.Classpath;
import org.jarcheck.model.JarFile;
import org.jarcheck.report.ReportSection;
import org.jarcheck.report.ReportTable;

import java.util.*;
import java.util.stream.Collectors;

public class SplitPackagesAnalyzer extends Analyzer {

	@Override
	public ReportSection analyze(Classpath classpath) {

		ReportTable table = buildTable(classpath);

		ReportSection section = new ReportSection("Split Packages", "Packages found in multiple JAR files.");
		section.add(table);
		return section;
	}

	private ReportTable buildTable(Classpath classpath) {

		// map from package name to JAR file names
		Map<String, Set<String>> map = new TreeMap<>();

		// for every JAR file ...
		List<JarFile> jarFiles = classpath.getJarFiles();
		for (JarFile jarFile : jarFiles) {

			String fileName = jarFile.getFileName();

			// for every class definition ...
			List<ClassDef> classDefs = jarFile.getClassDefs();
			for (ClassDef classDef : classDefs) {

				// get package name from class name
				String className = classDef.getClassName();
				int pos = className.lastIndexOf("/");
				String packageName = pos < 0 ? "" : className.substring(0, pos);

				// remember JAR files for package name
				Set<String> fileNames = map.computeIfAbsent(packageName, k -> new TreeSet<>());
				fileNames.add(fileName);
			}
		}

		ReportTable table = new ReportTable("Package", "JAR files");

		// for every package ...
		for (String packageName : map.keySet()) {
			Set<String> fileNames = map.get(packageName);
			// if package has been found in more than one JAR file ...
			if (fileNames.size() > 1) {
				table.addRow(formatPackageName(packageName), fileNames.stream().collect(Collectors.joining(System.lineSeparator())));
			}
		}

		return table;
	}

	private static String formatPackageName(String name) {
		return name.replaceAll("/", ".");
	}

}
