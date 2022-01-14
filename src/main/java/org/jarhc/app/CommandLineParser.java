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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import org.jarhc.analyzer.AnalyzerDescription;
import org.jarhc.analyzer.AnalyzerRegistry;
import org.jarhc.artifacts.Artifact;
import org.jarhc.java.ClassLoaderStrategy;
import org.jarhc.report.ReportFormatFactory;
import org.jarhc.utils.ArrayUtils;
import org.jarhc.utils.FileUtils;
import org.jarhc.utils.JarHcException;
import org.jarhc.utils.ResourceUtils;
import org.jarhc.utils.VersionUtils;

public class CommandLineParser {

	// TODO: inject dependency
	private final AnalyzerRegistry registry = new AnalyzerRegistry(null);

	private final PrintStream out;
	private final PrintStream err;

	private final Map<String, OptionParser> optionParsers = new HashMap<>();

	private interface OptionParser {
		void parse(Iterator<String> args, Options options) throws CommandLineException;
	}

	public CommandLineParser(PrintStream out, PrintStream err) {
		if (out == null) throw new IllegalArgumentException("out");
		if (err == null) throw new IllegalArgumentException("err");
		this.out = out;
		this.err = err;

		optionParsers.put("-r", this::parseRelease);
		optionParsers.put("--release", this::parseRelease);
		optionParsers.put("-cp", this::parseClasspath);
		optionParsers.put("--classpath", this::parseClasspath);
		optionParsers.put("--provided", this::parseProvided);
		optionParsers.put("--runtime", this::parseRuntime);
		optionParsers.put("--strategy", this::parseStrategy);
		optionParsers.put("-f", this::parseFormat);
		optionParsers.put("--format", this::parseFormat);
		optionParsers.put("-o", this::parseOutput);
		optionParsers.put("--output", this::parseOutput);
		optionParsers.put("-t", this::parseTitle);
		optionParsers.put("--title", this::parseTitle);
		optionParsers.put("-s", this::parseSections);
		optionParsers.put("--sections", this::parseSections);
		optionParsers.put("--skip-empty", (args, options) -> options.setSkipEmpty(true));
		optionParsers.put("--remove-version", (args, options) -> options.setRemoveVersion(true));
		optionParsers.put("--use-artifact-name", (args, options) -> options.setUseArtifactName(true));
		optionParsers.put("--ignore-missing-annotations", (args, options) -> options.setIgnoreMissingAnnotations(true));
		optionParsers.put("--data", this::parseData);
		optionParsers.put("--debug", (args, options) -> options.setDebug(true));
		optionParsers.put("--trace", (args, options) -> options.setTrace(true));
		optionParsers.put("-h", (args, options) -> printUsage(null, out));
		optionParsers.put("--help", (args, options) -> printUsage(null, out));
		optionParsers.put("-v", (args, options) -> printVersion());
		optionParsers.put("--version", (args, options) -> printVersion());
	}

