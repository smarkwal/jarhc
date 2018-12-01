package org.jarcheck.report;

import java.io.PrintWriter;
import java.io.StringWriter;

public class ReportSection extends PrintWriter {

	private final String title;
	private final String description;

	public ReportSection(String title, String description) {
		super(new StringWriter());
		this.title = title;
		this.description = description;
	}

	public String getTitle() {
		return title;
	}

	public String getDescription() {
		return description;
	}

	public String getText() {
		StringWriter writer = (StringWriter) out;
		return writer.toString();
	}

}
