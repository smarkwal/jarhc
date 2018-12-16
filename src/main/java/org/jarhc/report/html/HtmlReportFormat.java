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

import org.jarhc.report.Report;
import org.jarhc.report.ReportFormat;
import org.jarhc.report.ReportSection;
import org.jarhc.report.ReportTable;

import java.io.PrintWriter;
import java.util.List;
import java.util.regex.Pattern;

public class HtmlReportFormat implements ReportFormat {

	@Override
	public void format(Report report, PrintWriter out) {

		out.println("<!DOCTYPE html>");
		out.println("<html>");
		out.println("<head>");

		// add optional report title
		String title = report.getTitle();
		if (title != null) {
			out.print("<title>");
			out.print(escape(title));
			out.println("</title>");
		}

		out.println("</head>");
		out.println("<body>");
		out.println();

		List<ReportSection> sections = report.getSections();
		for (ReportSection section : sections) {
			formatSection(section, out);
			out.println();
		}

		out.println("</body>");
		out.println("</html>");
	}

	private void formatSection(ReportSection section, PrintWriter out) {

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

	private void formatTable(ReportTable table, PrintWriter out) {
		out.println("<table>");
		String[] columns = table.getColumns();
		printTableRow(out, columns, true);
		List<String[]> rows = table.getRows();
		for (String[] values : rows) {
			printTableRow(out, values, false);
		}
		out.println("</table>");
	}

	private static void printTableRow(PrintWriter out, String[] values, boolean header) {
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
		// TODO: implement full HTML escaping
		text = text.replace("<", "&lt;");
		text = text.replace(">", "&gt;");
		text = text.replace("\"", "&quote;");
		text = text.replaceAll(Pattern.quote(System.lineSeparator()), "<br>");
		return text;
	}

}
