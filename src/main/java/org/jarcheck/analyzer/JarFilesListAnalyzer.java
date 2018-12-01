package org.jarcheck.analyzer;

import org.jarcheck.model.Classpath;
import org.jarcheck.model.JarFile;
import org.jarcheck.report.ReportSection;
import org.jarcheck.report.ReportTable;

import java.text.DecimalFormat;

public class JarFilesListAnalyzer extends Analyzer {

	@Override
	public ReportSection analyze(Classpath classpath) {

		ReportTable table = new ReportTable("JAR file", "Size", "Java class files");

		long totalFileSize = 0;
		int totalClassCount = 0;

		for (JarFile jarFile : classpath.getJarFiles()) {
			String fileName = jarFile.getFileName();
			long fileSize = jarFile.getFileSize();
			int classCount = jarFile.getClassDefs().size();
			table.addRow(fileName, formatFileSize(fileSize), String.valueOf(classCount));

			totalFileSize += fileSize;
			totalClassCount += classCount;
		}

		table.addRow("Classpath", formatFileSize(totalFileSize), String.valueOf(totalClassCount));


		ReportSection section = new ReportSection("JAR Files", "List of JAR files found in classpath.");
		section.append(table.toString());
		return section;
	}

	private static String formatFileSize(long fileSize) {
		if (fileSize < 1024) return String.format("%d B", fileSize);
		double size = fileSize / 1024d;
		if (size < 1024) return String.format("%s KB", formatNumber(size));
		size = size / 1024d;
		return String.format("%s MB", formatNumber(size));
	}

	private static String formatNumber(double number) {
		DecimalFormat format;
		if (number < 10) {
			format = new DecimalFormat("0.00");
		} else if (number < 100) {
			format = new DecimalFormat("0.0");
		} else {
			format = new DecimalFormat("0");
		}
		return format.format(number);
	}

}
