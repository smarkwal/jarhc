package org.jarhc.analyzer;

import org.jarhc.model.ClassDef;
import org.jarhc.model.Classpath;
import org.jarhc.model.JarFile;
import org.jarhc.report.ReportSection;
import org.jarhc.report.ReportTable;
import org.jarhc.utils.JavaUtils;
import org.jarhc.utils.MultiMap;

import java.util.List;
import java.util.Set;
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
				map.add(packageName, fileName);
			}
		}

		ReportTable table = new ReportTable("Package", "JAR files");

		// for every package ...
		for (String packageName : map.getKeys()) {
			Set<String> fileNames = map.getValues(packageName);
			// if package has been found in more than one JAR file ...
			if (fileNames.size() > 1) {
				table.addRow(packageName, fileNames.stream().collect(Collectors.joining(System.lineSeparator())));
			}
		}

		return table;
	}

}
