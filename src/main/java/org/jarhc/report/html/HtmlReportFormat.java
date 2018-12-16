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

	private final StyleProvider styleProvider;

	public HtmlReportFormat() {
		// TODO: use dependency injection
		this(new DefaultStyleProvider());
	}

	public HtmlReportFormat(StyleProvider styleProvider) {
		this.styleProvider = styleProvider;
	}

	@Override
	public void format(Report report, PrintWriter out) {
		out.println("<!DOCTYPE html>");
		out.println("<html lang=\"en\">");
		formatHtmlHead(report, out);
		formatHtmlBody(report, out);
		out.println("</html>");
	}

	private void formatHtmlHead(Report report, PrintWriter out) {
		out.println("<head>");

		// add optional report title
		String title = report.getTitle();
		if (title != null) {
			out.print("<title>");
			out.print(escape(title));
			out.println("</title>");
		}

		// include CSS styles
		String css = styleProvider.getStyle();
		if (css != null) {
			out.println("<style>");
			out.println(css);
			out.println("</style>");
		}

		out.println("</head>");
	}

	private void formatHtmlBody(Report report, PrintWriter out) {
		out.println("<body>");
		out.println();

		// add optional title
		String title = report.getTitle();
		if (title != null) {
			out.print("<h1>");
			out.print(escape(title));
			out.println("</h1>");
			out.println();
		}

		// if report contains more than 1 section ...
		List<ReportSection> sections = report.getSections();
		if (sections.size() > 1) {
			// add table of contents
			formatToC(report, out);
		}

		// add individual sections
		for (ReportSection section : sections) {
			formatSection(section, out);
			out.println();
		}

		out.println("</body>");
	}

	private void formatToC(Report report, PrintWriter out) {
		out.println("<h3>Table of Contents</h3>");
		out.println("<ul>");
		List<ReportSection> sections = report.getSections();
		for (ReportSection section : sections) {
			String title = section.getTitle();
			String id = section.getId();
			out.printf("<li><a href=\"#%s\">%s</a></li>%s", id, escape(title), System.lineSeparator());
		}
		out.println("</ul>");
		out.println();
	}

	private void formatSection(ReportSection section, PrintWriter out) {

		String title = section.getTitle();
		String id = section.getId();
		String description = section.getDescription();
		List<Object> contents = section.getContent();

		// section start
		out.print("<section id=\"");
		out.print(id);
		out.println("\">");

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

		out.println("</section>");

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
