package org.jarcheck.report;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ReportSection {

	private final String title;
	private final String description;
	private final List<Object> content = new ArrayList<>();

	public ReportSection(String title, String description) {
		this.title = title;
		this.description = description;
	}

	public void add(CharSequence text) {
		content.add(text);
	}

	public void add(ReportTable table) {
		content.add(table);
	}

	public String getTitle() {
		return title;
	}

	public String getDescription() {
		return description;
	}

	public List<Object> getContent() {
		return Collections.unmodifiableList(content);
	}

}
