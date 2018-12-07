package org.jarcheck.report.html;

import org.jarcheck.TestUtils;
import org.jarcheck.report.Report;
import org.jarcheck.report.ReportSection;
import org.jarcheck.report.ReportTable;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HtmlReportFormatTest {

	@Test
	void test_format() throws IOException {

		// prepare
		Report report = new Report();

		ReportSection section1 = new ReportSection("Section 1", "Description 1");
		section1.add("Some text.");
		report.addSection(section1);

		ReportSection section2 = new ReportSection("Section 2", "Description 2, Line 1" + System.lineSeparator() + "Description 2, Line 2");
		ReportTable table2 = new ReportTable("Column 1", "Column 2", "Column 3");
		table2.addRow("Short", "Medium Medium", "Long Long Long Long Long Long Long");
		table2.addRow("Line 1", "Line 1" + System.lineSeparator() + "Longer Line 2", "Line 1" + System.lineSeparator() + "Longer Line 2" + System.lineSeparator() + "Extra Long Line 3");
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

		// assert
		String expectedResult = TestUtils.getResourceAsString("/HtmlReportFormatTest_result.txt", "UTF-8");
		assertEquals(expectedResult, text);

	}

}
