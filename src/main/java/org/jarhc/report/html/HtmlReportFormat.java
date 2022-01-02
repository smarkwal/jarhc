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

import java.util.List;
import java.util.regex.Pattern;
import org.jarhc.report.Report;
import org.jarhc.report.ReportFormat;
import org.jarhc.report.ReportSection;
import org.jarhc.report.ReportTable;
import org.jarhc.report.writer.ReportWriter;
import org.jarhc.utils.VersionUtils;
import org.slf4j.LoggerFactory;

public class HtmlReportFormat implements ReportFormat {

	private final StyleProvider styleProvider;

	public HtmlReportFormat() {
		// TODO: use dependency injection
		this(new DefaultStyleProvider(LoggerFactory.getLogger(DefaultStyleProvider.class)));
	}

	public HtmlReportFormat(StyleProvider styleProvider) {
		this.styleProvider = styleProvider;
	}

	@Override
	public void format(Report report, ReportWriter writer) {
		writer.println("<!DOCTYPE html>");
		writer.println("<html lang=\"en\">");
		formatHtmlHead(report, writer);
		formatHtmlBody(report, writer);
		writer.println("</html>");
	}

	private void formatHtmlHead(Report report, ReportWriter writer) {
		writer.println("<head>");

		// add optional report title
		String title = report.getTitle();
		if (title != null) {
			writer.println("<title>%s</title>", escape(title));
		}

		// set character set
		writer.println("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"/>");

		// set JarHC with version as generator
		writer.println("<meta name=\"generator\" content=\"JarHC " + VersionUtils.getVersion() + "\" />");

		// include CSS styles
		String css = styleProvider.getStyle();
		if (css != null) {
			writer.println("<style>");
			writer.println(css);
			writer.println("</style>");
		}

		writer.println("</head>");
	}

	private void formatHtmlBody(Report report, ReportWriter writer) {
		writer.println("<body>");
		writer.println();

		// add optional title
		String title = report.getTitle();
		if (title != null) {
			writer.println("<h1>%s</h1>", escape(title));
			writer.println();
		}

		// if report contains more than 1 section ...
		List<ReportSection> sections = report.getSections();
		if (sections.size() > 1) {
			// add table of contents
			formatToC(report, writer);
		}

		// add individual sections
		for (ReportSection section : sections) {
			formatSection(section, writer);
			writer.println();
		}

		// add "generated with" note
		writer.println("<div class=\"generator\">Generated with <a href=\"http://jarhc.org\" target=\"_blank\">JarHC " + VersionUtils.getVersion() + "</a></div>");

		writer.println("</body>");
	}

	private void formatToC(Report report, ReportWriter writer) {
		writer.println("<h3>Table of Contents</h3>");
		writer.println("<ul>");
		List<ReportSection> sections = report.getSections();
		for (ReportSection section : sections) {
			String title = section.getTitle();
			String id = section.getId();
			writer.println("<li><a href=\"#%s\">%s</a></li>", id, escape(title));
		}
		writer.println("</ul>");
		writer.println();
	}

	private void formatSection(ReportSection section, ReportWriter writer) {

		String title = section.getTitle();
		String id = section.getId();
		String description = section.getDescription();
		List<Object> contents = section.getContent();

		// section start
		writer.println("<section id=\"%s\">", id);

		// format header
		writer.println("<h2>%s</h2>", escape(title));
		if (description != null) {
			writer.println("<p>%s</p>", escape(description));
		}

		// format contents
		for (Object content : contents) {
			if (content instanceof ReportTable) {
				ReportTable table = (ReportTable) content;
				formatTable(table, writer);
			} else {
				writer.println("<p>%s</p>", escape(content.toString()));
			}
		}

		writer.println("</section>");

	}

	private void formatTable(ReportTable table, ReportWriter writer) {
		writer.println("<table>");
		String[] columns = table.getColumns();
		printTableRow(writer, columns, true);
		List<String[]> rows = table.getRows();
		for (String[] values : rows) {
			printTableRow(writer, values, false);
		}
		writer.println("</table>");
	}

	private static void printTableRow(ReportWriter writer, String[] values, boolean header) {
		writer.print("\t<tr>");
		for (String value : values) {
			if (header) {
				writer.print("<th>%s</th>", escape(value));
			} else {
				writer.print("<td>%s</td>", escape(value));
			}
		}
		writer.println("</tr>");
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
