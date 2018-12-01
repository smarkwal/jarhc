package org.jarcheck;

import org.jarcheck.analyzer.ClassVersionAnalyzer;
import org.jarcheck.analyzer.JarFilesListAnalyzer;
import org.jarcheck.loader.ClasspathLoader;
import org.jarcheck.model.Classpath;
import org.jarcheck.report.Report;
import org.jarcheck.report.ReportSection;

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

		try {
			ClasspathLoader loader = new ClasspathLoader();
			Classpath classpath = loader.load(directory, true);

			Analysis analysis = new Analysis(
					new JarFilesListAnalyzer(),
					new ClassVersionAnalyzer()
			);

			Report report = analysis.run(classpath);
			for (ReportSection section : report.getSections()) {

				String title = section.getTitle();
				String description = section.getDescription();
				String text = section.getText();

				System.out.println(title);
				System.out.println(title.replaceAll(".", "-"));
				if (description != null) {
					System.out.println(description);
				}
				System.out.println();
				System.out.println(text);

			}

		} catch (IOException e) {
			e.printStackTrace();
			System.exit(2);
		}

	}

}