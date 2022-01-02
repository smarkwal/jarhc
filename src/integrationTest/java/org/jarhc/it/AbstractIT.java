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

package org.jarhc.it;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.jarhc.TestUtils;
import org.jarhc.analyzer.Analysis;
import org.jarhc.analyzer.Analyzer;
import org.jarhc.analyzer.AnalyzerRegistry;
import org.jarhc.app.Options;
import org.jarhc.artifacts.Artifact;
import org.jarhc.artifacts.MavenRepository;
import org.jarhc.artifacts.Repository;
import org.jarhc.env.JavaRuntime;
import org.jarhc.inject.Injector;
import org.jarhc.loader.ClasspathLoader;
import org.jarhc.loader.LoaderBuilder;
import org.jarhc.model.Classpath;
import org.jarhc.report.Report;
import org.jarhc.report.ReportFormat;
import org.jarhc.report.text.TextReportFormat;
import org.jarhc.test.JavaRuntimeMock;
import org.jarhc.test.TextUtils;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract class AbstractIT {

	private final String[] artifacts;

	AbstractIT(String... artifacts) {
		this.artifacts = artifacts;
	}

	@TestFactory
	Collection<DynamicTest> testAnalyzers(@TempDir Path tempDir) throws IOException {

		// prepare list of files
		List<File> files = new ArrayList<>();
		for (String coordinates : artifacts) {
			Artifact artifact = new Artifact(coordinates);
			String resourcePath = "/repository/" + artifact.getPath();
			File file = TestUtils.getResourceAsFile(resourcePath, tempDir);
			files.add(file);
		}

		// prepare context
		Options options = new Options();
		JavaRuntime javaRuntime = JavaRuntimeMock.getOracleRuntime();
		Logger logger = LoggerFactory.getLogger(MavenRepository.class);
		MavenRepository repository = new MavenRepository(tempDir.toString(), logger); // TODO: use a 100% local Maven repo

		// prepare an injector
		Injector injector = new Injector();
		injector.addBinding(Options.class, options);
		injector.addBinding(JavaRuntime.class, javaRuntime);
		injector.addBinding(Repository.class, repository);

		// load classpath
		ClasspathLoader classpathLoader = LoaderBuilder.create()
				.withParentClassLoader(javaRuntime)
				.withRepository(repository)
				.buildClasspathLoader();
		Classpath classpath = classpathLoader.load(files);

		List<DynamicTest> tests = new ArrayList<>();
		AnalyzerRegistry registry = new AnalyzerRegistry(injector);
		for (String code : registry.getCodes()) {
			Analyzer analyzer = registry.createAnalyzer(code);
			String analyzerName = analyzer.getClass().getSimpleName().replace("Analyzer", "");
			tests.add(DynamicTest.dynamicTest(analyzerName, () -> test(classpath, analyzer, analyzerName)));
		}
		return tests;
	}

	private void test(Classpath classpath, Analyzer analyzer, String analyzerName) throws IOException {

		// prepare
		ReportFormat reportFormat = new TextReportFormat();

		// analyze classpath
		Report report = new Report();
		Analysis analysis = new Analysis(analyzer);
		analysis.run(classpath, report);

		// create report
		String output = reportFormat.format(report);

		if (TestUtils.createResources()) {
			TestUtils.saveResource("integrationTest", getReportResourcePath(analyzerName), output, "UTF-8");
			return;
		}

		// normalize
		output = TextUtils.toUnixLineSeparators(output);
		String expectedOutput = TestUtils.getResourceAsString(getReportResourcePath(analyzerName), "UTF-8");
		expectedOutput = TextUtils.toUnixLineSeparators(expectedOutput);

		// assert
		assertEquals(expectedOutput, output);
	}

	private String getReportResourcePath(String analyzerName) {
		return "/org/jarhc/it/" + this.getClass().getSimpleName() + "/report-" + analyzerName + ".txt";
	}

}
