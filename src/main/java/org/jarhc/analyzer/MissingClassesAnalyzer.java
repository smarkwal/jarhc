package org.jarhc.analyzer;

import org.jarhc.model.ClassDef;
import org.jarhc.model.ClassRef;
import org.jarhc.model.Classpath;
import org.jarhc.model.JarFile;
import org.jarhc.report.ReportSection;
import org.jarhc.report.ReportTable;
import org.jarhc.utils.JavaUtils;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class MissingClassesAnalyzer extends Analyzer {

	@Override
	public ReportSection analyze(Classpath classpath) {

		ReportTable table = buildTable(classpath);

		ReportSection section = new ReportSection("Missing Classes", "References to classes not found on the classpath.");
		section.add(table);
		return section;
	}

	private ReportTable buildTable(Classpath classpath) {
		ReportTable table = new ReportTable("JAR file", "Missing class");

		// for every JAR file ...
		List<JarFile> jarFiles = classpath.getJarFiles();
		for (JarFile jarFile : jarFiles) {

			// find all missing classes
			Set<String> missingClasses = collectMissingClasses(jarFile, classpath);
			if (missingClasses.isEmpty()) continue;

			table.addRow(jarFile.getFileName(), String.join(System.lineSeparator(), missingClasses));
		}

		return table;
	}

	private Set<String> collectMissingClasses(JarFile jarFile, Classpath classpath) {
		Set<String> missingClasses = new TreeSet<>();

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
					missingClasses.add(className);
				}
			}
		}

		return missingClasses;
	}

	private boolean findClass(Classpath classpath, String className) {

		// check if class exists in classpath
		Set<ClassDef> classDefs = classpath.getClassDefs(className);
		if (classDefs != null) return true;

		// check if class is a Java bootstrap class
		return JavaUtils.isBootstrapClass(className);

	}

}
