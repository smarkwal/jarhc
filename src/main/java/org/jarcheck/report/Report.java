package org.jarcheck.report;

import java.util.ArrayList;
import java.util.List;

public class Report {

	private final List<ReportSection> sections = new ArrayList<>();

	public Report() {

	}

	public void addSection(ReportSection section) {
		sections.add(section);
	}

	public List<ReportSection> getSections() {
		return sections;
	}

}
