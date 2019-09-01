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

package org.jarhc.report.html;

import static org.jarhc.utils.StringUtils.joinLines;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import org.jarhc.TestUtils;
import org.jarhc.report.Report;
import org.jarhc.report.ReportSection;
import org.jarhc.report.ReportTable;
import org.jarhc.test.TextUtils;
import org.junit.jupiter.api.Test;

class HtmlReportFormatTest {

	@Test
	void test_format() throws IOException {

		// prepare
		Report report = new Report();
		report.setTitle("Report Title");

		ReportSection section1 = new ReportSection("Section 1", "Description 1");
		section1.add("Some text.");
		report.addSection(section1);

		ReportSection section2 = new ReportSection("Section 2", joinLines("Description 2, Line 1", "Description 2, Line 2"));
		ReportTable table2 = new ReportTable("Column 1", "Column 2", "Column 3");
		table2.addRow("Short", "Medium Medium", "Long Long Long Long Long Long Long");
		table2.addRow("Line 1", joinLines("Line 1", "Longer Line 2"), joinLines("Line 1", "Longer Line 2", "Extra Long Line 3"));
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

		// test
		HtmlReportFormat format = new HtmlReportFormat();
		String text = format.format(report);

		if (TestUtils.createResources()) {
			TestUtils.saveResource("/HtmlReportFormatTest/result.txt", text, "UTF-8");
			return;
		}

		// normalize
		text = TextUtils.toUnixLineSeparators(text);
		String expectedResult = TestUtils.getResourceAsString("/HtmlReportFormatTest/result.txt", "UTF-8");
		expectedResult = TextUtils.toUnixLineSeparators(expectedResult);

		// assert
		assertEquals(expectedResult, text);

	}

}
