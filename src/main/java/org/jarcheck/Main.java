package org.jarcheck;

import org.jarcheck.app.Application;
import org.jarcheck.app.CommandLineParser;

public class Main {

	public static void main(String[] args) {
		CommandLineParser commandLineParser = new CommandLineParser(System.err);
		Application application = new Application(commandLineParser, System.out, System.err);
		int exitCode = application.run(args);
		System.exit(exitCode);
	}

}