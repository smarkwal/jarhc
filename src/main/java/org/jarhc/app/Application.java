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

import org.jarhc.Context;
import org.jarhc.analyzer.Analysis;
import org.jarhc.analyzer.Analyzer;
import org.jarhc.analyzer.AnalyzerDescription;
import org.jarhc.analyzer.AnalyzerRegistry;
import org.jarhc.artifacts.Resolver;
import org.jarhc.env.ClasspathRuntime;
import org.jarhc.env.DefaultJavaRuntime;
import org.jarhc.env.ExtendedRuntime;
import org.jarhc.env.JavaRuntime;
import org.jarhc.loader.ClasspathLoader;
import org.jarhc.loader.JarFileNameNormalizer;
import org.jarhc.loader.LoaderBuilder;
import org.jarhc.model.Classpath;
import org.jarhc.report.Report;
import org.jarhc.report.ReportFormat;
import org.jarhc.report.ReportFormatFactory;
import org.jarhc.report.writer.ReportWriter;
import org.jarhc.report.writer.impl.FileReportWriter;
import org.jarhc.report.writer.impl.StreamReportWriter;
import org.jarhc.utils.StringUtils;
import org.jarhc.utils.VersionUtils;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class Application {

	private PrintStream out = System.out;
	private PrintStream err = System.err;
	private Resolver resolver;
	private Supplier<JavaRuntime> javaRuntimeFactory = DefaultJavaRuntime::new;

	public Application() {
	}

	public void setOut(PrintStream out) {
		this.out = out;
	}

	public void setErr(PrintStream err) {
		this.err = err;
	}

	public void setResolver(Resolver resolver) {
		this.resolver = resolver;
	}

	public void setJavaRuntimeFactory(Supplier<JavaRuntime> javaRuntimeFactory) {
		this.javaRuntimeFactory = javaRuntimeFactory;
	}

	public int run(Options options) {

		String version = VersionUtils.getVersion();
		out.println("JarHC - JAR Health Check " + version);
		out.println("=========================" + StringUtils.repeat("=", version.length()));
		out.println();

		// long time = System.nanoTime();

		out.println("Scan JAR files ...");

		List<File> classpathJarFiles = options.getClasspathJarFiles();
		Classpath classpath = createClasspath(options, classpathJarFiles);

		List<File> runtimeJarFiles = options.getRuntimeJarFiles();
		JavaRuntime javaRuntime = createJavaRuntime(runtimeJarFiles);

		List<File> providedJarFiles = options.getProvidedJarFiles();
		javaRuntime = wrapJavaRuntime(javaRuntime, providedJarFiles);

		out.println("Analyze classpath ...");

		AnalyzerRegistry registry = new AnalyzerRegistry();
		Context context = new Context(javaRuntime, resolver);

		List<String> sections = options.getSections();
		if (sections == null || sections.isEmpty()) {
			sections = registry.getCodes();
		}

		List<Analyzer> analyzers = new ArrayList<>(sections.size());
		for (String section : sections) {
			AnalyzerDescription description = registry.getDescription(section);
			if (description == null) {
				System.err.println("Analyzer not found: " + section);
				return 3;
			}
			Analyzer analyzer = registry.createAnalyzer(section, context);
			analyzers.add(analyzer);
		}

		Analysis analysis = new Analysis(analyzers.toArray(new Analyzer[0]));

		Report report = analysis.run(classpath);

		// time = System.nanoTime() - time;
		// System.out.println("Time: " + (time / 1000 / 1000) + " ms");

		out.println("Create report ...");
		out.println();

		// set report title
		report.setTitle(options.getReportTitle());

		// create report format
		ReportFormat format = createReportFormat(options);

		// create report writer
		try (ReportWriter writer = createReportWriter(options)) {

			// print report
			format.format(report, writer);

		} catch (IOException e) {
			e.printStackTrace(err);
			return 2; // TODO: exit code?
		}

		return 0;
	}

	private Classpath createClasspath(Options options, List<File> classpathJarFiles) {
		// load classpath JAR files
		JarFileNameNormalizer jarFileNameNormalizer = createJarFileNameNormalizer(options);
		ClasspathLoader loader = LoaderBuilder.create().withJarFileNameNormalizer(jarFileNameNormalizer).buildClasspathLoader();
		return loader.load(classpathJarFiles);
	}

	private JarFileNameNormalizer createJarFileNameNormalizer(Options options) {
		boolean useArtifactName = options.isUseArtifactName();
		boolean removeVersion = options.isRemoveVersion();
		if (useArtifactName) {
			return (fileName, checksum) -> JarFileNameNormalizer.getArtifactFileName(checksum, resolver, removeVersion, fileName);
		} else if (removeVersion) {
			return (fileName, checksum) -> JarFileNameNormalizer.getFileNameWithoutVersionNumber(fileName);
		} else {
			return null;
		}
	}

	private JavaRuntime createJavaRuntime(List<File> runtimeJarFiles) {

		if (runtimeJarFiles.isEmpty()) {
			// create default Java runtime
			return javaRuntimeFactory.get();
		}

		// load runtime classpath JAR files
		ClasspathLoader loader = LoaderBuilder.create().forClassLoader("Runtime").scanForReferences(false).buildClasspathLoader();
		Classpath runtimeClasspath = loader.load(runtimeJarFiles);

		// create Java runtime based on classpath
		return new ClasspathRuntime(runtimeClasspath);
	}

	private JavaRuntime wrapJavaRuntime(JavaRuntime javaRuntime, List<File> providedJarFiles) {

		if (providedJarFiles.isEmpty()) {
			// use original Java runtime
			return javaRuntime;
		}

		// load provided classpath JAR files
		ClasspathLoader loader = LoaderBuilder.create().forClassLoader("Provided").scanForReferences(false).buildClasspathLoader();
		Classpath providedClasspath = loader.load(providedJarFiles);

		// wrap Java runtime
		return new ExtendedRuntime(providedClasspath, javaRuntime);
	}

	private ReportFormat createReportFormat(Options options) {
		String format = options.getReportFormat();
		ReportFormatFactory factory = new ReportFormatFactory(); // TODO: inject dependency
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
