package org.jarcheck.analyzer;

import org.jarcheck.model.ClassDef;
import org.jarcheck.model.Classpath;
import org.jarcheck.model.JarFile;
import org.jarcheck.report.ReportSection;
import org.jarcheck.report.ReportTable;

import java.util.List;
import java.util.Properties;

public class ShadowedClassesAnalyzer extends Analyzer {

	@Override
	public ReportSection analyze(Classpath classpath) {

		ReportTable table = buildTable(classpath);

		StringBuilder description = new StringBuilder("Classes shadowing JRE/JDK classes.").append(System.lineSeparator());

		// print information about JRE/JDK in description
		Properties properties = System.getProperties();
		description.append("Java home   : ").append(properties.getProperty("java.home")).append(System.lineSeparator());
		description.append("Java runtime: ").append(properties.getProperty("java.runtime.name")).append(System.lineSeparator());
		description.append("Java version: ").append(properties.getProperty("java.version")).append(System.lineSeparator());
		description.append("Java vendor : ").append(properties.getProperty("java.vendor"));

		ReportSection section = new ReportSection("Shadowed Classes", description.toString());
		section.add(table);
		return section;
	}

	private ReportTable buildTable(Classpath classpath) {

		ClassLoader parentClassLoader = this.getClass().getClassLoader().getParent();

		ReportTable table = new ReportTable("Class name", "JAR file", "ClassLoader");

		// for every JAR file ...
		List<JarFile> jarFiles = classpath.getJarFiles();
		for (JarFile jarFile : jarFiles) {

			// for every class definition ...
			List<ClassDef> classDefs = jarFile.getClassDefs();
			for (ClassDef classDef : classDefs) {

				String className = classDef.getClassName();

				String realClassName = formatClassName(className);

				Class cls;
				try {
					cls = Class.forName(realClassName, false, parentClassLoader);
				} catch (ClassNotFoundException e) {
					continue;
				} catch (Throwable t) {
					// TODO: ignore ?
					continue;
				}

				ClassLoader classLoader = cls.getClassLoader();
				String classLoaderInfo = classLoader != null ? classLoader.toString() : "Bootstrap";
				table.addRow(realClassName, jarFile.getFileName(), classLoaderInfo);

			}
		}

		return table;
	}

	private static String formatClassName(String name) {
		return name.replaceAll("/", ".");
	}

}
