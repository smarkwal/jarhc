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
import java.util.stream.Collectors;
import org.json.JSONArray;
import org.json.JSONObject;

public class ReportSection {

	private final String title;
	private final String description;
	private final List<Object> content = new ArrayList<>();

	public ReportSection(String title, String description) {
		this.title = title;
		this.description = description;
	}

	public void add(String text) {
		content.add(text);
	}

	public void add(ReportTable table) {
		content.add(table);
	}

	public String getTitle() {
		return title;
	}

	public String getId() {
		return title.replaceAll("[^a-zA-Z0-9]", "");
	}

	public String getDescription() {
		return description;
	}

	public List<Object> getContent() {
		return content;
	}

	public boolean isEmpty() {

		if (content.isEmpty()) {
			return true;
		}

		// get all tables
		List<ReportTable> tables = content.stream().filter(ReportTable.class::isInstance).map(ReportTable.class::cast).collect(Collectors.toList());

		// special case: a section with text but no tables is not considered empty
		if (tables.isEmpty()) {
			return false;
		}

		// section is empty if all tables are empty
		return tables.stream().allMatch(t -> t.getRows().isEmpty());

	}

	public JSONObject toJSON() {
		JSONObject json = new JSONObject();
		json.put("title", title);
		json.put("description", description);
		JSONArray contentList = new JSONArray();
		for (Object item : content) {
			if (item instanceof ReportTable) {
				ReportTable table = (ReportTable) item;
				contentList.put(table.toJSON());
			} else {
				contentList.put(item);
			}
		}
		json.put("content", contentList);
		return json;
	}

	public static ReportSection fromJSON(JSONObject json) {
		String title = json.getString("title");
		String description = json.getString("description");
		ReportSection section = new ReportSection(title, description);
		JSONArray contentArray = json.getJSONArray("content");
		for (int i = 0; i < contentArray.length(); i++) {
			Object item = contentArray.get(i);
			if (item instanceof JSONObject) {
				JSONObject tableObject = (JSONObject) item;
				ReportTable table = ReportTable.fromJSON(tableObject);
				section.add(table);
			} else {
				String text = (String) item;
				section.add(text);
			}
		}
		return section;
	}

}
