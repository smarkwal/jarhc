package org.jarcheck.analyzer;

import org.jarcheck.model.ClassDef;
import org.jarcheck.model.Classpath;
import org.jarcheck.model.JarFile;
import org.jarcheck.report.ReportSection;
import org.jarcheck.report.ReportTable;
import org.jarcheck.utils.JavaUtils;
import org.jarcheck.utils.MultiMap;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class PackagesAnalyzer extends Analyzer {

	@Override
	public ReportSection analyze(Classpath classpath) {

		ReportTable table = buildTable(classpath);

		ReportSection section = new ReportSection("Packages", "List of packages per JAR file.");
		section.add(table);
		return section;
	}

	private ReportTable buildTable(Classpath classpath) {

		// map from JAR file name to package Names
		MultiMap<String, String> map = new MultiMap<>();

		// for every JAR file ...
		List<JarFile> jarFiles = classpath.getJarFiles();
		for (JarFile jarFile : jarFiles) {

			String fileName = jarFile.getFileName();

			// for every class definition ...
			List<ClassDef> classDefs = jarFile.getClassDefs();
			for (ClassDef classDef : classDefs) {

				// get package name from class name
				String className = classDef.getClassName();
				String packageName = JavaUtils.getPackageName(className);

				// remember JAR files for package name
				map.add(fileName, packageName);
			}
		}

		ReportTable table = new ReportTable("JAR file", "Packages");

		// for every JAR file ...
		for (String fileName : map.getKeys()) {
			Set<String> packageNames = map.getValues(fileName);
			// if package has been found in more than one JAR file ...
			if (packageNames.size() > 0) {
				table.addRow(fileName, packageNames.stream().collect(Collectors.joining(System.lineSeparator())));
			}
		}

		return table;
	}

}
