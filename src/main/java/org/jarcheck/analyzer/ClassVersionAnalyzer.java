package org.jarcheck.analyzer;

import org.jarcheck.model.ClassDef;
import org.jarcheck.model.Classpath;
import org.jarcheck.model.JarFile;
import org.jarcheck.report.ReportSection;
import org.jarcheck.report.ReportTable;
import org.jarcheck.utils.JavaVersion;

import java.util.List;

public class ClassVersionAnalyzer extends Analyzer {

	@Override
	public ReportSection analyze(Classpath classpath) {

		ReportTable table = buildTable(classpath);

		ReportSection section = new ReportSection("Class Versions", "Java class file format information.");
		section.add(table);
		return section;
	}

	private ReportTable buildTable(Classpath classpath) {

		ReportTable table = new ReportTable("JAR file", "Java version");

		int minClassVersion_Classpath = Integer.MAX_VALUE;
		int maxClassVersion_Classpath = Integer.MIN_VALUE;

		// for every JAR file ...
		List<JarFile> jarFiles = classpath.getJarFiles();
		for (JarFile jarFile : jarFiles) {

			int minClassVersion_JarFile = Integer.MAX_VALUE;
			int maxClassVersion_JarFile = Integer.MIN_VALUE;

			List<ClassDef> classDefs = jarFile.getClassDefs();
			if (classDefs.isEmpty()) {
				table.addRow(jarFile.getFileName(), "[no class files]");
				continue;
			}

			for (ClassDef classDef : classDefs) {
				int classVersion = classDef.getMajorClassVersion();

				// update min/max class version for JAR file
				minClassVersion_JarFile = Math.min(minClassVersion_JarFile, classVersion);
				maxClassVersion_JarFile = Math.max(maxClassVersion_JarFile, classVersion);
			}

			// add row for JAR file
			String javaVersions = formatJavaVersions(minClassVersion_JarFile, maxClassVersion_JarFile);
			table.addRow(jarFile.getFileName(), javaVersions);

			// update min/max class version for classpath
			minClassVersion_Classpath = Math.min(minClassVersion_Classpath, minClassVersion_JarFile);
			maxClassVersion_Classpath = Math.max(maxClassVersion_Classpath, maxClassVersion_JarFile);
		}

		if (minClassVersion_Classpath > 0) {
			// add row with summary
			String javaVersions = formatJavaVersions(minClassVersion_Classpath, maxClassVersion_Classpath);
			table.addRow("Classpath", javaVersions);
		}

		return table;
	}

	private static String formatJavaVersions(int minClassVersion, int maxClassVersion) {
		if (minClassVersion == maxClassVersion) {
			return JavaVersion.fromClassVersion(minClassVersion);
		} else {
			String minJavaVersion = JavaVersion.fromClassVersion(minClassVersion);
			String maxJavaVersion = JavaVersion.fromClassVersion(maxClassVersion);
			return String.format("%s - %s", minJavaVersion, maxJavaVersion);
		}
	}

}
