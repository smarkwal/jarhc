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

package org.jarhc.report;

import static org.jarhc.utils.StringUtils.joinLines;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import org.jarhc.TestUtils;
import org.jarhc.test.TextUtils;
import org.jarhc.utils.Markdown;
import org.junit.jupiter.api.Test;

public abstract class AbstractReportFormatTest {

	private final ReportFormat format;

	public AbstractReportFormatTest(ReportFormat reportFormat) {
		this.format = reportFormat;
	}

	@Test
	void test_format() throws IOException {

		// prepare
		Report report = createTestReport();

		// test
		String text = format.format(report);

		String resource = "/org/jarhc/report/" + this.getClass().getSimpleName() + "/result.txt";
		if (TestUtils.createResources()) {
			text = text.replace("JarHC 0.0.1", "JarHC {version}");
			TestUtils.saveResource("test", resource, text, "UTF-8");
			return;
		}

		// assert
		String expectedResult = TestUtils.getResourceAsString(resource, "UTF-8");
		expectedResult = TextUtils.replacePlaceholders(expectedResult);
		assertEquals(expectedResult, text);
	}

	public Report createTestReport() {

		Report report = new Report();
		report.setTitle("Report Title");
		report.setTimestamp(1739808014453L);

		ReportSection section1 = new ReportSection("Section 1", "Description 1");
		section1.add("Some text.");
		report.addSection(section1);

		ReportSection section2 = new ReportSection("Section 2", joinLines("Description 2, Line 1", "Description 2, Line 2"));
		ReportTable table2 = new ReportTable("Column 1", "Column 2", "Column 3");
		table2.addRow("Short", "Medium Medium", "Long Long Long Long Long Long Long");
		table2.addRow("Line 1", joinLines("Line 1", "Longer Line 2"), joinLines("Line 1", "Longer Line 2", "Extra Long Line 3", "[[commons-io:commons-io:2.8.0]]"));
		section2.add(table2);
		report.addSection(section2);

		ReportSection section3 = new ReportSection("Section 3", null);
		ReportTable table3 = new ReportTable("Values");
		table3.addRow("1");
		table3.addRow("2");
		table3.addRow("3");
		section3.add(table3);
		ReportTable table3b = new ReportTable("Empty");
		section3.add(table3b);
		report.addSection(section3);

		ReportSection section4 = new ReportSection("Section 4", null);
		ReportSection subsection41 = new ReportSection("Subsection 4.1", "Description 4.1");
		subsection41.add("Some text.");
		section4.add(subsection41);
		ReportSection subsection42 = new ReportSection("Subsection 4.2", "Description 4.2");
		ReportSection subsection421 = new ReportSection("Subsection 4.2.1", "Description 4.2.1");
		subsection42.add(subsection421);
		subsection421.add("Some text.");
		subsection42.add("Some text.");
		section4.add(subsection42);
		report.addSection(section4);

		ReportSection section5 = new ReportSection("JAR Manifests", Markdown.deleted("Old description.") + "\n" + Markdown.inserted("New description."));
		ReportTable table5 = new ReportTable("Artifact", "Issues");
		table5.addRow("a.jar", Markdown.deleted("Old issue.") + "\n" + Markdown.inserted("New issue."));
		table5.addRow("b.jar", "Issue 1\nIssue 2");
		table5.addRow("c.jar", "Issue 1\n" + Markdown.deleted("Old issue 2") + "\n" + Markdown.inserted("New issue 2") + "\nIssue 3");
		section5.add(table5);
		section5.add(Markdown.deleted("Old content.") + "\n" + Markdown.inserted("New content."));
		report.addSection(section5);

		return report;
	}

}
