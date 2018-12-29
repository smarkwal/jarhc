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

import org.jarhc.analyzer.AnalyzerDescription;
import org.jarhc.analyzer.AnalyzerRegistry;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class CommandLineParser {

	private final PrintStream err;

	public CommandLineParser(PrintStream err) {
		if (err == null) throw new IllegalArgumentException("err");
		this.err = err;
	}

	Options parse(String[] args) throws CommandLineException {

		if (args.length < 1) {
			String errorMessage = "Argument <path> is missing.";
			handleError(-1, errorMessage);
		}

		List<File> paths = new ArrayList<>();
		List<String> sections = null;
		String reportTitle = "JAR Health Check Report";
		String reportFormat = null;
		String reportFile = null;

		Iterator<String> iterator = Arrays.asList(args).iterator();
		while (iterator.hasNext()) {
			String arg = iterator.next();
			if (arg.equals("-f") || arg.equals("--format")) {
				if (iterator.hasNext()) {
					reportFormat = iterator.next();
					if (!reportFormat.equals("text") && !reportFormat.equals("html")) {
						String errorMessage = String.format("Unknown report format: '%s'.", reportFormat);
						handleError(-6, errorMessage);
					}
				} else {
					handleError(-5, "Report format not specified.");
				}
			} else if (arg.equals("-o") || arg.equals("--output")) {
				if (iterator.hasNext()) {
					reportFile = iterator.next();
				} else {
					handleError(-7, "Report file not specified.");
				}
			} else if (arg.equals("-t") || arg.equals("--title")) {
				if (iterator.hasNext()) {
					reportTitle = iterator.next();
				} else {
					handleError(-8, "Report title not specified.");
				}
			} else if (arg.equals("-s") || arg.equals("--sections")) {
				if (iterator.hasNext()) {
					String value = iterator.next();
					String[] values = value.split(",");
					sections = Arrays.stream(values).map(String::trim).collect(Collectors.toList());
				} else {
					handleError(-8, "Report sections not specified.");
				}
			} else if (arg.startsWith("-")) {
				String errorMessage = String.format("Unknown option: '%s'.", arg);
				handleError(-100, errorMessage);
			} else {
				File path = new File(arg);
				paths.add(path);
			}
		}

		if (reportFormat == null) {
			if (reportFile != null) {
				// guess report format from filename extension
				if (reportFile.endsWith(".txt")) {
					reportFormat = "text";
				} else if (reportFile.endsWith(".html")) {
					reportFormat = "html";
				} else {
					reportFormat = "text"; // use default report format
				}
			} else {
				reportFormat = "text"; // use default report format
			}
		}

		// collect JAR files
		List<File> jarFiles = new ArrayList<>();
		collectJarFiles(paths, true, jarFiles);

		// check if at least one JAR file has been found
		if (jarFiles.isEmpty()) {
			String errorMessage = "No *.jar files found in path.";
			handleError(-4, errorMessage);
		}

		// exit code 0 -> no errors
		return new Options(jarFiles, sections, reportTitle, reportFormat, reportFile);
	}

	private void collectJarFiles(List<File> paths, boolean strict, List<File> jarFiles) throws CommandLineException {

		for (File path : paths) {
			if (path.isFile()) {
				// if file is a *.jar file ...
				String fileName = path.getName().toLowerCase();
				if (fileName.endsWith(".jar")) {
					jarFiles.add(path);
				} else if (strict) {
					String errorMessage = String.format("File is not a *.jar file: %s", path.getAbsolutePath());
					handleError(-2, errorMessage);
				}
			} else if (path.isDirectory()) {
				File[] array = path.listFiles();
				if (array == null) continue;
				collectJarFiles(Arrays.asList(array), false, jarFiles);
			} else if (strict) {
				String errorMessage = String.format("File or directory not found: %s", path.getAbsolutePath());
				handleError(-3, errorMessage);
			}
		}
	}

	private void handleError(int exitCode, String errorMessage) throws CommandLineException {
		printUsage(errorMessage);
		throw new CommandLineException(exitCode, errorMessage);
	}

	private void printUsage(String errorMessage) {
		if (errorMessage != null) {
			err.println(errorMessage);
		}
		err.println("Usage: java -jar JarHC.jar [options] <path> [<path>]*");
		err.println("   <path>: Path to JAR file or directory with JAR files.");
		err.println();
		err.println("Options:");
		err.println("   -f <type> | --format <type>: Report format type ('text' or 'html').");
		err.println("   -o <file> | --output <file>: Report file path.");
		err.println("   -t <text> | --title <text>: Report title.");
		err.println("   -s <sections> | --sections <sections>: List of report sections (example: 'jf,dc,sc').");
		err.println();
		err.println("Sections:");

		// TODO: find a better solution than creating an instance here
		AnalyzerRegistry registry = new AnalyzerRegistry();
		List<String> codes = registry.getCodes();
		for (String code : codes) {
			AnalyzerDescription description = registry.getDescription(code);
			err.println(String.format("   %-2s - %s", code, description.getName()));
		}

	}

}
