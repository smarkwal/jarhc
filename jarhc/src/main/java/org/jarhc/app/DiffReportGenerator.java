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

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jarhc.report.Report;
import org.jarhc.report.ReportSection;
import org.jarhc.report.ReportTable;
import org.jarhc.utils.DateTimeUtils;
import org.jarhc.utils.DiffUtils;
import org.jarhc.utils.Markdown;
import org.jarhc.utils.StringUtils;
import org.slf4j.Logger;

public class DiffReportGenerator {

	private final Logger logger;

	public DiffReportGenerator(Logger logger) {
		this.logger = logger;
	}

	public Report diff(Report report1, Report report2, Options options) {

		// create a new empty report
		Report report = new Report();
		report.setType(Report.Type.DIFF);

		// set report title
		report.setTitle(options.getReportTitle());

		ReportSection diffSection = createDiffSection(report1, report2);
		report.addSection(diffSection);

		// TODO: use sections from options
		// List<String> sections = options.getSections();

		List<ReportSection> sections1 = report1.getSections();
		List<ReportSection> sections2 = report2.getSections();
		logMissingSections(sections1, sections2);

		// for every section in report 1 ...
		for (ReportSection section1 : sections1) {

			// find matching section in report 2
			String title = section1.getTitle();
			ReportSection section2 = sections2.stream()
					.filter(s -> s.getTitle().equals(title))
					.findFirst()
					.orElse(null);
			if (section2 == null) {
				continue;
			}

			// diff sections and add result to new report
			ReportSection section = diff(section1, section2);
			if (section != null) {
				report.addSection(section);
			}
		}

		return report;
	}

	private ReportSection createDiffSection(Report report1, Report report2) {
		ReportSection section = new ReportSection("Reports", "Information about the reports used as input for this diff report.");

		String text = Markdown.bold("Report 1") + "\n" +
				report1.getTitle() + "\n" +
				DateTimeUtils.formatTimestamp(report1.getTimestamp()) + "\n" +
				"JarHC " + report1.getVersion() + "\n" +
				"\n" +
				Markdown.bold("Report 2") + "\n" +
				report2.getTitle() + "\n" +
				DateTimeUtils.formatTimestamp(report2.getTimestamp()) + "\n" +
				"JarHC " + report2.getVersion() + "\n";
		section.add(text);

		return section;
	}

	private void logMissingSections(List<ReportSection> sections1, List<ReportSection> sections2) {

		// get all section titles from both reports
		List<String> sectionTitles1 = sections1.stream().map(ReportSection::getTitle).collect(Collectors.toList());
		List<String> sectionTitles2 = sections2.stream().map(ReportSection::getTitle).collect(Collectors.toList());

		// find sections present in report 1 but missing in report 2
		sectionTitles1.stream()
				.filter(title -> !sectionTitles2.contains(title))
				.forEach(title -> logger.warn("Section found in report 1 but not in report 2: {}", title));

		// find sections present in report 2 but missing in report 1
		sectionTitles2.stream()
				.filter(title -> !sectionTitles1.contains(title))
				.forEach(title -> logger.warn("Section found in report 2 but not in report 1: {}", title));
	}

