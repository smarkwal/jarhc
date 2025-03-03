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

import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;
import org.jarhc.report.Report;
import org.jarhc.report.ReportFormat;
import org.jarhc.report.ReportSection;
import org.jarhc.report.ReportTable;
import org.jarhc.report.writer.ReportWriter;
import org.jarhc.utils.CompressUtils;
import org.jarhc.utils.JarHcException;
import org.jarhc.utils.Markdown;
import org.jarhc.utils.ResourceUtils;
import org.jarhc.utils.VersionUtils;

public class HtmlReportFormat implements ReportFormat {

	private static final String ISSUES_COLUMN = "Issues";

	private static final String RESOURCE = "/html-report-template.html";
	private static final String JSON_DATA_MARKER_START = "<!-- JSON REPORT DATA";
	private static final String JSON_DATA_MARKER_END = "-->";

	private final String template;

	public HtmlReportFormat() {

		// load template from resources
		try {
			this.template = ResourceUtils.getResourceAsString(RESOURCE, "UTF-8");
		} catch (IOException e) {
			throw new JarHcException("Resource not found: " + RESOURCE, e);
		}

	}

	@Override
	public void format(Report report, ReportWriter writer) {

		// split template
		int pos = template.indexOf("{REPORT}");
		String part1 = template.substring(0, pos);
		String part2 = template.substring(pos + 8);

		// add first part of template with HTML header
		String type = report.getType().name().toLowerCase(); // "scan" or "diff"
		part1 = part1.replace("{TYPE}", type);
		part1 = part1.replace("{TITLE}", escape(report.getTitle()));
		part1 = part1.replace("{VERSION}", escape(VersionUtils.getVersion()));
		writer.println(part1);

		// add table of contents
		formatToC(report, writer);

		// add individual sections
		List<ReportSection> sections = report.getSections();
		for (ReportSection section : sections) {
			formatSection(section, writer);
			writer.println();
		}

		// generate JSON data (if report is not a diff report)
		String jsonData = "";
		if (report.getType() != Report.Type.DIFF) {
			jsonData = generateJsonData(report);
		}
		part2 = part2.replace("{JSONDATA}", jsonData);

		// add second part of template
		writer.print(part2);
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
		writer.println("<section class=\"report-section\" id=\"%s\" title=\"%s\">", id, escape(title));

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

	private static String generateJsonData(Report report) {

		// get report data in JSON format
		String json = report.toJSON().toString();

		// compress and Base64-encode JSON data
		String data = CompressUtils.compressString(json);

		// split Base64 code into lines of max. 200 characters
		data = data.replaceAll("(.{200})", "$1\n");

		return JSON_DATA_MARKER_START + "\n" +
				data + "\n" +
				JSON_DATA_MARKER_END;
	}

	public static String extractJsonData(String text) {

		// find JSON data in HTML report
		int start = text.indexOf(JSON_DATA_MARKER_START);
		if (start < 0) {
			throw new JarHcException("JSON data not found in file.");
		}

		// find end marker
		int end = text.indexOf(JSON_DATA_MARKER_END, start);
		if (end < 0) {
			throw new JarHcException("Invalid JSON data in file.");
		}

		// extract Base64-encoded JSON data
		text = text.substring(start + JSON_DATA_MARKER_START.length(), end);

		// remove all whitespaces (including line breaks)
		text = text.replaceAll("\\s+", "");

		// decode and decompress JSON data
		return CompressUtils.decompressString(text);
	}

	private static String escape(String text) {
		// TODO: implement full HTML escaping
		text = text.replace("<", "&lt;");
		text = text.replace(">", "&gt;");
		text = text.replace("\"", "&quot;");
		return text;
	}

}
