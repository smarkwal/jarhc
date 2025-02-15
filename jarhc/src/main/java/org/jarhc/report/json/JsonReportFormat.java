/*
 * Copyright 2025 Stephan Markwalder
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

package org.jarhc.report.json;

import org.jarhc.report.Report;
import org.jarhc.report.ReportFormat;
import org.jarhc.report.writer.ReportWriter;
import org.json.JSONObject;

public class JsonReportFormat implements ReportFormat {

	@Override
	public void format(Report report, ReportWriter writer) {
		JSONObject json = report.toJSON();
		String text = json.toString(3);
		writer.print(text);
	}

}