	private ReportSection diff(ReportSection section1, ReportSection section2) {

		// create new section with same title and description
		String title = section1.getTitle();
		String description = section1.getDescription();
		ReportSection section = new ReportSection(title, description);

		// get content of both sections
		List<Object> content1 = section1.getContent();
		List<Object> content2 = section2.getContent();

		// compare size of sections
		int size1 = content1.size();
		int size2 = content2.size();
		if (size1 != size2) {
			logger.warn("Non-matching size of section '{}': {} and {}", title, size1, size2);
		}

		// for every item in both sections ...
		int size = Math.min(size1, size2);
		for (int i = 0; i < size; i++) {
			Object item1 = content1.get(i);
			Object item2 = content2.get(i);

			// check if items have same type
			if (item1.getClass() != item2.getClass()) {
				String type1 = item1.getClass().getSimpleName();
				String type2 = item2.getClass().getSimpleName();
				logger.warn("Non-matching content in section '{}': {} and {}", title, type1, type2);
				continue;
			}

			if (item1 instanceof ReportSection) {
				ReportSection subsection1 = (ReportSection) item1;
				ReportSection subsection2 = (ReportSection) item2;
				ReportSection subsection = diff(subsection1, subsection2);
				if (subsection != null) {
					section.add(subsection);
				}
			} else if (item1 instanceof ReportTable) {
				ReportTable table1 = (ReportTable) item1;
				ReportTable table2 = (ReportTable) item2;
				ReportTable table = diff(table1, table2);
				if (table != null) {
					section.add(table);
				}
			} else if (item1 instanceof String) {
				String value1 = (String) item1;
				String value2 = (String) item2;
				String value = diff(value1, value2);
				section.add(value);
			} else {
				String type = item1.getClass().getSimpleName();
				logger.warn("Unexpected content in section '{}': {}", title, type);
			}
		}

		if (section.isEmpty()) {
			return null;
		}

		return section;
	}

	private ReportTable diff(ReportTable table1, ReportTable table2) {

		String[] columns1 = table1.getColumns();
		String[] columns2 = table2.getColumns();

		// compare columns
		if (!Arrays.equals(columns1, columns2)) {
			logger.warn("Non-matching columns in table: {} and {}", columns1.length, columns2.length);
			return null; // TODO: support tables with different columns
		}

		// create new empty table with same columns
		ReportTable table = new ReportTable(columns1);

		// collect rows by "key" (value in first column)
		Map<String, List<String[]>> map1 = table1.getRows().stream().collect(Collectors.groupingBy(row -> row[0]));
		Map<String, List<String[]>> map2 = table2.getRows().stream().collect(Collectors.groupingBy(row -> row[0]));

		// get all distinct keys (sorted)
		List<String> keys = Stream.concat(
						map1.keySet().stream(),
						map2.keySet().stream()
				)
				.sorted(ReportTable.RowComparator.INSTANCE)
				.distinct()
				.collect(Collectors.toList());

		// for every key ...
		for (String key : keys) {

			// get rows for key (0 or more rows per key
			List<String[]> rows1 = map1.getOrDefault(key, List.of());
			List<String[]> rows2 = map2.getOrDefault(key, List.of());

			// for every row in both tables ...
			int rowCount1 = rows1.size();
			int rowCount2 = rows2.size();
			int rowCountMax = Math.max(rowCount1, rowCount2);
			for (int r = 0; r < rowCountMax; r++) {

				// get row from both tables
				String[] row1 = r < rowCount1 ? rows1.get(r) : null;
				String[] row2 = r < rowCount2 ? rows2.get(r) : null;

				String[] row = diff(row1, row2);
				table.addRow(row);
			}
		}

		return table;
	}

	private String[] diff(String[] row1, String[] row2) {

		int length = row1 != null ? row1.length : row2.length;

		// prepare a new empty row of the same size
		String[] row = new String[length];

		// for every column value in the rows ...
		for (int i = 0; i < length; i++) {
			String value1 = row1 != null ? row1[i] : "";
			String value2 = row2 != null ? row2[i] : "";

			// compare values
			String value = diff(value1, value2);
			row[i] = value;
		}

		return row;
	}

	private String diff(String value1, String value2) {

		// if values are equal, return one of them
		if (value1.equals(value2)) {
			return value1;
		}

		// split values into lines
		List<String> lines1 = value1.isEmpty() ? List.of() : List.of(value1.split("\n"));
		List<String> lines2 = value2.isEmpty() ? List.of() : List.of(value2.split("\n"));

		// compare lines and calculate diff
		List<String> lines = DiffUtils.diff(lines1, lines2);

		return StringUtils.joinLines(lines);
	}

}
