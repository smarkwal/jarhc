/*
 * Copyright 2025 Stephan Markwalder
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import org.jarhc.report.Report;
import org.jarhc.report.ReportFormat;
import org.jarhc.report.ReportSection;
import org.jarhc.report.html.HtmlReportFormat;
import org.jarhc.report.json.JsonReportFormat;
import org.jarhc.report.writer.ReportWriter;
import org.jarhc.report.writer.impl.FileReportWriter;
import org.jarhc.test.PrintStreamBuffer;
import org.jarhc.utils.FileUtils;
import org.jarhc.utils.JarHcException;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class DiffTest {

	@Test
	void run(@TempDir File tempDir) throws Exception {

		// prepare
		File inputFile1 = generateTestReport(tempDir, "report1.json", "Test report 1", "0.0.1");
		File inputFile2 = generateTestReport(tempDir, "report2.html", "Test report 2", "0.0.1");
		File reportFile = new File(tempDir, "report.html");

		// test
		String output = run(inputFile1, inputFile2, reportFile);

		// assert
		assertThat(output)
				.startsWith("JarHC - JAR Health Check 0.0.1");
		assertThat(reportFile)
				.isFile()
				.content()
				.startsWith("<!DOCTYPE html>");
	}

	@Test
	void run_differentVersions(@TempDir File tempDir) throws Exception {

		// prepare
		File inputFile1 = generateTestReport(tempDir, "report1.json", "Test report 1", "0.0.2");
		File inputFile2 = generateTestReport(tempDir, "report2.html", "Test report 2", "0.0.3");
		File reportFile = new File(tempDir, "report.html");

		// test
		String output = run(inputFile1, inputFile2, reportFile);

		// assert
		assertThat(output)
				.startsWith("JarHC - JAR Health Check 0.0.1")
				.contains("Report 1 was generated with a different version of JarHC: 0.0.2")
				.contains("Report 2 was generated with a different version of JarHC: 0.0.3");
		assertThat(reportFile)
				.isFile()
				.content()
				.startsWith("<!DOCTYPE html>");
	}

	@Test
	void run_inputFile1NotFound(@TempDir File tempDir) throws Exception {

		// prepare
		File inputFile1 = new File(tempDir, "report1.json"); // file "report1.json" does not exist
		File inputFile2 = generateTestReport(tempDir, "report2.html", "Test report 2", "0.0.1");
		File reportFile = new File(tempDir, "report.html");

		// test
		Exception result = assertThrows(JarHcException.class, () -> run(inputFile1, inputFile2, reportFile));

		// assert
		assertThat(result)
				.hasMessageStartingWith("File not found: ")
				.hasMessageEndingWith("/report1.json")
				.hasNoCause();
		assertThat(reportFile).doesNotExist();
	}

	@Test
	void run_inputFile2NotFound(@TempDir File tempDir) throws Exception {

		// prepare
		File inputFile1 = generateTestReport(tempDir, "report1.json", "Test report 1", "0.0.1");
		File inputFile2 = new File(tempDir, "report2.html"); // file "report2.html" does not exist
		File reportFile = new File(tempDir, "report.html");

		// test
		Exception result = assertThrows(JarHcException.class, () -> run(inputFile1, inputFile2, reportFile));

		// assert
		assertThat(result)
				.hasMessageStartingWith("File not found: ")
				.hasMessageEndingWith("/report2.html")
				.hasNoCause();
		assertThat(reportFile).doesNotExist();
	}

	@Test
	void run_invalidJsonData(@TempDir File tempDir) throws Exception {

		// prepare
		File inputFile1 = generateTestReport(tempDir, "report1.json", "Test report 1", "0.0.1");
		File inputFile2 = generateTestReport(tempDir, "report2.html", "Test report 2", "0.0.1");
		File reportFile = new File(tempDir, "report.html");

		FileUtils.writeStringToFile("This is not JSON data!", inputFile1);

		// test
		Exception result = assertThrows(JarHcException.class, () -> run(inputFile1, inputFile2, reportFile));

		// assert
		assertThat(result)
				.hasMessageStartingWith("Invalid JSON data in file: ")
				.hasMessageEndingWith("/report1.json")
				.hasCause(new JSONException("A JSONObject text must begin with '{' at 1 [character 2 line 1]"));
		assertThat(reportFile).doesNotExist();
	}

	@Test
	void run_invalidJsonDataInHtmlFile(@TempDir File tempDir) throws Exception {

		// prepare
		File inputFile1 = generateTestReport(tempDir, "report1.json", "Test report 1", "0.0.1");
		File inputFile2 = generateTestReport(tempDir, "report2.html", "Test report 2", "0.0.1");
		File reportFile = new File(tempDir, "report.html");

		String html = FileUtils.readFileToString(inputFile2);
		html = html.replace("<!-- JSON REPORT DATA", "This is not JSON data!");
		FileUtils.writeStringToFile(html, inputFile2);

		// test
		Exception result = assertThrows(JarHcException.class, () -> run(inputFile1, inputFile2, reportFile));

		// assert
		assertThat(result)
				.hasMessage("JSON data not found in file.")
				.hasNoCause();
		assertThat(reportFile).doesNotExist();
	}

	@Test
	void run_reportFileNotFound(@TempDir File tempDir) throws Exception {

		// prepare
		File inputFile1 = generateTestReport(tempDir, "report1.json", "Test report 1", "0.0.2");
		File inputFile2 = generateTestReport(tempDir, "report2.html", "Test report 2", "0.0.3");
		File reportFile = new File(tempDir, "foo/report.html"); // directory "foo" does not exist

		// test
		Exception result = assertThrows(JarHcException.class, () -> run(inputFile1, inputFile2, reportFile));

		// assert
		assertThat(result)
				.hasMessageStartingWith("I/O error for file ")
				.hasCauseInstanceOf(FileNotFoundException.class);
		assertThat(reportFile).doesNotExist();
	}

	private static String run(File inputFile1, File inputFile2, File reportFile) {

		// prepare
		Properties properties = new Properties();
		Options options = new Options(Options.Command.DIFF, properties);
		options.setInput1(inputFile1.getAbsolutePath());
		options.setInput2(inputFile2.getAbsolutePath());
		options.addReportFile(reportFile.getAbsolutePath());

		Diff diff = new Diff();
		PrintStreamBuffer out = new PrintStreamBuffer();
		diff.setOut(out);

		// test
		int result = diff.run(options);

		// assert
		assertEquals(0, result);
		return out.getText();
	}

	private File generateTestReport(File tempDir, String fileName, String title, String version) throws IOException {

		Report report = new Report();
		report.setType(Report.Type.SCAN);
		report.setTitle(title);
		report.setVersion(version);

		ReportSection section = new ReportSection("Test Section", "Description of the test section.");
		section.add("Test content.");
		report.addSection(section);

		ReportFormat format;
		if (fileName.endsWith(".html")) {
			format = new HtmlReportFormat();
		} else {
			format = new JsonReportFormat();
		}

		File file = new File(tempDir, fileName);
		try (ReportWriter writer = new FileReportWriter(file)) {
			format.format(report, writer);
		}

		return file;
	}

}
