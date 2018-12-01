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

		ReportTable table = new ReportTable("JAR file", "Java version");

		int minClassVersion_Classpath = Integer.MAX_VALUE;
		int maxClassVersion_Classpath = 45;

		for (JarFile jarFile : classpath.getJarFiles()) {

			int minClassVersion_JarFile = Integer.MAX_VALUE;
			int maxClassVersion_JarFile = 45;

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

			String javaVersions = formatJavaVersions(minClassVersion_JarFile, maxClassVersion_JarFile);
			table.addRow(jarFile.getFileName(), javaVersions);

			// update min/max class version for classpath
			minClassVersion_Classpath = Math.min(minClassVersion_Classpath, minClassVersion_JarFile);
			maxClassVersion_Classpath = Math.max(maxClassVersion_Classpath, maxClassVersion_JarFile);

		}

		String javaVersions = formatJavaVersions(minClassVersion_Classpath, maxClassVersion_Classpath);
		table.addRow("Classpath", javaVersions);

		ReportSection section = new ReportSection("Class Versions", "Java class file format information.");
		section.append(table.toString());
		return section;

	}

	private static String formatJavaVersions(int minClassVersion, int maxClassVersion) {
		if (minClassVersion == maxClassVersion) {
			return JavaVersion.fromClassVersion(minClassVersion);
		} else {
			return String.format("%s - %s", JavaVersion.fromClassVersion(minClassVersion), JavaVersion.fromClassVersion(maxClassVersion));
		}
	}

}
