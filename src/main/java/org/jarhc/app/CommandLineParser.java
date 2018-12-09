package org.jarhc.app;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class CommandLineParser {

	private final PrintStream err;

	public CommandLineParser(PrintStream err) {
		if (err == null) throw new IllegalArgumentException("err");
		this.err = err;
	}

	Options parse(String[] args) {

		if (args.length < 1) {
			printUsage("Argument <path> is missing.");
			return new Options(-1);
		}

		// map arguments to files
		File[] paths = Stream.of(args).map(File::new).toArray(File[]::new);

		// collect JAR files
		List<File> files = new ArrayList<>();
		int exitCode = collectJarFiles(paths, true, files);
		if (exitCode != 0) {
			return new Options(exitCode);
		}

		// check if at least one JAR file has been found
		if (files.isEmpty()) {
			printUsage("No *.jar files found in path.");
			return new Options(-4);
		}

		// exit code 0 -> no errors
		return new Options(files);
	}

	private int collectJarFiles(File[] paths, boolean strict, List<File> files) {

		for (File path : paths) {
			if (path.isFile()) {
				// if file is a *.jar file ...
				String fileName = path.getName().toLowerCase();
				if (fileName.endsWith(".jar")) {
					files.add(path);
				} else if (strict) {
					String errorMessage = String.format("File is not a *.jar file: %s", path.getAbsolutePath());
					printUsage(errorMessage);
					return -2;
				}
			} else if (path.isDirectory()) {
				File[] array = path.listFiles();
				if (array == null) continue;
				collectJarFiles(array, false, files);
			} else if (strict) {
				String errorMessage = String.format("File or directory not found: %s", path.getAbsolutePath());
				printUsage(errorMessage);
				return -3;
			}
		}
		return 0;
	}

	private void printUsage(String errorMessage) {
		if (errorMessage != null) {
			err.println(errorMessage);
		}
		err.println("Usage: java -jar JarHC.jar <path> [<path>]*");
		err.println("   <path>: Path to JAR file or directory with JAR files.");
	}

}
