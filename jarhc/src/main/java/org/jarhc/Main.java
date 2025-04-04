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
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import org.jarhc.app.Application;
import org.jarhc.app.CollectionManager;
import org.jarhc.app.CollectionManagerImpl;
import org.jarhc.app.CommandLineException;
import org.jarhc.app.CommandLineParser;
import org.jarhc.app.Diff;
import org.jarhc.app.Options;
import org.jarhc.app.Options.Command;
import org.jarhc.app.PropertiesManager;
import org.jarhc.app.PropertiesManagerImpl;
import org.jarhc.artifacts.ArtifactFinder;
import org.jarhc.artifacts.MavenArtifactFinder;
import org.jarhc.artifacts.MavenRepository;
import org.jarhc.artifacts.Repository;
import org.jarhc.utils.FileUtils;
import org.jarhc.utils.JarHcException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

public class Main {

	private static final String TEMPDIR_PREFIX = "JarHC-Data-TEMP-";

	private static Logger LOGGER;

	public static void main(String[] args) {

		// load properties
		PropertiesManager propertiesManager = new PropertiesManagerImpl();
		Properties properties = propertiesManager.loadProperties();

		// prepare collection manager
		CollectionManager collectionManager = new CollectionManagerImpl(properties);

		// parse command line
		Options options = null;
		try {
			CommandLineParser commandLineParser = new CommandLineParser(System.out, System.err, properties, collectionManager);
			options = commandLineParser.parse(args);
		} catch (CommandLineException e) {
			// note: error message has already been printed

			// return with exit code
			int exitCode = e.getExitCode();
			System.exit(exitCode);
		}

		setupLogging(options);

		int exitCode;
		Command command = options.getCommand();
		if (command == Command.SCAN) {

			// prepare Maven repository
			Repository repository = createRepository(options);

			// create and run application
			Logger logger = LoggerFactory.getLogger(Application.class);
			Application application = new Application(logger);
			application.setRepository(repository);
			exitCode = application.run(options);

		} else if (command == Command.DIFF) {

			// create and run diff
			Diff diff = new Diff();
			exitCode = diff.run(options);

		} else {
			throw new JarHcException("Unsupported command: " + command);
		}

		// perform clean-up operations
		cleanUp(options);

		if (exitCode != 0) {
			System.exit(exitCode);
		}
	}

	private static void setupLogging(Options options) {

		if (options.isTrace()) {

			// enable trace log output
			System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "trace");

		} else if (options.isDebug()) {

			// enable debug log output
			System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "debug");

			// do NOT enable debug output for Apache HttpClient special loggers
			// 'org.apache.http.headers' and 'org.apache.http.wire'
			System.setProperty("org.slf4j.simpleLogger.log.org.apache.http.headers", "info");
			System.setProperty("org.slf4j.simpleLogger.log.org.apache.http.wire", "info");
		}

		// install bridge from JUL to SFL4J and reset level on root logger
		SLF4JBridgeHandler.removeHandlersForRootLogger();
		SLF4JBridgeHandler.install();
		java.util.logging.Logger.getLogger("").setLevel(Level.FINEST);

		// create logger AFTER setup of logging
		LOGGER = LoggerFactory.getLogger(Main.class);

		boolean loggingTestEnabled = System.getProperties().containsKey("jarhc.logging.test.enabled");
		if (loggingTestEnabled) {

			// test logging through SLF4J
			LOGGER.error("Test logging: SLF4J ERROR");
			LOGGER.warn("Test logging: SLF4J WARN");
			LOGGER.info("Test logging: SLF4J INFO");
			LOGGER.debug("Test logging: SLF4J DEBUG");
			LOGGER.trace("Test logging: SLF4J TRACE");

			// test logging through JUL (java.util.logging)
			java.util.logging.Logger julLogger = java.util.logging.Logger.getLogger(Main.class.getName());
			julLogger.severe("Test logging: JUL SEVERE");
			julLogger.warning("Test logging: JUL WARNING");
			julLogger.info("Test logging: JUL INFO");
			julLogger.config("Test logging: JUL CONFIG");
			julLogger.fine("Test logging: JUL FINE");
			julLogger.finer("Test logging: JUL FINER");
			julLogger.finest("Test logging: JUL FINEST");

			// test logging through Commons Logging
			org.apache.commons.logging.Log commonsLogger = org.apache.commons.logging.LogFactory.getLog(Main.class);
			commonsLogger.fatal("Test logging: Commons Logging FATAL");
			commonsLogger.error("Test logging: Commons Logging ERROR");
			commonsLogger.warn("Test logging: Commons Logging WARN");
			commonsLogger.info("Test logging: Commons Logging INFO");
			commonsLogger.debug("Test logging: Commons Logging DEBUG");
			commonsLogger.trace("Test logging: Commons Logging TRACE");
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

		File cacheDirTagFile = new File(directory, "CACHEDIR.TAG");
		if (!cacheDirTagFile.isFile()) {
			try {
				FileUtils.writeStringToFile("Signature: 8a477f597d28d172789f06886806bc55\n# This file is a cache directory tag created by JarHC.", cacheDirTagFile);
			} catch (IOException e) {
				LOGGER.warn("Failed to create CACHEDIR.TAG file.", e);
			}
		}

		File cacheDir = new File(dataPath, "checksums");
		Logger mavenArtifactFinderLogger = LoggerFactory.getLogger(MavenArtifactFinder.class);
		ArtifactFinder artifactFinder = new MavenArtifactFinder(cacheDir, mavenArtifactFinderLogger);

		int javaVersion = options.getRelease();
		Logger mavenRepositoryLogger = LoggerFactory.getLogger(MavenRepository.class);
		return new MavenRepository(javaVersion, options, dataPath, artifactFinder, mavenRepositoryLogger);
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

		// if a data directory has been specified,
		// and it is a temporary directory ...
		String dataPath = options.getDataPath();
		if (dataPath != null && dataPath.contains(TEMPDIR_PREFIX)) {

			// delete data directory (recursively)
			File directory = new File(dataPath);
			FileUtils.delete(directory);
		}
	}

}