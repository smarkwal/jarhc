package org.jarcheck.analyzer;

import org.jarcheck.env.JavaRuntime;
import org.jarcheck.model.ClassDef;
import org.jarcheck.model.Classpath;
import org.jarcheck.model.JarFile;
import org.jarcheck.report.ReportSection;
import org.jarcheck.report.ReportTable;

import java.util.List;

public class ShadowedClassesAnalyzer extends Analyzer {

	private final JavaRuntime javaRuntime;

	public ShadowedClassesAnalyzer(JavaRuntime javaRuntime) {
		if (javaRuntime == null) throw new IllegalArgumentException("javaRuntime");
		this.javaRuntime = javaRuntime;
	}

	@Override
	public ReportSection analyze(Classpath classpath) {

		ReportTable table = buildTable(classpath);

		StringBuilder description = new StringBuilder("Classes shadowing JRE/JDK classes.").append(System.lineSeparator());

		// print information about JRE/JDK in description
		description.append("Java home   : ").append(javaRuntime.getJavaHome()).append(System.lineSeparator());
		description.append("Java runtime: ").append(javaRuntime.getName()).append(System.lineSeparator());
		description.append("Java version: ").append(javaRuntime.getJavaVersion()).append(System.lineSeparator());
		description.append("Java vendor : ").append(javaRuntime.getJavaVendor());

		ReportSection section = new ReportSection("Shadowed Classes", description.toString());
		section.add(table);
		return section;
	}

	private ReportTable buildTable(Classpath classpath) {

		ReportTable table = new ReportTable("Class name", "JAR file", "ClassLoader");

		// for every JAR file ...
		List<JarFile> jarFiles = classpath.getJarFiles();
		for (JarFile jarFile : jarFiles) {

			// for every class definition ...
			List<ClassDef> classDefs = jarFile.getClassDefs();
			for (ClassDef classDef : classDefs) {

				String className = classDef.getClassName();

				String realClassName = formatClassName(className);
				String classLoader = javaRuntime.getClassLoaderName(realClassName);
				if (classLoader == null) {
					continue;
				}

				table.addRow(realClassName, jarFile.getFileName(), classLoader);

			}
		}

		return table;
	}

	private static String formatClassName(String name) {
		return name.replaceAll("/", ".");
	}

}
