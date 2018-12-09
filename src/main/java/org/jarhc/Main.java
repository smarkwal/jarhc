package org.jarhc;

import org.jarhc.app.Application;
import org.jarhc.app.CommandLineParser;

public class Main {

	public static void main(String[] args) {
		CommandLineParser commandLineParser = new CommandLineParser(System.err);
		Application application = new Application(commandLineParser, System.out, System.err);
		int exitCode = application.run(args);
		System.exit(exitCode);
	}

}