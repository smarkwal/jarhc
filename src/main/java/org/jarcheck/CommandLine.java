package org.jarcheck;

import java.io.File;

class CommandLine {

	private final String[] args;
	private Options options = null;

	CommandLine(String[] args) {
		if (args == null) throw new IllegalArgumentException("args");
		this.args = args;
	}

	int parse() {

		if (args.length < 1) {
			printUsage("Argument <path> is missing.");
			return -1;
		} else if (args.length > 1) {
			printUsage("Too many arguments.");
			return -1;
		}

		String path = args[0];
		File directory = new File(path);
		if (!directory.isDirectory()) {
			String errorMessage = String.format("Directory not found: %s", directory.getAbsolutePath());
			printUsage(errorMessage);
			return 1;
		}

		this.options = new Options(directory);

		// exit code 0 -> no errors
		return 0;
	}

	Options getOptions() {
		return options;
	}

	private static void printUsage(String errorMessage) {
		if (errorMessage != null) {
			System.err.println(errorMessage);
		}
		System.err.println("Usage: java -jar JarCheck.jar <path>");
		System.err.println("   <path>: Path to directory with JAR files.");
	}

}
