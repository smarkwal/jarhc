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
import org.jarhc.utils.CompressUtils;
import org.jarhc.utils.Markdown;
import org.jarhc.utils.VersionUtils;
import org.slf4j.LoggerFactory;

public class HtmlReportFormat implements ReportFormat {

	private static final String ISSUES_COLUMN = "Issues";

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
		writer.println("<!--suppress CssUnusedSymbol, HttpUrlsUsage, SpellCheckingInspection, GrazieInspection, DeprecatedClassUsageInspection -->");
		writer.println("<html lang=\"en\">");
		formatHtmlHead(report, writer);
		formatHtmlBody(report, writer);

		if (report.getType() != Report.Type.DIFF) {
			formatJsonData(report, writer);
		}
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

		String type = report.getType().name().toLowerCase(); // "scan" or "diff"
		String cssClass = "report " + type + "-report";
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
		formatTableHeader(writer, columns);
		writer.println("</thead>");
		List<String[]> rows = table.getRows();
		writer.println("<tbody>");
		for (String[] values : rows) {
			formatTableRow(writer, columns, values);
		}
		writer.println("</tbody>");
		writer.println("</table>");
	}

	private static void formatTableHeader(ReportWriter writer, String[] columns) {
		writer.println("\t<tr class=\"report-table-header\">");
		for (String column : columns) {
			writer.println("\t\t<th>%s</th>", escape(column));
		}
		writer.println("\t</tr>");
	}

	private static void formatTableRow(ReportWriter writer, String[] columns, String[] values) {

		// prepare CSS class
		String cssClass = "report-table-row";
		if (Stream.of(values).anyMatch(Markdown::isDiff)) {
			cssClass += " diff";
		}

		writer.println("\t<tr class=\"%s\">", cssClass);
		for (int c = 0; c < columns.length; c++) {
			String column = columns[c];
			String value = values[c];
			formatTableCell(writer, column, value);
		}
		writer.println("\t</tr>");
	}

	private static void formatTableCell(ReportWriter writer, String column, String value) {

		// fast path: check if cell contains multiple lines or issues
		if (!value.contains("\n") && !column.equals(ISSUES_COLUMN)) {
			writer.println("\t\t<td>%s</td>", Markdown.toHtml(escape(value)));
			return;
		}

		// start cell
		writer.println("\t\t<td>");

		// split cell value into blocks (separated by empty line)
		String[] blocks = value.split("\n\n");

		// render and wrap each block individually
		for (String block : blocks) {

			// wrap block in div element
			// - with CSS class "report-issue" if it is in column "Issues"
			// - with CSS class "diff" if it contains a diff
			String cssClass = "report-text-block";
			if (column.equals(ISSUES_COLUMN)) { // mark issues
				cssClass += " report-issue";
			}
			if (Markdown.isDiff(block)) {
				cssClass += " diff";
			}
			writer.println("\t\t\t<div class=\"%s\">%s</div>", cssClass, Markdown.toHtml(escape(block)));
		}

		// end cell
		writer.println("\t\t</td>");
	}

	private static void formatJsonData(Report report, ReportWriter writer) {

		// get report data in JSON format
		String json = report.toJSON().toString();

		// compress and Base64-encode JSON data
		String data = CompressUtils.compressString(json);

		// split Base64 code into lines of max. 200 characters
		data = data.replaceAll("(.{200})", "$1\n");

		writer.println("<!-- JSON REPORT DATA");
		writer.println(data);
		writer.println("-->");
	}

	private static String escape(String text) {
		// TODO: implement full HTML escaping
		text = text.replace("<", "&lt;");
		text = text.replace(">", "&gt;");
		text = text.replace("\"", "&quot;");
		return text;
	}

}
