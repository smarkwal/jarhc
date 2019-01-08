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

import org.jarhc.app.Application;
import org.jarhc.app.CommandLineException;
import org.jarhc.app.CommandLineParser;
import org.jarhc.app.Options;
import org.jarhc.artifacts.CachedResolver;
import org.jarhc.artifacts.MavenCentralResolver;
import org.jarhc.artifacts.Resolver;

import java.io.File;
import java.time.Duration;

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

		Resolver resolver = createResolver();

		// create and run application
		Application application = new Application();
		application.setResolver(resolver);

		int exitCode = application.run(options);

		if (exitCode != 0) {
			System.exit(exitCode);
		}
	}

	private static Resolver createResolver() {

		// resolve artifacts using Maven Central and a local disk cache
		Duration timeout = Duration.ofSeconds(5);
		Resolver mavenResolver = new MavenCentralResolver(timeout);
		File cacheDir = new File("./.jarhc/cache/resolver"); // TODO: make this configurable
		return new CachedResolver(cacheDir, mavenResolver);

	}

}