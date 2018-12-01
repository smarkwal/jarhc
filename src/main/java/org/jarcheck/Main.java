package org.jarcheck;

import org.jarcheck.analyzer.ClassVersionAnalyzer;
import org.jarcheck.analyzer.JarFilesListAnalyzer;
import org.jarcheck.loader.ClasspathLoader;
import org.jarcheck.model.Classpath;
import org.jarcheck.report.Report;
import org.jarcheck.report.ReportFormat;
import org.jarcheck.report.html.HtmlReportFormat;

import java.io.File;
import java.io.IOException;

public class Main {

	public static void main(String[] args) {
		System.out.println("JarCheck 1.0-SNAPSHOT");
		System.out.println("=====================");
		System.out.println();

		CommandLine commandLine = new CommandLine(args);
		int exitCode = commandLine.parse();
		if (exitCode != 0) {
			System.exit(exitCode);
		}

		Options options = commandLine.getOptions();
		File directory = options.getDirectory();

		ClasspathLoader loader = new ClasspathLoader();
		Classpath classpath = null;
		try {
			classpath = loader.load(directory, true);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(2);
		}

		// analyze classpath and create report
		Analysis analysis = new Analysis(
				new JarFilesListAnalyzer(),
				new ClassVersionAnalyzer()
		);
		Report report = analysis.run(classpath);

		// format report as text to STDOUT
//		ReportFormat format = new TextReportFormat();
		ReportFormat format = new HtmlReportFormat();
		format.format(report, System.out);

	}

}