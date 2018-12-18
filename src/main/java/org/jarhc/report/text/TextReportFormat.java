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

package org.jarhc.report.text;

import org.jarhc.report.Report;
import org.jarhc.report.ReportFormat;
import org.jarhc.report.ReportSection;
import org.jarhc.report.ReportTable;
import org.jarhc.report.writer.ReportWriter;
import org.jarhc.utils.StringUtils;

import java.util.List;
import java.util.regex.Pattern;

public class TextReportFormat implements ReportFormat {

	@Override
	public void format(Report report, ReportWriter writer) {

		// add optional report title
		String title = report.getTitle();
		if (title != null) {
			writer.println(title);
			writer.println(StringUtils.repeat("=", title.length()));
			writer.println();
		}

		List<ReportSection> sections = report.getSections();
		for (ReportSection section : sections) {
			formatSection(section, writer);
			writer.println();
		}
	}

	private void formatSection(ReportSection section, ReportWriter writer) {

		String title = section.getTitle();
		String description = section.getDescription();
		List<Object> contents = section.getContent();

		// format header
		writer.println(title);
		writer.println(StringUtils.repeat("-", title.length()));
		if (description != null) {
			writer.println(description);
		}
		writer.println();

		// format contents
		for (int i = 0; i < contents.size(); i++) {
			Object content = contents.get(i);
			if (i > 0) {
				writer.println(); // add an empty line between all content blocks
			}
			if (content instanceof ReportTable) {
				ReportTable table = (ReportTable) content;
				formatTable(table, writer);
			} else {
				String text = String.valueOf(content);
				writer.println(text);
			}
		}

	}

	private void formatTable(ReportTable table, ReportWriter writer) {
		String[] columns = table.getColumns();
		List<String[]> rows = table.getRows();
		int[] widths = calculateColumnWidths(columns, rows);
		formatTableRow(writer, columns, widths);
		printTableSeparator(writer, widths);
		for (String[] values : rows) {
			formatTableRow(writer, values, widths);
			// printTableSeparator(writer, widths);
		}
	}

	private static int[] calculateColumnWidths(String[] columns, List<String[]> rows) {
		int[] columnSizes = new int[columns.length];
		for (int i = 0; i < columnSizes.length; i++) {
			int length = getColumnWidth(columns[i]);
			columnSizes[i] = Math.max(columnSizes[i], length);
		}
		for (String[] values : rows) {
			for (int i = 0; i < values.length; i++) {
				int length = getColumnWidth(values[i]);
				columnSizes[i] = Math.max(columnSizes[i], length);
			}
		}
		return columnSizes;
	}

	private static int getColumnWidth(String text) {
		if (text == null || text.isEmpty()) return 0;
		int maxLength = 0;
		String[] lines = text.split("\\r?\\n");
		for (String line : lines) {
			int width = line.length();
			maxLength = Math.max(maxLength, width);
		}
		return maxLength;
	}

	private static void formatTableRow(ReportWriter writer, String[] values, int[] widths) {

		String[][] cells = new String[values.length][];

		int maxHeight = 1;
		for (int i = 0; i < values.length; i++) {
			String value = values[i];
			String[] lines = value.split(Pattern.quote(System.lineSeparator()));
			cells[i] = lines;
			maxHeight = Math.max(maxHeight, lines.length);
		}

		for (int h = 0; h < maxHeight; h++) {
			for (int i = 0; i < cells.length; i++) {
				String[] cell = cells[i];
				String value = h < cell.length ? cell[h] : "";
				if (i > 0) {
					if (i < cells.length - 1 || !value.isEmpty()) {
						writer.print(" | ");
					} else {
						writer.print(" |"); // no trailing space in an empty last cell
					}
				}
				writer.print(value);
				if (i < cells.length - 1) {
					int width = widths[i];
					String padding = StringUtils.repeat(" ", width - value.length());
					writer.print(padding);
				}
			}
			writer.println();
		}
	}

	private static void printTableSeparator(ReportWriter writer, int[] widths) {
		for (int i = 0; i < widths.length; i++) {
			if (i > 0) {
				writer.print("-+-");
			}
			int width = widths[i];
			String line = StringUtils.repeat("-", width);
			writer.print(line);
		}
		writer.println();
	}

}
