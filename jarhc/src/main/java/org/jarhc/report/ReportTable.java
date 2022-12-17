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
		return rows;
	}

}
