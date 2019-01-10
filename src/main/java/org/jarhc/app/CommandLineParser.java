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
import org.jarhc.utils.ArrayUtils;
import org.jarhc.utils.ResourceUtils;
import org.jarhc.utils.VersionUtils;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

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

		List<File> classpath = new ArrayList<>();
		List<File> provided = new ArrayList<>();
		List<File> runtime = new ArrayList<>();

		Iterator<String> iterator = Arrays.asList(args).iterator();
		while (iterator.hasNext()) {
			String arg = iterator.next();
			if (arg.equals("-cp") || arg.equals("--classpath")) {
				if (iterator.hasNext()) {
					String values = iterator.next();
					addFiles(values, classpath);
				} else {
					handleError(-10, "Classpath not specified.");
				}
			} else if (arg.equals("--provided")) {
				if (iterator.hasNext()) {
					String values = iterator.next();
					addFiles(values, provided);
				} else {
					handleError(-10, "Provided classpath not specified.");
				}
			} else if (arg.equals("--runtime")) {
				if (iterator.hasNext()) {
					String values = iterator.next();
					addFiles(values, runtime);
				} else {
					handleError(-10, "Runtime classpath not specified.");
				}
			} else if (arg.equals("-f") || arg.equals("--format")) {
				if (iterator.hasNext()) {
					String value = iterator.next();
					if (!value.equals("text") && !value.equals("html")) {
						String errorMessage = String.format("Unknown report format: '%s'.", value);
						handleError(-6, errorMessage);
					}
					options.setReportFormat(value);
				} else {
					handleError(-5, "Report format not specified.");
				}
			} else if (arg.equals("-o") || arg.equals("--output")) {
				if (iterator.hasNext()) {
					String value = iterator.next();
					options.setReportFile(value);
				} else {
					handleError(-7, "Report file not specified.");
				}
			} else if (arg.equals("-t") || arg.equals("--title")) {
				if (iterator.hasNext()) {
					String value = iterator.next();
					options.setReportTitle(value);
				} else {
					handleError(-8, "Report title not specified.");
				}
			} else if (arg.equals("-s") || arg.equals("--sections")) {
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
			} else if (arg.equals("--remove-version")) {
				options.setRemoveVersion(true);
			} else if (arg.equals("--use-artifact-name")) {
				options.setUseArtifactName(true);
			} else if (arg.equals("-h") || arg.equals("--help")) {
				printUsage(null, out);
			} else if (arg.equals("-v") || arg.equals("--version")) {
				out.println("JarHC - JAR Health Check " + VersionUtils.getVersion());
			} else if (arg.equals("--debug")) {
				options.setDebug(true);
			} else if (arg.startsWith("-")) {
				String errorMessage = String.format("Unknown option: '%s'.", arg);
				handleError(-100, errorMessage);
			} else {
				File path = new File(arg);
				classpath.add(path);
			}
		}

		// if path argument is missing ...
		if (classpath.isEmpty()) {

			// if help or version information has been printed ...
			if (ArrayUtils.containsAny(args, "-h", "--help", "-v", "--version")) {
				// stop execution (path argument is not mandatory)
				throw new CommandLineException(0, "OK");
			}

			String errorMessage = "Argument <path> is missing.";
			handleError(-1, errorMessage);
		}

		// collect JAR files
		List<File> classpathJarFiles = new ArrayList<>();
		collectJarFiles(classpath, true, classpathJarFiles);
		// check if at least one JAR file has been found
		if (classpathJarFiles.isEmpty()) {
			String errorMessage = "No *.jar files found in classpath.";
			handleError(-4, errorMessage);
		}
		options.addClasspathJarFiles(classpathJarFiles);

		if (!provided.isEmpty()) {
			// collect JAR files for provided classpath
			List<File> providedJarFiles = new ArrayList<>();
			collectJarFiles(provided, true, providedJarFiles);
			// check if at least one JAR file has been found
			if (providedJarFiles.isEmpty()) {
				String errorMessage = "No *.jar files found in provided classpath.";
				handleError(-4, errorMessage);
			}
			options.addProvidedJarFiles(providedJarFiles);
		}

		if (!runtime.isEmpty()) {
			// collect JAR files for runtime classpath
			List<File> runtimeJarFiles = new ArrayList<>();
			collectJarFiles(runtime, true, runtimeJarFiles);
			// check if at least one JAR file has been found
			if (runtimeJarFiles.isEmpty()) {
				String errorMessage = "No *.jar files found in runtime classpath.";
				handleError(-4, errorMessage);
			}
			options.addRuntimeJarFiles(runtimeJarFiles);
		}

		return options;
	}

	private void addFiles(String values, List<File> classpath) {
		for (String value : values.split(",")) {
			File path = new File(value);
			classpath.add(path);
		}
	}

	private void collectJarFiles(List<File> paths, boolean strict, List<File> jarFiles) throws CommandLineException {

		for (File path : paths) {
			if (path.isFile()) {
				// if file is a *.jar file ...
				String fileName = path.getName().toLowerCase();
				if (fileName.endsWith(".jar")) {
					jarFiles.add(path);
				} else if (fileName.endsWith(".war")) {
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
