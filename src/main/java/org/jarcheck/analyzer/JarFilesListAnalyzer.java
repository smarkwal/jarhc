package org.jarcheck.analyzer;

import org.jarcheck.model.Classpath;
import org.jarcheck.model.JarFile;
import org.jarcheck.report.ReportSection;
import org.jarcheck.report.ReportTable;

import java.util.List;

import static org.jarcheck.utils.FileUtils.formatFileSize;

public class JarFilesListAnalyzer extends Analyzer {

	@Override
	public ReportSection analyze(Classpath classpath) {

		ReportTable table = buildTable(classpath);

		ReportSection section = new ReportSection("JAR Files", "List of JAR files found in classpath.");
		section.add(table);
		return section;
	}

	private ReportTable buildTable(Classpath classpath) {

		ReportTable table = new ReportTable("JAR file", "Size", "Java class files");

		// total values
		long totalFileSize = 0;
		int totalClassCount = 0;

		// for every JAR file ...
		List<JarFile> jarFiles = classpath.getJarFiles();
		for (JarFile jarFile : jarFiles) {

			// add a row with file name, size and class count
			String fileName = jarFile.getFileName();
			long fileSize = jarFile.getFileSize();
			int classCount = jarFile.getClassDefs().size();
			table.addRow(fileName, formatFileSize(fileSize), String.valueOf(classCount));

			// update total values
			totalFileSize += fileSize;
			totalClassCount += classCount;
		}

		// add a row with total values
		table.addRow("Classpath", formatFileSize(totalFileSize), String.valueOf(totalClassCount));

		return table;
	}

}
