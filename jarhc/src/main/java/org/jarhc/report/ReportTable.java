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

package org.jarhc.report;

import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

public class ReportTable {

	private final String[] columns;
	private final List<String[]> rows = new ArrayList<>();

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
		return rows;
	}

	public void sortRows() {
		rows.sort(ReportTable::compareRows);
	}

	private static int compareRows(String[] row1, String[] row2) {

		// get values from first column
		String value1 = row1[0];
		String value2 = row2[0];

		// special handling for "Classpath" row:
		// row should always be at the bottom
		if (value1.equals("Classpath")) return 1;
		if (value2.equals("Classpath")) return -1;

		// first compare case-insensitive, then case-sensitive
		int diff = value1.compareToIgnoreCase(value2);
		if (diff == 0) {
			diff = value1.compareTo(value2);
		}
		return diff;
	}

	public JSONObject toJSON() {
		JSONObject json = new JSONObject();
		json.put("columns", new JSONArray(columns));
		JSONArray rowsArray = new JSONArray();
		for (String[] row : rows) {
			JSONArray rowArray = new JSONArray();
			for (String value : row) {
				rowArray.put(value);
			}
			rowsArray.put(rowArray);
		}
		json.put("rows", rowsArray);
		return json;
	}

}
