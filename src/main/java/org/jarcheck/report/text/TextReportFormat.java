package org.jarcheck.report.text;

import org.jarcheck.report.Report;
import org.jarcheck.report.ReportFormat;
import org.jarcheck.report.ReportSection;
import org.jarcheck.report.ReportTable;
import org.jarcheck.utils.StringUtils;

import java.io.PrintStream;
import java.util.List;

public class TextReportFormat implements ReportFormat {

	@Override
	public void format(Report report, PrintStream out) {
		List<ReportSection> sections = report.getSections();
		for (ReportSection section : sections) {
			formatSection(section, out);
			out.println();
		}
	}

	private void formatSection(ReportSection section, PrintStream out) {

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
		for (Object content : contents) {
			if (content instanceof ReportTable) {
				ReportTable table = (ReportTable) content;
				formatTable(table, out);
			} else {
				out.print(content);
			}
		}

	}

	private void formatTable(ReportTable table, PrintStream out) {
		String[] columns = table.getColumns();
		List<String[]> rows = table.getRows();
		int[] widths = calculateColumnWidths(columns, rows);
		formatTableRow(out, columns, widths);
		printTableSeparator(out, widths);
		for (String[] values : rows) {
			formatTableRow(out, values, widths);
		}
	}

	private static int[] calculateColumnWidths(String[] columns, List<String[]> rows) {
		int[] columnSizes = new int[columns.length];
		for (int i = 0; i < columnSizes.length; i++) {
			int length = columns[i].length();
			columnSizes[i] = Math.max(columnSizes[i], length);
		}
		for (String[] values : rows) {
			for (int i = 0; i < values.length; i++) {
				int length = values[i].length();
				columnSizes[i] = Math.max(columnSizes[i], length);
			}
		}
		return columnSizes;
	}

	private static void formatTableRow(PrintStream out, String[] values, int[] widths) {
		for (int i = 0; i < values.length; i++) {
			if (i > 0) {
				out.print(" | ");
			}
			String value = values[i];
			out.print(value);
			if (i < values.length - 1) {
				int width = widths[i];
				String padding = StringUtils.repeat(" ", width - value.length());
				out.print(padding);
			}
		}
		out.println();
	}

	private static void printTableSeparator(PrintStream out, int[] widths) {
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
