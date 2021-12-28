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

package org.jarhc;

import java.io.File;
import org.jarhc.app.Application;
import org.jarhc.app.CommandLineException;
import org.jarhc.app.CommandLineParser;
import org.jarhc.app.Options;
import org.jarhc.artifacts.MavenRepository;
import org.jarhc.artifacts.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

	public static void main(String[] args) {

		// parse command line
		Options options = null;
		try {
			CommandLineParser commandLineParser = new CommandLineParser(System.out, System.err);
			options = commandLineParser.parse(args);
		} catch (CommandLineException e) {
			// note: error message has already been printed

			// return with exit code
			int exitCode = e.getExitCode();
			System.exit(exitCode);
		}

		setupLogging(options);

		Repository repository = createRepository(options);

		// create and run application
		Logger logger = LoggerFactory.getLogger(Application.class);
		Application application = new Application(logger);
		application.setRepository(repository);

		int exitCode = application.run(options);

		if (exitCode != 0) {
			System.exit(exitCode);
		}
	}

	private static void setupLogging(Options options) {

		if (options.isDebug()) {
			// enable debug log output
			System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "debug");
		}

	}

	private static Repository createRepository(Options options) {

		String dataPath = options.getDataPath();
		if (dataPath == null) {

			String userHome = System.getProperty("user.home");
			if (userHome == null) {
				throw new IllegalArgumentException("User home not defined.");
			}

			File directory = new File(userHome);
			if (!directory.isDirectory()) {
				throw new IllegalArgumentException("User home not found: " + directory.getAbsolutePath());
			}

			directory = new File(directory, ".jarhc");
			if (!directory.isDirectory()) {
				boolean created = directory.mkdirs();
				if (!created) {
					throw new IllegalArgumentException("Failed to create directory: " + directory.getAbsolutePath());
				}
			}

			dataPath = directory.getAbsolutePath();
		}

		Logger logger = LoggerFactory.getLogger(MavenRepository.class);
		return new MavenRepository(dataPath, logger);
	}

}