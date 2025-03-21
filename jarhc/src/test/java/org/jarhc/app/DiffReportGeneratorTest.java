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

import static org.jarhc.test.log.LoggerAssertions.assertLogger;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.util.List;
import org.jarhc.TestUtils;
import org.jarhc.report.Report;
import org.jarhc.report.ReportSection;
import org.jarhc.report.ReportTable;
import org.jarhc.test.log.LoggerBuilder;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

class DiffReportGeneratorTest {

	private static final long TIMESTAMP = 1739992928000L;
	private final Logger logger = LoggerBuilder.collect(DiffReportGenerator.class);
	private final DiffReportGenerator generator = new DiffReportGenerator(logger);

	private final Report report1 = generateTestReport("Test Report 1", false);
	private final Report report2a = generateTestReport("Test Report 2", false);
	private final Report report2b = generateTestReport("Test Report 2", true);
	private final Options options = new Options();

	@Test
	void test_noDifferences() throws IOException {

		// test
		Report report = generator.diff(report1, report2a, options);

		// assert
		assertJSON(report, "/org/jarhc/app/diff-report-1.json");
		assertLogger(logger)
				.hasWarn("Unexpected content in section 'Section 5': Boolean")
				.isEmpty();
	}

	@Test
	void test_differences() throws IOException {

		// prepare

		// replace row 3 in report 2
		List<String[]> rows = ((ReportTable) report2b.getSections().get(0).getContent().get(0)).getRows();
		rows.get(0)[1] = "Value 1.2\nAdditional line";
		rows.get(0)[2] = "Additional line\nValue 1.3";
		rows.get(1)[1] = "Replaced line";
		rows.get(1)[2] = "";
		rows.set(2, new String[] { "Value 4.1", "Value 4.2", "Value 4.3" });

		// test
		Report report = generator.diff(report1, report2b, options);

		// assert
		assertJSON(report, "/org/jarhc/app/diff-report-2.json");
		assertLogger(logger)
				.hasWarn("Section found in report 1 but not in report 2: Section 2")
				.hasWarn("Section found in report 2 but not in report 1: Section 3")
				.hasWarn("Non-matching size of section 'Section 1': 2 and 3")
				.hasWarn("Non-matching content in section 'Section 4': ReportTable and String")
				.hasWarn("Unexpected content in section 'Section 5': Boolean")
				.isEmpty();
	}

	private static void assertJSON(Report report, String resource) throws IOException {

		// override timestamp to have a deterministic result
		report.setTimestamp(TIMESTAMP);

		// convert report to JSON
		String actualJson = report.toJSON().toString(3);

		if (TestUtils.createResources()) {
			TestUtils.saveResource("test", resource, actualJson, "UTF-8");
			return;
		}

		String expectedJson = TestUtils.getResourceAsString(resource, "UTF-8");
		assertEquals(expectedJson, actualJson);
	}

	private static Report generateTestReport(String title, boolean diff) {

		// prepare report with given title and fixed timestamp
		Report report = new Report();
		report.setTitle(title);
		report.setTimestamp(TIMESTAMP);

		// add 6 sections
		for (int s = 1; s <= 6; s++) {

			// skip section 3 in report 1
			if (!diff && s == 3) {
				continue;
			}
			// skip section 2 in report 2
			if (diff && s == 2) {
				continue;
			}

			ReportSection section = new ReportSection("Section " + s, "Description for section " + s + ".");
			report.addSection(section);

			// section 5 contains an unexpected item in both reports
			if (s == 5) {
				section.getContent().add(diff);
				continue;
			}

			// section 6 is empty in both reports
			if (s == 6) {
				continue;
			}

			// add table with 3 columns and 3 rows
			ReportTable table = new ReportTable("Column 1", "Column 2", "Column 3");
			for (int r = 1; r <= 3; r++) {
				String[] values = new String[] { "Value " + r + ".1", "Value " + r + ".2", "Value " + r + ".3" };
				table.addRow(values);
			}
			section.addTable(table);

			// add a subsection to section 1
			if (s == 1) {
				ReportSection subsection = new ReportSection("Subsection 1.1", "Description for subsection 1.1.");
				subsection.addText("Text in subsection 1.1.");
				section.addSection(subsection);
			}

			// add additional content to section 1 in report 2
			if (diff && s == 1) {
				section.addText("This is additional text content.");
			}

			// replace content of section 4 in report 2 with a non-table item
			if (diff && s == 4) {
				section.getContent().clear();
				section.addText("This is a text item instead of a table.");
			}
		}

		// add last section with text content
		ReportSection section = new ReportSection("Last section", "Description for last section.");
		if (diff) {
			String text = "This is a changed text section.\nIt contains multiple lines of text.";
			section.addText(text);
		} else {
			String text = "This is a text section.\nIt contains multiple lines of text.";
			section.addText(text);
		}
		report.addSection(section);

		return report;
	}

}
