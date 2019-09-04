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

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import org.jarhc.analyzer.AnalyzerDescription;
import org.jarhc.analyzer.AnalyzerRegistry;
import org.jarhc.artifacts.Artifact;
import org.jarhc.java.ClassLoaderStrategy;
import org.jarhc.report.ReportFormatFactory;
import org.jarhc.utils.ArrayUtils;
import org.jarhc.utils.FileUtils;
import org.jarhc.utils.ResourceUtils;
import org.jarhc.utils.VersionUtils;

public class CommandLineParser {

	// TODO: inject dependency
	private final AnalyzerRegistry registry = new AnalyzerRegistry();

	private final PrintStream out;
	private final PrintStream err;

	public CommandLineParser(PrintStream out, PrintStream err) {
		if (out == null) throw new IllegalArgumentException("out");
		if (err == null) throw new IllegalArgumentException("err");
		this.out = out;
		this.err = err;
	}

	public Options parse(String[] args) throws CommandLineException {

		Options options = new Options();

		options.setRemoveVersion(false);
		options.setUseArtifactName(false);

		options.setReportTitle("JAR Health Check Report");
		options.setReportFormat(null);
		options.setReportFile(null);

		boolean classpathFound = false;

		Iterator<String> iterator = Arrays.asList(args).iterator();
		while (iterator.hasNext()) {
			String arg = iterator.next();
			if (arg.equals("-cp") || arg.equals("--classpath")) {
				parse_classpath(iterator, options);
				classpathFound = true;
			} else if (arg.equals("--provided")) {
				parse_provided(iterator, options);
			} else if (arg.equals("--runtime")) {
				parse_runtime(iterator, options);
			} else if (arg.equals("--strategy")) {
				parse_strategy(iterator, options);
			} else if (arg.equals("-f") || arg.equals("--format")) {
				parse_format(iterator, options);
			} else if (arg.equals("-o") || arg.equals("--output")) {
				parse_output(iterator, options);
			} else if (arg.equals("-t") || arg.equals("--title")) {
				parse_title(iterator, options);
			} else if (arg.equals("-s") || arg.equals("--sections")) {
				parse_sections(iterator, options);
			} else if (arg.equals("--skip-empty")) {
				options.setSkipEmpty(true);
			} else if (arg.equals("--remove-version")) {
				options.setRemoveVersion(true);
			} else if (arg.equals("--use-artifact-name")) {
				options.setUseArtifactName(true);
			} else if (arg.equals("-h") || arg.equals("--help")) {
				printUsage(null, out);
			} else if (arg.equals("-v") || arg.equals("--version")) {
				out.println("JarHC - JAR Health Check " + VersionUtils.getVersion());
			} else if (arg.equals("--data")) {
				parse_data(iterator, options);
			} else if (arg.equals("--nodata")) {
				options.setDataPath(null);
			} else if (arg.equals("--debug")) {
				options.setDebug(true);
			} else if (arg.startsWith("-")) {
				String errorMessage = String.format("Unknown option: '%s'.", arg);
				handleError(-100, errorMessage);
			} else {
				addSources(arg, options::addClasspathJarPath);
				classpathFound = true;
			}
		}

		// if path argument is missing ...
		if (!classpathFound) {

			// if help or version information has been printed ...
			if (ArrayUtils.containsAny(args, "-h", "--help", "-v", "--version")) {
				// stop execution (path argument is not mandatory)
				throw new CommandLineException(0, "OK");
			}

			String errorMessage = "Argument <path> is missing.";
			handleError(-1, errorMessage);
		}

		// check if at least one JAR file has been found
		List<String> classpathJarPaths = options.getClasspathJarPaths();
		if (classpathJarPaths.isEmpty()) {
			String errorMessage = "No *.jar files found in classpath.";
			handleError(-4, errorMessage);
		}

		return options;
	}

	private void parse_classpath(Iterator<String> iterator, Options options) throws CommandLineException {
		if (iterator.hasNext()) {
			String values = iterator.next();
			addSources(values, options::addClasspathJarPath);
		} else {
			handleError(-10, "Classpath not specified.");
		}
	}

	private void parse_provided(Iterator<String> iterator, Options options) throws CommandLineException {
		if (iterator.hasNext()) {
			String values = iterator.next();
			addSources(values, options::addProvidedJarPath);
		} else {
			handleError(-10, "Provided classpath not specified.");
		}
	}

	private void parse_runtime(Iterator<String> iterator, Options options) throws CommandLineException {
		if (iterator.hasNext()) {
			String values = iterator.next();
			addSources(values, options::addRuntimeJarPath);
		} else {
			handleError(-10, "Runtime classpath not specified.");
		}
	}

	private void parse_strategy(Iterator<String> iterator, Options options) throws CommandLineException {
		if (iterator.hasNext()) {
			String value = iterator.next();
			ClassLoaderStrategy strategy = null;
			try {
				strategy = ClassLoaderStrategy.valueOf(value);
			} catch (IllegalArgumentException e) {
				String errorMessage = String.format("Unknown class loader strategy: %s", value);
				handleError(-13, errorMessage);
			}
			options.setClassLoaderStrategy(strategy);
		} else {
			handleError(-12, "Class loader strategy not specified.");
		}
	}

