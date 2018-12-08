package org.jarcheck.app;

import org.jarcheck.analyzer.Analysis;
import org.jarcheck.loader.ClasspathLoader;
import org.jarcheck.model.Classpath;
import org.jarcheck.report.Report;
import org.jarcheck.report.ReportFormat;
import org.jarcheck.report.text.TextReportFormat;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.List;

public class Application {

	private final CommandLineParser commandLineParser;
	private final PrintStream out;
	private final PrintStream err;

	public Application(CommandLineParser commandLineParser, PrintStream out, PrintStream err) {
		this.commandLineParser = commandLineParser;
		this.out = out;
		this.err = err;
	}

	public int run(String[] args) {

		out.println("JarCheck 1.0-SNAPSHOT");
		out.println("=====================");
		out.println();

		// parse command line
		Options options = commandLineParser.parse(args);
		if (options.getErrorCode() != 0) {
			return options.getErrorCode();
		}
		List<File> files = options.getFiles();

		out.println("Scan JAR files ...");

		Classpath classpath;
		try {
			ClasspathLoader loader = new ClasspathLoader();
			classpath = loader.load(files);
		} catch (IOException e) {
			e.printStackTrace(err);
			return 1;
		}

		out.println("Analyze classpath ...");
		out.println();

		// analyze classpath and create report
		Analysis analysis = FullAnalysis.build();
		Report report = analysis.run(classpath);

		// format report as text to STDOUT
		ReportFormat format = new TextReportFormat();
		// ReportFormat format = new HtmlReportFormat();

		PrintWriter writer = new PrintWriter(out, true);
		format.format(report, writer);
		writer.flush();

		return 0;
	}

}
