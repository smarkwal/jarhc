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

public class ReportSection {

	private final String title;
	private final String description;
	private final List<Object> content = new ArrayList<>();

	/**
	 * Parent section, or null if this is a top-level section.
	 */
	private ReportSection parent;

	public ReportSection(String title, String description) {
		this.title = title;
		this.description = description;
	}

	public void add(ReportSection section) {
		content.add(section);
		section.parent = this;
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
		String id = title.replaceAll("[^a-zA-Z0-9]", "");
		if (parent != null) {
			id = parent.getId() + "-" + id;
		}
		return id;
	}

	public String getDescription() {
		return description;
	}

	public List<Object> getContent() {
		return content;
	}

	public int getLevel() {
		if (parent == null) {
			return 0;
		}
		return parent.getLevel() + 1;
	}

	public boolean isEmpty() {

		for (Object item : content) {

			if (item instanceof ReportSection) {
				ReportSection section = (ReportSection) item;

				// check if subsection is empty
				if (!section.isEmpty()) {
					return false;
				}
			} else if (item instanceof ReportTable) {
				ReportTable table = (ReportTable) item;

				// check if table is empty
				if (!table.isEmpty()) {
					return false;
				}
			} else {

				// text is never considered empty
				return false;
			}
		}

		return true;
	}

	void removeEmptySections() {
		content.removeIf(item -> {
			if (item instanceof ReportSection) {
				ReportSection section = (ReportSection) item;
				section.removeEmptySections();
				return section.isEmpty();
			}
			return false;
		});
	}

	public JSONObject toJSON() {
		JSONObject json = new JSONObject();
		json.put("title", title);
		json.put("description", description);
		JSONArray contentList = new JSONArray();
		for (Object item : content) {
			if (item instanceof ReportSection) {
				ReportSection section = (ReportSection) item;
				contentList.put(section.toJSON());
			} else if (item instanceof ReportTable) {
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
		JSONArray content = json.getJSONArray("content");
		for (int i = 0; i < content.length(); i++) {
			Object item = content.get(i);
			if (item instanceof JSONObject) {
				JSONObject object = (JSONObject) item;
				if (object.has("title")) {
					ReportSection subsection = ReportSection.fromJSON(object);
					section.add(subsection);
				} else {
					ReportTable table = ReportTable.fromJSON(object);
					section.add(table);
				}
			} else {
				String text = (String) item;
				section.add(text);
			}
		}
		return section;
	}

}