	private void parse_format(Iterator<String> iterator, Options options) throws CommandLineException {
		if (iterator.hasNext()) {
			String value = iterator.next();
			if (!ReportFormatFactory.isSupportedFormat(value)) {
				String errorMessage = String.format("Unknown report format: '%s'.", value);
				handleError(-6, errorMessage);
			}
			options.setReportFormat(value);
		} else {
			handleError(-5, "Report format not specified.");
		}
	}

	private void parse_output(Iterator<String> iterator, Options options) throws CommandLineException {
		if (iterator.hasNext()) {
			String value = iterator.next();
			options.setReportFile(value);
		} else {
			handleError(-7, "Report file not specified.");
		}
	}

	private void parse_title(Iterator<String> iterator, Options options) throws CommandLineException {
		if (iterator.hasNext()) {
			String value = iterator.next();
			options.setReportTitle(value);
		} else {
			handleError(-8, "Report title not specified.");
		}
	}

	private void parse_sections(Iterator<String> iterator, Options options) throws CommandLineException {
		if (iterator.hasNext()) {
			String value = iterator.next();
			if (value.startsWith("-")) {
				value = value.substring(1);
				// exclude specified sections
				List<String> sections = registry.getCodes();
				String[] values = value.split(",");
				for (String section : values) {
					sections.remove(section.trim());
				}
				options.setSections(sections);
			} else {
				// include specified sections
				List<String> sections = new ArrayList<>();
				String[] values = value.split(",");
				for (String section : values) {
					sections.add(section.trim());
				}
				options.setSections(sections);
			}
		} else {
			handleError(-9, "Report sections not specified.");
		}
	}

	private void parse_data(Iterator<String> iterator, Options options) throws CommandLineException {
		if (iterator.hasNext()) {
			String value = iterator.next();
			options.setDataPath(value);
		} else {
			handleError(-11, "Data path not specified.");
		}
	}

	private void addSources(String values, Consumer<String> classpath) throws CommandLineException {
		for (String value : values.split(",")) {
			value = value.trim();
			addSource(value, classpath);
		}
	}

	private void addSource(String value, Consumer<String> classpath) throws CommandLineException {

		// if value is Maven artifact coordinates ...
		if (Artifact.validateCoordinates(value)) {
			classpath.accept(value);
		} else {
			// value must be a path to a file or directory
			File path = new File(value);
			if (path.isFile()) {
				addFile(path, value, classpath);
			} else if (path.isDirectory()) {
				addFiles(path, value, classpath);
			} else {
				String errorMessage = String.format("File or directory not found: %s", path.getAbsolutePath());
				handleError(-3, errorMessage);
			}
		}

	}

	private void addFile(File path, String value, Consumer<String> classpath) throws CommandLineException {
		String fileName = path.getName().toLowerCase();
		if (fileName.endsWith(".jar")) {
			classpath.accept(value);
		} else if (fileName.endsWith(".war")) {
			classpath.accept(value);
		} else {
			String errorMessage = String.format("File is not a *.jar file: %s", path.getAbsolutePath());
			handleError(-2, errorMessage);
		}
	}

	private void addFiles(File path, String value, Consumer<String> classpath) throws CommandLineException {
		if (findJarFiles(path).isEmpty()) {
			String errorMessage = String.format("No *.jar files found in directory: %s", path.getAbsolutePath());
			handleError(-2, errorMessage);
		}
		classpath.accept(value);
	}

	static List<File> findJarFiles(File directory) {
		List<File> jarFiles = new ArrayList<>();
		findJarFiles(directory, jarFiles);
		return jarFiles;
	}

	private static void findJarFiles(File directory, List<File> jarFiles) {

		// get all files and directories in current directory
		File[] files = directory.listFiles();
		if (files == null) return;

		// sort files and directories by name (case-insensitive)
		// (this guarantees a "stable" order across different platforms)
		Arrays.sort(files, FileUtils::compareByName);

		// collect all JAR files in current directory
		for (File file : files) {
			if (file.isFile()) {
				String fileName = file.getName().toLowerCase();
				if (fileName.endsWith(".jar")) {
					jarFiles.add(file);
				}
			}
		}

		// collect all JAR files in subdirectories
		for (File file : files) {
			if (file.isDirectory()) {
				findJarFiles(file, jarFiles);
			}
		}
	}

	private void handleError(int exitCode, String errorMessage) throws CommandLineException {
		printUsage(errorMessage, err);
		throw new CommandLineException(exitCode, errorMessage);
	}

	private void printUsage(String errorMessage, PrintStream out) {

		// optional: print error message
		if (errorMessage != null) {
			out.println(errorMessage);
		}

		// load usage text from resource
		String usage;
		try {
			usage = ResourceUtils.getResourceAsString("/usage.txt", "UTF-8");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		// print usage text
		out.println(usage);

		// append list of sections
		List<String> codes = registry.getCodes();
		for (String code : codes) {
			AnalyzerDescription description = registry.getDescription(code);
			out.println(String.format("   %-2s - %s", code, description.getName()));
		}

	}

}
