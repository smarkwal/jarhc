package org.jarcheck.it;

import org.jarcheck.Analysis;
import org.jarcheck.TestUtils;
import org.jarcheck.analyzer.Analyzer;
import org.jarcheck.analyzer.AnalyzerRegistry;
import org.jarcheck.loader.ClasspathLoader;
import org.jarcheck.model.Classpath;
import org.jarcheck.report.Report;
import org.jarcheck.report.ReportFormat;
import org.jarcheck.report.html.HtmlReportFormat;
import org.jarcheck.report.text.TextReportFormat;
import org.jarcheck.test.JavaRuntimeMock;
import org.jarcheck.test.TextUtils;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

abstract class AbstractIT {

	private final String baseResourcePath;
	private final String[] fileNames;

	AbstractIT(String baseResourcePath, String[] fileNames) {
		this.baseResourcePath = baseResourcePath;
		this.fileNames = fileNames;
	}

	@TestFactory
	Collection<DynamicTest> testAnalyzers() throws IOException {

		// prepare list of files
		List<File> files = new ArrayList<>();
		for (String fileName : fileNames) {
			String resourcePath = baseResourcePath + fileName;
			File file = TestUtils.getResourceAsFile(resourcePath, "IntegrationTest-");
			files.add(file);
		}

		// load classpath
		ClasspathLoader classpathLoader = new ClasspathLoader();
		Classpath classpath = classpathLoader.load(files);

		List<DynamicTest> tests = new ArrayList<>();
		AnalyzerRegistry registry = new AnalyzerRegistry(true, new JavaRuntimeMock());
		for (String analyzerName : registry.getAnalyzerNames()) {
			Analyzer analyzer = registry.getAnalyzer(analyzerName);
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
		// Files.write(Paths.get("src/test/resources" + getReportResourcePath(analyzerName, reportType)), output.getBytes());

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
