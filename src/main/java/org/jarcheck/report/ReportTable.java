package org.jarcheck.report;

import java.util.ArrayList;
import java.util.Arrays;
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

	@Override
	public String toString() {
		int[] columnSize = calculateColumnSizes(columns, rows);
		StringBuilder buffer = new StringBuilder();
		appendLine(buffer, columns, columnSize);
		appendSeparator(buffer, columnSize);
		for (String[] values : rows) {
			appendLine(buffer, values, columnSize);
		}
		return buffer.toString();
	}

	private static int[] calculateColumnSizes(String[] columns, List<String[]> rows) {
		int[] columnSize = new int[columns.length];
		for (int i = 0; i < columnSize.length; i++) {
			int length = columns[i].length();
			columnSize[i] = Math.max(columnSize[i], length);
		}
		for (String[] values : rows) {
			for (int i = 0; i < values.length; i++) {
				int length = values[i].length();
				columnSize[i] = Math.max(columnSize[i], length);
			}
		}
		return columnSize;
	}

	private static int calculateTableSize(int[] columnSize) {
		return Arrays.stream(columnSize).sum() + (columnSize.length - 1) * 3;
	}

	private static void appendLine(StringBuilder buffer, String[] values, int[] columnSize) {
		for (int i = 0; i < values.length; i++) {
			if (i > 0) buffer.append(" | ");
			String column = values[i];
			buffer.append(column);
			if (i < values.length - 1) appendSpaces(buffer, columnSize[i] - column.length());
		}
		buffer.append(System.lineSeparator());
	}

	private static void appendSeparator(StringBuilder buffer, int[] columnSize) {
		for (int i = 0; i < columnSize.length; i++) {
			if (i > 0) buffer.append("-+-");
			append(buffer, "-", columnSize[i]);
		}
		buffer.append(System.lineSeparator());
	}

	private static void appendSpaces(StringBuilder buffer, int count) {
		append(buffer, " ", count);
	}

	private static void append(StringBuilder buffer, String text, int count) {
		while (count > 0) {
			buffer.append(text);
			count--;
		}
	}

}
