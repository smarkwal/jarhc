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
import org.jarhc.utils.DateTimeUtils;
import org.jarhc.utils.VersionUtils;
import org.json.JSONArray;
import org.json.JSONObject;

public class Report {

	private String version = VersionUtils.getVersion();
	private long timestamp = DateTimeUtils.getTimestamp();

	private String title = "JAR Health Check Report";
	private Type type = Type.SCAN;
	private final List<ReportSection> sections = new ArrayList<>();

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		if (type == null) throw new IllegalArgumentException("type");
		this.type = type;
	}

	public void addSection(ReportSection section) {
		sections.add(section);
	}

	public void removeSection(ReportSection section) {
		sections.remove(section);
	}

	public List<ReportSection> getSections() {
		return new ArrayList<>(sections);
	}

	public JSONObject toJSON() {
		JSONObject json = new JSONObject();
		json.put("version", version);
		json.put("timestamp", timestamp);
		json.put("title", title);
		json.put("type", type);
		List<JSONObject> sectionList = new ArrayList<>();
		for (ReportSection section : sections) {
			sectionList.add(section.toJSON());
		}
		json.put("sections", sectionList);
		return json;
	}

	public static Report fromJSON(JSONObject json) {
		Report report = new Report();
		report.version = json.getString("version");
		report.timestamp = json.getLong("timestamp");
		report.title = json.getString("title");
		report.type = Type.valueOf(json.getString("type"));
		JSONArray sectionList = json.getJSONArray("sections");
		for (int i = 0; i < sectionList.length(); i++) {
			JSONObject sectionObject = sectionList.getJSONObject(i);
			ReportSection section = ReportSection.fromJSON(sectionObject);
			report.addSection(section);
		}
		return report;
	}

	public void removeEmptySections() {
		for (ReportSection section : sections) {
			section.removeEmptySections();
		}
		sections.removeIf(ReportSection::isEmpty);
	}

	public enum Type {
		SCAN,
		DIFF
	}

}
