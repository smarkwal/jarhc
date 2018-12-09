package org.jarhc.report;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ReportTable {

	private String[] columns;
	private List<String[]> rows = new ArrayList<>();

	public ReportTable(String... columns) {
		this.columns = columns;
	}

	public void addRow(String... values) {
		if (values.length > columns.length) throw new IllegalArgumentException("values");
		rows.add(values);
	}

	public String[] getColumns() {
		return columns;
	}

	public List<String[]> getRows() {
		return Collections.unmodifiableList(rows);
	}

}
