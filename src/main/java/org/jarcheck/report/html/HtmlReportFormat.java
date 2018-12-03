package org.jarcheck.report.html;

import org.jarcheck.report.Report;
import org.jarcheck.report.ReportFormat;
import org.jarcheck.report.ReportSection;
import org.jarcheck.report.ReportTable;

import java.io.PrintStream;
import java.util.List;
import java.util.regex.Pattern;

public class HtmlReportFormat implements ReportFormat {

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
		out.print("<h2>");
		out.print(escape(title));
		out.println("</h2>");
		if (description != null) {
			out.print("<p>");
			out.print(escape(description));
			out.println("</p>");
		}

		// format contents
		for (Object content : contents) {
			if (content instanceof ReportTable) {
				ReportTable table = (ReportTable) content;
				formatTable(table, out);
			} else {
				out.print("<p>");
				out.print(escape(content.toString()));
				out.println("</p>");
			}
		}

	}

	private void formatTable(ReportTable table, PrintStream out) {
		out.println("<table>");
		String[] columns = table.getColumns();
		printTableRow(out, columns, true);
		List<String[]> rows = table.getRows();
		for (String[] values : rows) {
			printTableRow(out, values, false);
		}
		out.println("</table>");
	}

	private static void printTableRow(PrintStream out, String[] values, boolean header) {
		out.print("\t<tr>");
		for (String value : values) {
			if (header) {
				out.print("<th>");
				out.print(escape(value));
				out.print("</th>");
			} else {
				out.print("<td>");
				out.print(escape(value));
				out.print("</td>");
			}
		}
		out.println("</tr>");
	}

	private static String escape(String text) {
		// TODO: implement HTML escaping
		text = text.replaceAll(Pattern.quote(System.lineSeparator()), "<br>");
		return text;
	}

}
