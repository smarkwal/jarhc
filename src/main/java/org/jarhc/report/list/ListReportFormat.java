/*
 * Copyright 2019 Stephan Markwalder
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

package org.jarhc.report.list;

import org.jarhc.report.Report;
import org.jarhc.report.ReportFormat;
import org.jarhc.report.ReportSection;
import org.jarhc.report.ReportTable;
import org.jarhc.report.writer.ReportWriter;
import org.jarhc.utils.StringUtils;

import java.util.List;

public class ListReportFormat implements ReportFormat {

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
		for (int i = 0; i < rows.size(); i++) {
			if (i > 0) {
				writer.println(); // add an empty line between every row
			}
			String[] values = rows.get(i);
			formatTableRow(columns, values, writer);
		}
	}

	private void formatTableRow(String[] columns, String[] values, ReportWriter writer) {

		for (int c = 0; c < columns.length; c++) {

			String column = columns[c];

			String value = values[c];
			if (value.isEmpty()) continue; // skip empty columns

			if (c == 0) {
				writer.println(column + ": " + value);
			} else {
				writer.println("\t" + column + ":");
				String[] lines = value.split("\\r?\\n");
				for (String line : lines) {
					writer.println("\t\t" + line);
				}
			}

		}

	}

}
