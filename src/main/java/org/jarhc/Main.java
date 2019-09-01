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
import java.time.Duration;
import org.jarhc.app.Application;
import org.jarhc.app.CommandLineException;
import org.jarhc.app.CommandLineParser;
import org.jarhc.app.Options;
import org.jarhc.artifacts.CachedRepository;
import org.jarhc.artifacts.MavenCentralRepository;
import org.jarhc.artifacts.MavenLocalRepository;
import org.jarhc.artifacts.Repository;

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

		Repository repository = createRepository(options);

		// create and run application
		Application application = new Application();
		application.setRepository(repository);

		int exitCode = application.run(options);

		if (exitCode != 0) {
			System.exit(exitCode);
		}
	}

	private static Repository createRepository(Options options) {

		// resolve artifacts using Maven Central
		Duration timeout = Duration.ofSeconds(5); // TODO: make this configurable
		Repository repository = new MavenCentralRepository(timeout);

		// if a local Maven repository is present ...
		String userHome = System.getProperty("user.home");
		File directory = new File(userHome, ".m2/repository");
		if (directory.isDirectory()) {
			// use local Maven repository
			repository = new MavenLocalRepository(directory, repository);
		}

		String dataPath = options.getDataPath();
		if (dataPath != null) {

			// use a local disk cache
			File cacheDir = new File(dataPath, "cache/repository");
			repository = new CachedRepository(cacheDir, repository);

		}

		return repository;

	}

}