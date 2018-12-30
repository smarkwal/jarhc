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
import org.jarhc.app.CommandLineParser;
import org.jarhc.artifacts.CachedResolver;
import org.jarhc.artifacts.MavenCentralResolver;
import org.jarhc.artifacts.Resolver;
import org.jarhc.env.DefaultJavaRuntime;
import org.jarhc.env.JavaRuntime;

import java.io.File;
import java.time.Duration;

public class Main {

	public static void main(String[] args) {
		CommandLineParser commandLineParser = new CommandLineParser(System.err);

		// use default Java runtime (the one used to run JarHC)
		JavaRuntime javaRuntime = new DefaultJavaRuntime();

		// resolve artifacts using Maven Central and a local disk cache
		Duration timeout = Duration.ofSeconds(5);
		Resolver mavenResolver = new MavenCentralResolver(timeout);
		File cacheDir = new File("./.jarhc/cache/resolver"); // TODO: make thisc onfigurable
		Resolver cachedResolver = new CachedResolver(cacheDir, mavenResolver);

		// prepare context
		Context context = new Context(javaRuntime, cachedResolver);

		// create and run application
		Application application = new Application(commandLineParser, context, System.out, System.err);
		int exitCode = application.run(args);

		if (exitCode != 0) {
			System.exit(exitCode);
		}
	}

}