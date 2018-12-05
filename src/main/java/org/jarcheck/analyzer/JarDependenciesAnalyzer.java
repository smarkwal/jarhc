package org.jarcheck.analyzer;

import org.jarcheck.model.ClassDef;
import org.jarcheck.model.ClassRef;
import org.jarcheck.model.Classpath;
import org.jarcheck.model.JarFile;
import org.jarcheck.report.ReportSection;
import org.jarcheck.report.ReportTable;
import org.jarcheck.utils.MultiMap;

import java.util.List;
import java.util.Set;

public class JarDependenciesAnalyzer extends Analyzer {

	@Override
	public ReportSection analyze(Classpath classpath) {

		MultiMap<String, String> index = createClassNameToJarFileIndex(classpath);
		MultiMap<String, String> dependencies = calculateJarFileDependencies(classpath, index);

		ReportTable table = buildTable(classpath, dependencies);

		ReportSection section = new ReportSection("JAR File Dependencies", "Dependencies between JAR files.");
		section.add(table);
		return section;
	}

	private ReportTable buildTable(Classpath classpath, MultiMap<String, String> dependencies) {

		ReportTable table = new ReportTable("JAR file", "Depends on");

		List<JarFile> jarFiles = classpath.getJarFiles();
		for (JarFile jarFile : jarFiles) {
			String sourceFileName = jarFile.getFileName();
			Set<String> targetFileNames = dependencies.getValues(sourceFileName);
			if (targetFileNames == null || targetFileNames.isEmpty()) {
				table.addRow(sourceFileName, "[none]");
			} else {
				table.addRow(sourceFileName, String.join(System.lineSeparator(), targetFileNames));
			}
		}


		return table;
	}

	private MultiMap<String, String> calculateJarFileDependencies(Classpath classpath, MultiMap<String, String> index) {

		// map from source JAR file name to target JAR file names
		MultiMap<String, String> dependencies = new MultiMap<>();

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
					Set<String> targetFileNames = index.getValues(className);
					if (targetFileNames == null) {
						// ignore unknown class
						continue;
					}

					for (String targetFileName : targetFileNames) {
						if (targetFileName.equals(jarFileName)) {
							// ignore references to classes in same JAR file
							continue;
						}

						dependencies.add(jarFileName, targetFileName);
					}
				}
			}
		}

		return dependencies;
	}

	private MultiMap<String, String> createClassNameToJarFileIndex(Classpath classpath) {

		// map from class name to JAR file names
		MultiMap<String, String> index = new MultiMap<>();

		// for every JAR file ...
		List<JarFile> jarFiles = classpath.getJarFiles();
		for (JarFile jarFile : jarFiles) {
			String jarFileName = jarFile.getFileName();

			// for every class definition ...
			List<ClassDef> classDefs = jarFile.getClassDefs();
			for (ClassDef classDef : classDefs) {
				String className = classDef.getClassName();

				index.add(className, jarFileName);
			}
		}

		return index;
	}

}
