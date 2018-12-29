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

import org.jarhc.Context;
import org.jarhc.TestUtils;
import org.jarhc.analyzer.Analysis;
import org.jarhc.analyzer.Analyzer;
import org.jarhc.analyzer.AnalyzerRegistry;
import org.jarhc.loader.ClasspathLoader;
import org.jarhc.model.Classpath;
import org.jarhc.report.Report;
import org.jarhc.report.ReportFormat;
import org.jarhc.report.html.HtmlReportFormat;
import org.jarhc.report.text.TextReportFormat;
import org.jarhc.test.ContextMock;
import org.jarhc.test.TextUtils;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junitpioneer.jupiter.TempDirectory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junitpioneer.jupiter.TempDirectory.TempDir;

@ExtendWith(TempDirectory.class)
abstract class AbstractIT {

	private final String baseResourcePath;
	private final String[] fileNames;

	AbstractIT(String baseResourcePath, String[] fileNames) {
		this.baseResourcePath = baseResourcePath;
		this.fileNames = fileNames;
	}

	@TestFactory
	Collection<DynamicTest> testAnalyzers(@TempDir Path tempDir) throws IOException {

		// prepare list of files
		List<File> files = new ArrayList<>();
		for (String fileName : fileNames) {
			String resourcePath = baseResourcePath + fileName;
			File file = TestUtils.getResourceAsFile(resourcePath, tempDir);
			files.add(file);
		}

		// prepare context
		Context context = ContextMock.createContext();

		// load classpath
		ClasspathLoader classpathLoader = new ClasspathLoader();
		Classpath classpath = classpathLoader.load(files);

		List<DynamicTest> tests = new ArrayList<>();
		AnalyzerRegistry registry = new AnalyzerRegistry();
		for (String code : registry.getCodes()) {
			Analyzer analyzer = registry.createAnalyzer(code, context);
			String analyzerName = analyzer.getClass().getSimpleName().replace("Analyzer", "");
			tests.add(DynamicTest.dynamicTest(analyzerName + "-txt", () -> test(classpath, analyzer, analyzerName, "txt")));
			tests.add(DynamicTest.dynamicTest(analyzerName + "-html", () -> test(classpath, analyzer, analyzerName, "html")));
		}
		return tests;
	}

	private void test(Classpath classpath, Analyzer analyzer, String analyzerName, String reportType) throws IOException {

		// prepare
		ReportFormat reportFormat;
		switch (reportType) {
			case "txt":
				reportFormat = new TextReportFormat();
				break;
			case "html":
				reportFormat = new HtmlReportFormat();
				break;
			default:
				throw new IllegalArgumentException("reportType");
		}

		// analyze classpath
		Analysis analysis = new Analysis(analyzer);
		Report report = analysis.run(classpath);

		// create report
		String output = reportFormat.format(report);

		if (TestUtils.createResources()) {
			TestUtils.saveResource(getReportResourcePath(analyzerName, reportType), output, "UTF-8");
			return;
		}

		// normalize
		output = TextUtils.toUnixLineSeparators(output);
		String expectedOutput = TestUtils.getResourceAsString(getReportResourcePath(analyzerName, reportType), "UTF-8");
		expectedOutput = TextUtils.toUnixLineSeparators(expectedOutput);

		// assert
		assertEquals(expectedOutput, output);
	}

	private String getReportResourcePath(String analyzerName, String reportType) {
		return baseResourcePath + "report-" + analyzerName + "." + reportType;
	}

}