	public Options parse(String[] args) throws CommandLineException {

		Options options = new Options();

		options.setRemoveVersion(false);
		options.setUseArtifactName(false);

		options.setReportTitle("JAR Health Check Report");
		options.setReportFormat(null);
		options.setReportFile(null);

		boolean classpathFound = ArrayUtils.containsAny(args, "-cp", "--classpath");

		Iterator<String> iterator = Arrays.asList(args).iterator();
		while (iterator.hasNext()) {
			String arg = iterator.next();
			if (optionParsers.containsKey(arg)) {
				OptionParser parser = optionParsers.get(arg);
				parser.parse(iterator, options);
			} else if (arg.startsWith("-")) {
				String errorMessage = String.format("Unknown option: '%s'.", arg);
				throw handleError(-100, errorMessage);
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
			throw handleError(-1, errorMessage);
		}

		// check if at least one JAR file has been found
		List<String> classpathJarPaths = options.getClasspathJarPaths();
		if (classpathJarPaths.isEmpty()) {
			String errorMessage = "No *.jar files found in classpath.";
			throw handleError(-4, errorMessage);
		}

		return options;
	}

	private void parseRelease(Iterator<String> args, Options options) throws CommandLineException {
		if (!args.hasNext()) throw handleError(-14, "Release not specified.");

		String value = args.next();
		int release;
		try {
			release = Integer.parseInt(value);
		} catch (NumberFormatException e) {
			throw handleError(-15, "Release '" + value + "' is not valid.");
		}
		if (release < 8) {
			throw handleError(-16, "Release " + release + " is not supported.");
		}
		options.setRelease(release);
	}

	private void parseClasspath(Iterator<String> args, Options options) throws CommandLineException {
		if (!args.hasNext()) throw handleError(-10, "Classpath not specified.");

		String values = args.next();
		addSources(values, options::addClasspathJarPath);
	}

	private void parseProvided(Iterator<String> args, Options options) throws CommandLineException {
		if (!args.hasNext()) throw handleError(-10, "Provided classpath not specified.");

		String values = args.next();
		addSources(values, options::addProvidedJarPath);
	}

	private void parseRuntime(Iterator<String> args, Options options) throws CommandLineException {
		if (!args.hasNext()) throw handleError(-10, "Runtime classpath not specified.");

		String values = args.next();
		addSources(values, options::addRuntimeJarPath);
	}

	private void parseStrategy(Iterator<String> args, Options options) throws CommandLineException {
		if (!args.hasNext()) throw handleError(-12, "Class loader strategy not specified.");

		String value = args.next();
		try {
			ClassLoaderStrategy strategy = ClassLoaderStrategy.valueOf(value);
			options.setClassLoaderStrategy(strategy);
		} catch (IllegalArgumentException e) {
			String errorMessage = String.format("Unknown class loader strategy: %s", value);
			throw handleError(-13, errorMessage);
		}
	}

	private void parseFormat(Iterator<String> args, Options options) throws CommandLineException {
		if (!args.hasNext()) throw handleError(-5, "Report format not specified.");

		String value = args.next();
		if (!ReportFormatFactory.isSupportedFormat(value)) {
			String errorMessage = String.format("Unknown report format: '%s'.", value);
			throw handleError(-6, errorMessage);
		}
		options.setReportFormat(value);
	}

	private void parseOutput(Iterator<String> args, Options options) throws CommandLineException {
		if (!args.hasNext()) throw handleError(-7, "Report file not specified.");

		String value = args.next();
		options.setReportFile(value);
	}

	private void parseTitle(Iterator<String> args, Options options) throws CommandLineException {
		if (!args.hasNext()) throw handleError(-8, "Report title not specified.");

		String value = args.next();
		options.setReportTitle(value);
	}

	private void parseSections(Iterator<String> args, Options options) throws CommandLineException {
		if (!args.hasNext()) throw handleError(-9, "Report sections not specified.");

		String value = args.next();
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
	}

	private void parseData(Iterator<String> args, Options options) throws CommandLineException {
		if (!args.hasNext()) throw handleError(-11, "Data path not specified.");

		String value = args.next();
		options.setDataPath(value);
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
				throw handleError(-3, errorMessage);
			}
		}

	}

	private void addFile(File path, String value, Consumer<String> classpath) throws CommandLineException {
		String fileName = path.getName().toLowerCase();
		if (fileName.endsWith(".jar")) {
			classpath.accept(value);
		} else if (fileName.endsWith(".jmod")) {
			classpath.accept(value);
		} else if (fileName.endsWith(".war")) {
			classpath.accept(value);
		} else {
			String errorMessage = String.format("File is not a *.jar file: %s", path.getAbsolutePath());
			throw handleError(-2, errorMessage);
		}
	}

	private void addFiles(File path, String value, Consumer<String> classpath) throws CommandLineException {
		if (findJarFiles(path).isEmpty()) {
			String errorMessage = String.format("No *.jar files found in directory: %s", path.getAbsolutePath());
			throw handleError(-2, errorMessage);
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

	private CommandLineException handleError(int exitCode, String errorMessage) {
		printUsage(errorMessage, err);
		return new CommandLineException(exitCode, errorMessage);
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
			throw new JarHcException(e);
		}

		// print usage text
		out.println(usage);

		// append list of sections
		List<String> codes = registry.getCodes();
		for (String code : codes) {
			AnalyzerDescription description = registry.getDescription(code);
			out.printf("   %-2s - %s%n", code, description.getName());
		}

	}

	private void printVersion() {
		out.println("JarHC - JAR Health Check " + VersionUtils.getVersion());
	}

}
