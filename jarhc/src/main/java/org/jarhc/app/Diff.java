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

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jarhc.inject.Injector;
import org.jarhc.report.Report;
import org.jarhc.report.ReportFormat;
import org.jarhc.report.ReportFormatFactory;
import org.jarhc.report.ReportSection;
import org.jarhc.report.ReportTable;
import org.jarhc.report.writer.ReportWriter;
import org.jarhc.report.writer.impl.FileReportWriter;
import org.jarhc.utils.DateTimeUtils;
import org.jarhc.utils.DiffUtils;
import org.jarhc.utils.FileUtils;
import org.jarhc.utils.JarHcException;
import org.jarhc.utils.Markdown;
import org.jarhc.utils.StringUtils;
import org.jarhc.utils.VersionUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;

public class Diff {

	private PrintStream out = System.out;
	private final Logger logger;

	public void setOut(PrintStream out) {
		this.out = out;
	}

	public Diff(Logger logger) {
		this.logger = logger;
	}

	public int run(Options options) {

		String version = VersionUtils.getVersion();
		out.println("JarHC - JAR Health Check " + version);
		out.println("=========================" + StringUtils.repeat("=", version.length()));
		out.println();

		// TODO: output progress
		// TODO: log debug messages

		// load report 1
		String inputPath1 = options.getInput1();
		out.println("Load report 1: " + inputPath1);
		Report report1 = loadJsonReport(inputPath1);
		if (!version.equals(report1.getVersion())) {
			String errorMessage = String.format("Report 1 was generated with a different version of JarHC: %s", report1.getVersion());
			out.println(errorMessage);
		}

		// load report 2
		String inputPath2 = options.getInput2();
		out.println("Load report 2: " + inputPath2);
		Report report2 = loadJsonReport(inputPath2);
		if (!version.equals(report1.getVersion())) {
			String errorMessage = String.format("Report 2 was generated with a different version of JarHC: %s", report1.getVersion());
			out.println(errorMessage);
		}

		// diff reports
		out.println("Compare reports ...");
		Report report = diff(report1, report2, options);

		out.println("Create diff report ...");
		// TODO: reuse existing code to generate output report
		// prepare an injector

		Injector injector = new Injector();
		injector.addBinding(Options.class, options);
		ReportFormatFactory reportFormatFactory = new ReportFormatFactory(injector);
		ReportFormat reportFormat = reportFormatFactory.getReportFormat("html");
		String outputPath = options.getReportFiles().get(0);
		try (ReportWriter writer = new FileReportWriter(new File(outputPath))) {
			reportFormat.format(report, writer);
		} catch (IOException e) {
			throw new JarHcException("I/O error when writing report: " + e.getMessage(), e);
		}

		return 0;
	}

	private Report loadJsonReport(String path) throws JarHcException {

		File file = new File(path);
		if (!file.isFile()) {
			throw new JarHcException("File not found: " + path);
		}

		String text;
		try {
			text = FileUtils.readFileToString(file);
		} catch (IOException e) {
			throw new JarHcException("I/O error when reading file: " + file.getAbsolutePath(), e);
		}

		JSONObject json;
		try {
			json = new JSONObject(text);
		} catch (JSONException e) {
			throw new JarHcException("Invalid JSON data in file: " + file.getAbsolutePath(), e);
		}

		try {
			return Report.fromJSON(json);
		} catch (Exception e) {
			throw new JarHcException("Invalid JSON report data in file: " + file.getAbsolutePath(), e);
		}

	}

	private Report diff(Report report1, Report report2, Options options) {

		// create a new empty report
		Report report = new Report();

		// set report title
		report.setTitle(options.getReportTitle());

		ReportSection diffSection = createDiffSection(report1, report2);
		report.addSection(diffSection);

		// TODO: use sections from options
		List<String> sections = options.getSections();

		List<ReportSection> sections1 = report1.getSections();
		List<ReportSection> sections2 = report2.getSections();

		// for every section in report 1 ...
		for (ReportSection section1 : sections1) {

			// find matching section in report 2
			String title = section1.getTitle();
			ReportSection section2 = sections2.stream()
					.filter(s -> s.getTitle().equals(title))
					.findFirst()
					.orElse(null);
			if (section2 == null) {
				logger.warn("Section found in report 1 but not in report 2: {}", title);
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
		ReportSection section = new ReportSection("Diff", "Information about the reports used as input for this diff report.");

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

	private ReportSection diff(ReportSection section1, ReportSection section2) {

		// create new section with same title and description
		String title = section1.getTitle();
		String description = section1.getDescription();
		ReportSection section = new ReportSection(title, description);

		out.println("   Section: " + title);

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

			if (item1 instanceof ReportTable) {
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

		List<String[]> rows1 = table1.getRows();
		List<String[]> rows2 = table2.getRows();

		Map<String, String[]> map1 = new LinkedHashMap<>();
		for (String[] row : rows1) {
			String key = row[0];
			if (map1.containsKey(key)) {
				logger.warn("Duplicate key in table of report 1: {}", key);
				continue;
			}
			map1.put(key, row);
		}

		Map<String, String[]> map2 = new LinkedHashMap<>();
		for (String[] row : rows2) {
			String key = row[0];
			if (map2.containsKey(key)) {
				logger.warn("Duplicate key in table of report 2: {}", key);
				continue;
			}
			map2.put(key, row);
		}

		// get collection of all distinct keys
		// TODO: implement a better merge strategy for keys with SequencesComparator
		List<String> keys = Stream.concat(
						map1.keySet().stream(),
						map2.keySet().stream()
				)
				.distinct()
				.collect(Collectors.toList());

		// for every key ...
		for (String key : keys) {

			// get rows for key (may be null if row is missing in a table)
			String[] row1 = map1.get(key);
			String[] row2 = map2.get(key);

			if (row1 == null) {
				// simulate an empty row with same size as row 2
				// TODO: mark complete row as inserted
				row1 = new String[row2.length];
				Arrays.fill(row1, "");
			} else if (row2 == null) {
				// simulate an empty row with same size as row 1
				// TODO: mark complete row as deleted
				row2 = new String[row1.length];
				Arrays.fill(row2, "");
			}

			String[] row = diff(row1, row2);
			table.addRow(row);
		}

		return table;
	}

	private String[] diff(String[] row1, String[] row2) {

		// prepare a new empty row of the same size
		String[] row = new String[row1.length];

		// for every column value in the rows ...
		for (int i = 0; i < row1.length; i++) {
			String value1 = row1[i];
			String value2 = row2[i];

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
