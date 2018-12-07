package org.jarcheck.it;

import org.jarcheck.Analysis;
import org.jarcheck.FullAnalysis;
import org.jarcheck.TestUtils;
import org.jarcheck.loader.ClasspathLoader;
import org.jarcheck.model.Classpath;
import org.jarcheck.report.Report;
import org.jarcheck.report.html.HtmlReportFormat;
import org.jarcheck.report.text.TextReportFormat;
import org.jarcheck.test.PrintStreamBuffer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AbstractIT {

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
		PrintStreamBuffer textReport = new PrintStreamBuffer();
		textFormat.format(report, textReport);
		String expectedTextReport = TestUtils.getResourceAsString(baseResourcePath + "report.txt", "UTF-8");
		// Files.write(Paths.get("src/test/resources/it/spring5/report.txt"), textReport.getText().getBytes());
		assertEquals(expectedTextReport, textReport.getText());

		// create HTML report
		HtmlReportFormat htmlFormat = new HtmlReportFormat();
		PrintStreamBuffer htmlReport = new PrintStreamBuffer();
		htmlFormat.format(report, htmlReport);
		String expectedHtmlReport = TestUtils.getResourceAsString(baseResourcePath + "report.html", "UTF-8");
		// Files.write(Paths.get("src/test/resources/it/spring5/report.html"), htmlReport.getText().getBytes());
		assertEquals(expectedHtmlReport, htmlReport.getText());

	}

}
