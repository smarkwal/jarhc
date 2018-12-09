package org.jarhc.report.text;

import org.jarhc.report.Report;
import org.jarhc.report.ReportFormat;
import org.jarhc.report.ReportSection;
import org.jarhc.report.ReportTable;
import org.jarhc.utils.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.List;
import java.util.regex.Pattern;

public class TextReportFormat implements ReportFormat {

	@Override
	public void format(Report report, PrintWriter out) {
		List<ReportSection> sections = report.getSections();
		for (ReportSection section : sections) {
			formatSection(section, out);
			out.println();
		}
	}

	private void formatSection(ReportSection section, PrintWriter out) {

		String title = section.getTitle();
		String description = section.getDescription();
		List<Object> contents = section.getContent();

		// format header
		out.println(title);
		out.println(StringUtils.repeat("-", title.length()));
		if (description != null) {
			out.println(description);
		}
		out.println();

		// format contents
		for (int i = 0; i < contents.size(); i++) {
			Object content = contents.get(i);
			if (i > 0) {
				out.println(); // add an empty line between all content blocks
			}
			if (content instanceof ReportTable) {
				ReportTable table = (ReportTable) content;
				formatTable(table, out);
			} else {
				out.println(content);
			}
		}

	}

	private void formatTable(ReportTable table, PrintWriter out) {
		String[] columns = table.getColumns();
		List<String[]> rows = table.getRows();
		int[] widths = calculateColumnWidths(columns, rows);
		formatTableRow(out, columns, widths);
		printTableSeparator(out, widths);
		for (String[] values : rows) {
			formatTableRow(out, values, widths);
			// printTableSeparator(out, widths);
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
		BufferedReader reader = new BufferedReader(new StringReader(text));
		int maxLength = 0;
		try {
			while (true) {
				String line = reader.readLine();
				if (line == null) break;
				int width = line.length();
				maxLength = Math.max(maxLength, width);

			}
		} catch (IOException e) {
			// ignore
		}
		return maxLength;
	}

	private static void formatTableRow(PrintWriter out, String[] values, int[] widths) {

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
						out.print(" | ");
					} else {
						out.print(" |"); // no trailing space in an empty last cell
					}
				}
				out.print(value);
				if (i < cells.length - 1) {
					int width = widths[i];
					String padding = StringUtils.repeat(" ", width - value.length());
					out.print(padding);
				}
			}
			out.println();
		}
	}

	private static void printTableSeparator(PrintWriter out, int[] widths) {
		for (int i = 0; i < widths.length; i++) {
			if (i > 0) {
				out.print("-+-");
			}
			int width = widths[i];
			String line = StringUtils.repeat("-", width);
			out.print(line);
		}
		out.println();
	}

}
