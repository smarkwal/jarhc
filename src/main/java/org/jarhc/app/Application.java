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

package org.jarhc.app;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import org.jarhc.analyzer.Analysis;
import org.jarhc.analyzer.Analyzer;
import org.jarhc.analyzer.AnalyzerDescription;
import org.jarhc.analyzer.AnalyzerRegistry;
import org.jarhc.artifacts.Artifact;
import org.jarhc.artifacts.Repository;
import org.jarhc.env.ClasspathJavaRuntime;
import org.jarhc.env.DefaultJavaRuntime;
import org.jarhc.env.JavaRuntime;
import org.jarhc.inject.Injector;
import org.jarhc.java.ClassLoader;
import org.jarhc.loader.ClasspathLoader;
import org.jarhc.loader.FileNameNormalizer;
import org.jarhc.loader.LoaderBuilder;
import org.jarhc.model.Classpath;
import org.jarhc.report.Report;
import org.jarhc.report.ReportFormat;
import org.jarhc.report.ReportFormatFactory;
import org.jarhc.report.ReportSection;
import org.jarhc.report.writer.ReportWriter;
import org.jarhc.report.writer.impl.FileReportWriter;
import org.jarhc.report.writer.impl.StreamReportWriter;
import org.jarhc.utils.StringUtils;
import org.jarhc.utils.VersionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Application {

	private PrintStream out = System.out;
	private Repository repository;
	private Supplier<JavaRuntime> javaRuntimeFactory;
	private final Logger logger;

	public Application(Logger logger) {
		this.logger = logger;
		this.javaRuntimeFactory = () -> {
			Logger defaultJavaRuntimeLogger = LoggerFactory.getLogger(DefaultJavaRuntime.class);
			return new DefaultJavaRuntime(defaultJavaRuntimeLogger);
		};
	}

	public void setOut(PrintStream out) {
		this.out = out;
	}

	public void setRepository(Repository repository) {
		this.repository = repository;
	}

	public void setJavaRuntimeFactory(Supplier<JavaRuntime> javaRuntimeFactory) {
		this.javaRuntimeFactory = javaRuntimeFactory;
	}

	public int run(Options options) {

		String version = VersionUtils.getVersion();
		out.println("JarHC - JAR Health Check " + version);
		out.println("=========================" + StringUtils.repeat("=", version.length()));
		out.println();

		long time = System.nanoTime();

		// prepare a new report
		Report report = new Report();

		List<String> runtimeJarPaths = options.getRuntimeJarPaths();
		List<String> providedJarPaths = options.getProvidedJarPaths();
		List<String> classpathJarPaths = options.getClasspathJarPaths();

		out.println("Load JAR files ...");

		List<JarSource> runtimeJarSources = loadJarSources(runtimeJarPaths);
		List<JarSource> providedJarSources = loadJarSources(providedJarPaths);
		List<JarSource> classpathJarSources = loadJarSources(classpathJarPaths);

		out.println("Scan JAR files ...");

		JavaRuntime javaRuntime = createJavaRuntime(options, runtimeJarSources);
		ClassLoader parentClassLoader = createClassLoader(options, javaRuntime, providedJarSources);
		Classpath classpath = createClasspath(options, classpathJarSources, parentClassLoader);

		out.println("Analyze classpath ...");

		// prepare an injector
		Injector injector = new Injector();
		injector.addBinding(Options.class, options);
		injector.addBinding(JavaRuntime.class, javaRuntime);
		injector.addBinding(Repository.class, repository);

		// get a new analyzer registry/factory
		AnalyzerRegistry registry = new AnalyzerRegistry(injector);

		List<String> sections = options.getSections();
		if (sections == null || sections.isEmpty()) {
			sections = registry.getCodes();
		}

		// create analyzers based on selected sections
		List<Analyzer> analyzers = new ArrayList<>(sections.size());
		for (String section : sections) {
			AnalyzerDescription description = registry.getDescription(section);
			if (description == null) {
				logger.error("Analyzer not found: {}", section);
				return 3;
			}
			Analyzer analyzer = registry.createAnalyzer(section);
			analyzers.add(analyzer);
		}

		// prepare a new analysis
		Analysis analysis = new Analysis(analyzers.toArray(new Analyzer[0]));

		// run analysis
		analysis.run(classpath, report);

		if (logger.isDebugEnabled()) {
			time = System.nanoTime() - time;
			logger.debug("Time: {} ms", time / 1000 / 1000);
		}

		out.println("Create report ...");
		out.println();

		if (options.isSkipEmpty()) {
			// remove empty report sections.
			for (ReportSection section : report.getSections()) {
				if (section.isEmpty()) {
					report.removeSection(section);
				}
			}
		}

		// set report title
		report.setTitle(options.getReportTitle());

		// create report format
		ReportFormat format = createReportFormat(options, injector);

		// create report writer
		try (ReportWriter writer = createReportWriter(options)) {

			// print report
			format.format(report, writer);

		} catch (IOException e) {
			logger.error("I/O error while writing report.", e);
			return 2; // TODO: exit code?
		}

		return 0;
	}

	private List<JarSource> loadJarSources(List<String> paths) {
		List<JarSource> sources = new ArrayList<>();
		for (String path : paths) {
			if (Artifact.validateCoordinates(path)) {
				ArtifactSource source = new ArtifactSource(path, repository);
				sources.add(source);
			} else {
				File file = new File(path);
				if (file.isFile()) {
					FileSource source = new FileSource(file);
					sources.add(source);
				} else if (file.isDirectory()) {
					List<File> jarFiles = CommandLineParser.findJarFiles(file);
					for (File jarFile : jarFiles) {
						FileSource source = new FileSource(jarFile);
						sources.add(source);
					}
				}
			}
		}
		return sources;
	}

	private Classpath createClasspath(Options options, List<JarSource> classpathJarFiles, ClassLoader parentClassLoader) {
		// load classpath JAR files
		FileNameNormalizer fileNameNormalizer = createFileNameNormalizer(options);
		ClasspathLoader loader = LoaderBuilder.create()
				.forRelease(options.getRelease())
				.withFileNameNormalizer(fileNameNormalizer)
				.withParentClassLoader(parentClassLoader)
				.withClassLoaderStrategy(options.getClassLoaderStrategy())
				.withRepository(repository)
				.buildClasspathLoader();
		return loader.load(classpathJarFiles);
	}

	private FileNameNormalizer createFileNameNormalizer(Options options) {
		Logger fileNameNormalizerLogger = LoggerFactory.getLogger(FileNameNormalizer.class);
		return new FileNameNormalizer(options, repository, fileNameNormalizerLogger);
	}

	private JavaRuntime createJavaRuntime(Options options, List<JarSource> runtimeJarFiles) {

		if (runtimeJarFiles.isEmpty()) {
			// create default Java runtime
			return javaRuntimeFactory.get();
		}

		// load runtime classpath JAR files
		ClasspathLoader loader = LoaderBuilder.create()
				.forClassLoader("Runtime")
				.forRelease(options.getRelease())
				.scanForReferences(false)
				.buildClasspathLoader();
		Classpath runtimeClasspath = loader.load(runtimeJarFiles);

		// create Java runtime based on classpath
		return new ClasspathJavaRuntime(runtimeClasspath);
	}

	private ClassLoader createClassLoader(Options options, JavaRuntime javaRuntime, List<JarSource> providedJarFiles) {

		if (providedJarFiles.isEmpty()) {
			// use original Java runtime
			return javaRuntime;
		}

		// load provided classpath JAR files
		ClasspathLoader loader = LoaderBuilder.create()
				.forClassLoader("Provided")
				.forRelease(options.getRelease())
				.scanForReferences(false)
				.withParentClassLoader(javaRuntime)
				.withClassLoaderStrategy(options.getClassLoaderStrategy())
				.withRepository(repository)
				.buildClasspathLoader();
		return loader.load(providedJarFiles);
	}

	private ReportFormat createReportFormat(Options options, Injector injector) {
		String format = options.getReportFormat();
		ReportFormatFactory factory = new ReportFormatFactory(injector); // TODO: inject dependency
		return factory.getReportFormat(format);
	}

	// TODO: move into a factory class and inject as dependency?
	private ReportWriter createReportWriter(Options options) {
		String path = options.getReportFile();
		if (path == null) {
			return new StreamReportWriter(this.out);
		} else {
			File file = new File(path);
			return new FileReportWriter(file);
		}
	}

}
