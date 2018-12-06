package org.jarcheck.analyzer;

import org.jarcheck.model.ClassDef;
import org.jarcheck.model.ClassRef;
import org.jarcheck.model.Classpath;
import org.jarcheck.model.JarFile;
import org.jarcheck.report.ReportSection;
import org.jarcheck.report.ReportTable;
import org.jarcheck.utils.JavaUtils;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class UnknownClassesAnalyzer extends Analyzer {

	@Override
	public ReportSection analyze(Classpath classpath) {

		ReportTable table = buildTable(classpath);

		ReportSection section = new ReportSection("Unknown Classes", "References to classes not found on the classpath.");
		section.add(table);
		return section;
	}

	private ReportTable buildTable(Classpath classpath) {
		ReportTable table = new ReportTable("JAR file", "Unknown class");

		// for every JAR file ...
		List<JarFile> jarFiles = classpath.getJarFiles();
		for (JarFile jarFile : jarFiles) {

			// find all unknown classes
			Set<String> unknownClasses = collectUnknownClasses(jarFile, classpath);
			if (unknownClasses.isEmpty()) continue;

			table.addRow(jarFile.getFileName(), String.join(System.lineSeparator(), unknownClasses));
		}

		return table;
	}

	private Set<String> collectUnknownClasses(JarFile jarFile, Classpath classpath) {
		Set<String> unknownClasses = new TreeSet<>();

		// for every class definition ...
		List<ClassDef> classDefs = jarFile.getClassDefs();
		for (ClassDef classDef : classDefs) {

			// for every class reference ...
			List<ClassRef> classRefs = classDef.getClassRefs();
			for (ClassRef classRef : classRefs) {
				String className = classRef.getClassName();

				// check if class exists
				boolean exists = findClass(classpath, className);
				if (!exists) {
					className = className.replace('/', '.');
					unknownClasses.add(className);
				}
			}
		}

		return unknownClasses;
	}

	private boolean findClass(Classpath classpath, String className) {

		// check if class exists in classpath
		Set<ClassDef> classDefs = classpath.getClassDefs(className);
		if (classDefs != null) return true;

		// check if class is a Java bootstrap class
		return JavaUtils.isBootstrapClass(className);

	}

}
