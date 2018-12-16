/*
 * Copyright 2018 Stephan Markwalder
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jarhc.app;

import org.jarhc.analyzer.Analysis;
import org.jarhc.loader.ClasspathLoader;
import org.jarhc.model.Classpath;
import org.jarhc.report.Report;
import org.jarhc.report.ReportFormat;
import org.jarhc.report.ReportFormatFactory;
import org.jarhc.utils.StringUtils;
import org.jarhc.utils.VersionUtils;

import java.io.*;
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

		String version = VersionUtils.getVersion();
		out.println("JarHC - JAR Health Check " + version);
		out.println("=========================" + StringUtils.repeat("=", version.length()));
		out.println();

		// parse command line
		Options options;
		try {
			options = commandLineParser.parse(args);
		} catch (CommandLineException e) {
			// note: error message has already been printed

			// return with exit code
			return e.getExitCode();
		}

		out.println("Scan JAR files ...");

		// scan JAR files
		List<File> files = options.getJarFiles();
		Classpath classpath;
		try {
			ClasspathLoader loader = new ClasspathLoader();
			classpath = loader.load(files);
		} catch (IOException e) {
			e.printStackTrace(err);
			return 1;
		}

		out.println("Analyze classpath ...");

		// analyze classpath
		Analysis analysis = FullAnalysis.build();
		Report report = analysis.run(classpath);

		out.println("Create report ...");

		// create report format and output stream
		ReportFormat format = createReportFormat(options);
		OutputStream stream;
		try {
			stream = createReportStream(options);
		} catch (FileNotFoundException e) {
			e.printStackTrace(err);
			return 2; // TODO: exit code?
		}

		out.println();

		// print report
		PrintWriter writer = new PrintWriter(stream, true);
		format.format(report, writer);
		writer.flush();

		// if report has been written into a file ...
		if (stream instanceof FileOutputStream) {
			// close the file stream
			try {
				stream.close();
			} catch (IOException e) {
				// ignore
			}
		}

		return 0;
	}

	private ReportFormat createReportFormat(Options options) {
		String format = options.getReportFormat();
		ReportFormatFactory factory = new ReportFormatFactory(); // TODO: inject dependency
		return factory.getReportFormat(format);
	}

	private OutputStream createReportStream(Options options) throws FileNotFoundException {
		String file = options.getReportFile();
		if (file == null) {
			return this.out; // use STDOUT
		} else {
			return new FileOutputStream(file);
		}
	}

}
