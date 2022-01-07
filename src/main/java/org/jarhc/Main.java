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

import static org.jarhc.artifacts.MavenRepository.MAVEN_CENTRAL_URL;

import java.io.File;
import org.jarhc.app.Application;
import org.jarhc.app.CommandLineException;
import org.jarhc.app.CommandLineParser;
import org.jarhc.app.Options;
import org.jarhc.artifacts.ArtifactFinder;
import org.jarhc.artifacts.MavenArtifactFinder;
import org.jarhc.artifacts.MavenRepository;
import org.jarhc.artifacts.Repository;
import org.jarhc.utils.FileUtils;
import org.jarhc.utils.JarHcException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

	private static final String TEMPDIR_PREFIX = "JarHC-Data-TEMP-";

	private static Logger LOGGER;

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

		// create logger AFTER setup of logging
		LOGGER = LoggerFactory.getLogger(Main.class);

		Repository repository = createRepository(options);

		// create and run application
		Logger logger = LoggerFactory.getLogger(Application.class);
		Application application = new Application(logger);
		application.setRepository(repository);

		int exitCode = application.run(options);

		// perform clean-up operations
		cleanUp(options);

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

		String dataPath = findDataPath(options);

		// write absolute data path back into options
		options.setDataPath(dataPath);

		File directory = new File(dataPath);
		if (!directory.isDirectory()) {
			boolean created = directory.mkdirs();
			if (!created) {
				throw new JarHcException("Failed to create directory: " + directory.getAbsolutePath());
			}
		}

		Logger mavenArtifactFinderLogger = LoggerFactory.getLogger(MavenArtifactFinder.class);
		ArtifactFinder artifactFinder = new MavenArtifactFinder(mavenArtifactFinderLogger);

		int javaVersion = options.getRelease();
		Logger mavenRepositoryLogger = LoggerFactory.getLogger(MavenRepository.class);
		return new MavenRepository(javaVersion, MAVEN_CENTRAL_URL, dataPath, artifactFinder, mavenRepositoryLogger);
	}

	private static String findDataPath(Options options) {

		// priority 1: command line option --data
		String dataPath = options.getDataPath();
		if (dataPath != null) {

			// special handling for data option "TEMP"
			if (dataPath.equals("TEMP")) {
				dataPath = FileUtils.createTempDirectory(TEMPDIR_PREFIX);
				LOGGER.debug("Data directory: {} (temporary)", dataPath);
				return dataPath;
			}

			LOGGER.debug("Data directory: {} (command line option)", dataPath);
			return dataPath;
		}

		// priority 2: environment variable $JARHC_DATA
		dataPath = System.getenv("JARHC_DATA");
		if (dataPath != null) {
			LOGGER.debug("Data directory: {} (environment variable)", dataPath);
			return dataPath;
		}

		// priority 3: user home
		dataPath = getUserHomePath();
		LOGGER.debug("Data directory: {} (user home)", dataPath);

		return dataPath;
	}

	private static String getUserHomePath() {

		String userHome = System.getProperty("user.home");
		if (userHome == null) {
			throw new JarHcException("User home not defined.");
		}

		File directory = new File(userHome);
		if (!directory.isDirectory()) {
			throw new JarHcException("User home not found: " + directory.getAbsolutePath());
		}

		directory = new File(directory, ".jarhc");

		return directory.getAbsolutePath();
	}

	private static void cleanUp(Options options) {

		// if data directory is a temporary directory ...
		String dataPath = options.getDataPath();
		if (dataPath.contains(TEMPDIR_PREFIX)) {

			// delete data directory (recursively)
			File directory = new File(dataPath);
			FileUtils.delete(directory);
		}
	}

}