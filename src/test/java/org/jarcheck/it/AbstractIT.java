package org.jarcheck.it;

import org.jarcheck.Analysis;
import org.jarcheck.FullAnalysis;
import org.jarcheck.TestUtils;
import org.jarcheck.loader.ClasspathLoader;
import org.jarcheck.model.Classpath;
import org.jarcheck.report.Report;
import org.jarcheck.report.html.HtmlReportFormat;
import org.jarcheck.report.text.TextReportFormat;
import org.jarcheck.test.TextUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

abstract class AbstractIT {

	protected void test(String baseResourcePath, String[] fileNames) throws IOException {

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

		// analyze classpath
		Analysis analysis = new FullAnalysis();
		Report report = analysis.run(classpath);

		// create text report
		TextReportFormat textFormat = new TextReportFormat();
		String textReport = textFormat.format(report);
		// Files.write(Paths.get("src/test/resources" + baseResourcePath + "report.txt"), textReport.getBytes());

		// normalize
		textReport = TextUtils.toUnixLineSeparators(textReport);
		String expectedTextReport = TestUtils.getResourceAsString(baseResourcePath + "report.txt", "UTF-8");
		expectedTextReport = TextUtils.toUnixLineSeparators(expectedTextReport);

		// assert
		assertEquals(expectedTextReport, textReport);

		// create HTML report
		HtmlReportFormat htmlFormat = new HtmlReportFormat();
		String htmlReport = htmlFormat.format(report);
		// Files.write(Paths.get("src/test/resources" + baseResourcePath + "report.html"), htmlReport.getBytes());

		// normalize
		htmlReport = TextUtils.toUnixLineSeparators(htmlReport);
		String expectedHtmlReport = TestUtils.getResourceAsString(baseResourcePath + "report.html", "UTF-8");
		expectedHtmlReport = TextUtils.toUnixLineSeparators(expectedHtmlReport);

		// assert
		assertEquals(expectedHtmlReport, htmlReport);

	}

}
