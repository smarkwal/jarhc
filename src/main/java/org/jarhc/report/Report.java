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
import java.util.Collections;
import java.util.List;

public class Report {

	private String title;
	private final List<ReportSection> sections = new ArrayList<>();

	public Report() {

	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void addSection(ReportSection section) {
		sections.add(section);
	}

	public List<ReportSection> getSections() {
		return Collections.unmodifiableList(sections);
	}

}
