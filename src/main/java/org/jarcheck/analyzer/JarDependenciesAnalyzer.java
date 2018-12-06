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

		MultiMap<String, String> dependencies = calculateJarFileDependencies(classpath);

		ReportTable table = buildTable(classpath, dependencies);

		ReportSection section = new ReportSection("JAR File Dependencies", "Dependencies between JAR files.");
		section.add(table);
		return section;
	}

	private ReportTable buildTable(Classpath classpath, MultiMap<String, String> dependencies) {

		// calculate "used by" dependencies
		MultiMap<String, String> inverted = dependencies.invert();

		ReportTable table = new ReportTable("JAR file", "Uses", "Used by");

		List<JarFile> jarFiles = classpath.getJarFiles();
		for (JarFile jarFile : jarFiles) {
			String jarFileName = jarFile.getFileName();
			Set<String> targetFileNames = dependencies.getValues(jarFileName);
			Set<String> sourceFileNames = inverted.getValues(jarFileName);
			String uses;
			if (targetFileNames == null) {
				uses = "[none]";
			} else {
				uses = String.join(System.lineSeparator(), targetFileNames);
			}
			String usedBy;
			if (sourceFileNames == null) {
				usedBy = "[none]";
			} else {
				usedBy = String.join(System.lineSeparator(), sourceFileNames);
			}
			table.addRow(jarFileName, uses, usedBy);
		}

		return table;
	}

	private MultiMap<String, String> calculateJarFileDependencies(Classpath classpath) {

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

					// get target class definitions
					String className = classRef.getClassName();
					Set<ClassDef> targetClassDefs = classpath.getClassDefs(className);
					if (targetClassDefs == null) {
						// ignore unknown class
						continue;
					}

					// for every class definition ...
					targetClassDefs.forEach(targetClassDef -> {

						// get JAR file
						JarFile targetJarFile = targetClassDef.getJarFile();
						if (targetJarFile == jarFile) {
							// ignore references to classes in same JAR file
							return;
						}

						// add dependency
						String targetFileName = targetJarFile.getFileName();
						dependencies.add(jarFileName, targetFileName);
					});

				}
			}
		}

		return dependencies;
	}

}
