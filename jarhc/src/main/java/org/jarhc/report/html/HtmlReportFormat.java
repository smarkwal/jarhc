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
import java.util.stream.Stream;
import org.jarhc.report.Report;
import org.jarhc.report.ReportFormat;
import org.jarhc.report.ReportSection;
import org.jarhc.report.ReportTable;
import org.jarhc.report.writer.ReportWriter;
import org.jarhc.utils.Markdown;
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
		writer.println("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">");

		// set JarHC with version as generator
		writer.println("<meta name=\"generator\" content=\"JarHC " + VersionUtils.getVersion() + "\">");

		// include CSS styles
		String style = styleProvider.getStyle();
		if (style != null) {
			writer.println("<style>");
			writer.println(style);
			writer.println("</style>");
		}

		writer.println("</head>");
	}

	private void formatHtmlBody(Report report, ReportWriter writer) {

		String cssClass = "report";
		String type = report.getType();
		if (type != null) {
			cssClass += " " + type + "-report";
		}
		writer.println("<body class=\"%s\">", cssClass);
		writer.println();

		writer.println("<header class=\"no-print\">");
		writer.println("\t<div id=\"controls\"></div>");
		writer.println("\t<div id=\"generator\">Generated with <a href=\"http://jarhc.org\" target=\"_blank\">JarHC " + VersionUtils.getVersion() + "</a></div>");
		writer.println("</header>");
		writer.println();
		writer.println("<main>");
		writer.println();

		// add optional title
		String title = report.getTitle();
		if (title != null) {
			writer.println("<h1 class=\"report-title\">%s</h1>", escape(title));
			writer.println();
		}

		// add table of contents
		formatToC(report, writer);

		// add individual sections
		List<ReportSection> sections = report.getSections();
		for (ReportSection section : sections) {
			formatSection(section, writer);
			writer.println();
		}

		writer.println("</main>");

		// include JavaScript code
		String script = styleProvider.getScript();
		if (script != null) {
			writer.println("<script>");
			writer.println(script);
			writer.println("</script>");
		}

		writer.println("</body>");
	}

	private void formatToC(Report report, ReportWriter writer) {
		writer.println("<nav>");
		writer.println("<h2 class=\"report-toc-title\">Table of Contents</h2>");
		writer.println("<ul class=\"report-toc\">");
		List<ReportSection> sections = report.getSections();
		for (ReportSection section : sections) {
			formatToC(section, writer);
		}
		writer.println("</ul>");
		writer.println("</nav>");
		writer.println();
	}

	private static void formatToC(ReportSection section, ReportWriter writer) {
		String title = section.getTitle();
		String id = section.getId();
		// if section has subsections ...
		boolean nested = section.getContent().stream().anyMatch(ReportSection.class::isInstance);
		if (nested) {
			writer.println("<li class=\"report-toc-item\"><a href=\"#%s\">%s</a>", id, escape(title));
			// create nested list
			writer.println("<ul class=\"report-toc-%d\">", section.getLevel() + 1);
			List<Object> content = section.getContent();
			for (Object item : content) {
				if (item instanceof ReportSection) {
					ReportSection subsection = (ReportSection) item;
					formatToC(subsection, writer);
				}
			}
			writer.println("</ul>");
			writer.println("</li>");
		} else {
			writer.println("<li class=\"report-toc-item\"><a href=\"#%s\">%s</a></li>", id, escape(title));
		}
	}

	private void formatSection(ReportSection section, ReportWriter writer) {

		String title = section.getTitle();
		String id = section.getId();
		int level = section.getLevel();
		String description = section.getDescription();
		List<Object> content = section.getContent();

		// section start
		writer.println("<section class=\"report-section\" id=\"%s\">", id);

		// format header
		String heading = "h" + (level + 2); // root sections use h2
		writer.println("<%s class=\"report-section-title\">%s</%s>", heading, escape(title), heading);
		if (description != null) {
			String cssClass = "report-section-description";
			if (Markdown.isDiff(description)) {
				cssClass += " diff";
			}
			writer.println("<p class=\"%s\">%s</p>", cssClass, Markdown.toHtml(escape(description)));
		}

		// format content
		for (Object item : content) {
			if (item instanceof ReportSection) {
				ReportSection subsection = (ReportSection) item;
				formatSection(subsection, writer);
			} else if (item instanceof ReportTable) {
				ReportTable table = (ReportTable) item;
				formatTable(table, writer);
			} else {
				String value = item.toString();
				String cssClass = "report-content";
				if (Markdown.isDiff(value)) {
					cssClass += " diff";
				}
				writer.println("<p class=\"%s\">%s</p>", cssClass, Markdown.toHtml(escape(value)));
			}
		}

		writer.println("</section>");

	}

	private void formatTable(ReportTable table, ReportWriter writer) {
		writer.println("<table class=\"report-table\">");
		String[] columns = table.getColumns();
		writer.println("<thead>");
		printTableRow(writer, columns, true);
		writer.println("</thead>");
		List<String[]> rows = table.getRows();
		writer.println("<tbody>");
		for (String[] values : rows) {
			printTableRow(writer, values, false);
		}
		writer.println("</tbody>");
		writer.println("</table>");
	}

	private static void printTableRow(ReportWriter writer, String[] values, boolean header) {

		// prepare CSS class
		String cssClass = header ? "report-table-header" : "report-table-row";
		if (Stream.of(values).anyMatch(Markdown::isDiff)) {
			cssClass += " diff";
		}

		writer.println("\t<tr class=\"%s\">", cssClass);
		for (String value : values) {
			if (header) {
				writer.println("\t\t<th>%s</th>", escape(value));
			} else {
				writer.println("\t\t<td>%s</td>", Markdown.toHtml(escape(value)));
			}
		}
		writer.println("\t</tr>");
	}

	private static String escape(String text) {
		// TODO: implement full HTML escaping
		text = text.replace("<", "&lt;");
		text = text.replace(">", "&gt;");
		text = text.replace("\"", "&quot;");
		return text;
	}

}
