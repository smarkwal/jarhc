package org.jarcheck.analyzer;

import org.jarcheck.model.ClassDef;
import org.jarcheck.model.Classpath;
import org.jarcheck.model.JarFile;
import org.jarcheck.report.ReportSection;
import org.jarcheck.report.ReportTable;

import java.util.*;
import java.util.stream.Collectors;

public class DuplicateClassesAnalyzer extends Analyzer {

	@Override
	public ReportSection analyze(Classpath classpath) {

		ReportTable table = buildTable(classpath);

		ReportSection section = new ReportSection("Duplicate Classes", "Classes found in multiple JAR files.");
		section.add(table);
		return section;
	}

	private ReportTable buildTable(Classpath classpath) {

		// map from class name to JAR file names
		Map<String, Set<String>> map = new TreeMap<>();

		// for every JAR file ...
		List<JarFile> jarFiles = classpath.getJarFiles();
		for (JarFile jarFile : jarFiles) {

			String fileName = jarFile.getFileName();

			// for every class definition ...
			List<ClassDef> classDefs = jarFile.getClassDefs();
			for (ClassDef classDef : classDefs) {

				String className = classDef.getClassName();

				// remember JAR files for class name
				Set<String> fileNames = map.computeIfAbsent(className, k -> new TreeSet<>());
				fileNames.add(fileName);
			}
		}

		ReportTable table = new ReportTable("Class name", "JAR files");

		// for every package ...
		for (String className : map.keySet()) {
			Set<String> fileNames = map.get(className);
			// if class has been found in more than one JAR file ...
			if (fileNames.size() > 1) {
				table.addRow(formatClassName(className), fileNames.stream().collect(Collectors.joining(System.lineSeparator())));
			}
		}

		return table;
	}

	private static String formatClassName(String name) {
		return name.replaceAll("/", ".");
	}

}
