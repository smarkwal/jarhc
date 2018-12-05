package org.jarcheck.analyzer;

import org.jarcheck.model.ClassDef;
import org.jarcheck.model.ClassRef;
import org.jarcheck.model.Classpath;
import org.jarcheck.model.JarFile;
import org.jarcheck.report.ReportSection;
import org.jarcheck.report.ReportTable;

import java.util.*;

public class JarDependenciesAnalyzer extends Analyzer {

	@Override
	public ReportSection analyze(Classpath classpath) {

		Map<String, List<String>> index = createClassNameToJarFileIndex(classpath);
		Map<String, Set<String>> dependencies = calculateJarFileDependencies(classpath, index);

		ReportTable table = buildTable(classpath, dependencies);

		ReportSection section = new ReportSection("JAR File Dependencies", "Dependencies between JAR files.");
		section.add(table);
		return section;
	}

	private ReportTable buildTable(Classpath classpath, Map<String, Set<String>> dependencies) {

		ReportTable table = new ReportTable("JAR file", "Depends on");

		List<JarFile> jarFiles = classpath.getJarFiles();
		for (JarFile jarFile : jarFiles) {
			String sourceFileName = jarFile.getFileName();
			Set<String> targetFileNames = dependencies.get(sourceFileName);
			if (targetFileNames == null || targetFileNames.isEmpty()) {
				table.addRow(sourceFileName, "[none]");
			} else {
				table.addRow(sourceFileName, String.join(System.lineSeparator(), targetFileNames));
			}
		}


		return table;
	}

	private Map<String, Set<String>> calculateJarFileDependencies(Classpath classpath, Map<String, List<String>> index) {

		// map from source JAR file name to target JAR file name
		Map<String, Set<String>> dependencies = new TreeMap<>();

		// for every JAR file ...
		List<JarFile> jarFiles = classpath.getJarFiles();
		for (JarFile jarFile : jarFiles) {
			String jarFileName = jarFile.getFileName();

			// for every class definition ...
			List<ClassDef> classDefs = jarFile.getClassDefs();
			for (ClassDef classDef : classDefs) {

				// for every class reference ...
				List<ClassRef> classRefs = classDef.getClassRefs();
				for (ClassRef classRef : classRefs) {

					// get target JAR file names
					String className = classRef.getClassName();
					List<String> targetFileNames = index.get(className);
					if (targetFileNames == null) continue; // ignore unknown class

					for (String targetFileName : targetFileNames) {
						if (targetFileName.equals(jarFileName)) continue; // ignore references to classes in same JAR file

						Set<String> list = dependencies.computeIfAbsent(jarFileName, c -> new TreeSet<>());
						list.add(targetFileName);
					}
				}
			}
		}

		return dependencies;
	}

	private Map<String, List<String>> createClassNameToJarFileIndex(Classpath classpath) {

		// map from class name to JAR file name
		Map<String, List<String>> index = new TreeMap<>();

		// for every JAR file ...
		List<JarFile> jarFiles = classpath.getJarFiles();
		for (JarFile jarFile : jarFiles) {
			String jarFileName = jarFile.getFileName();

			// for every class definition ...
			List<ClassDef> classDefs = jarFile.getClassDefs();
			for (ClassDef classDef : classDefs) {

				String className = classDef.getClassName();
				List<String> list = index.computeIfAbsent(className, c -> new ArrayList<>());
				list.add(jarFileName);

			}
		}

		return index;
	}

}
