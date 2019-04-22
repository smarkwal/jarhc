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

import org.jarhc.report.html.HtmlReportFormat;
import org.jarhc.report.list.ListReportFormat;
import org.jarhc.report.text.TextReportFormat;

/**
 * Factory for creating a {@link ReportFormat} given the type of report.
 * <p>
 * Supported types are:
 * <ul>
 * <li>"text": Text report</li>
 * <li>"html": HTML report</li>
 * </ul>
 */
public class ReportFormatFactory {

	public static boolean isSupportedFormat(String format) {
		return format.equals("text") || format.equals("list") || format.equals("html");
	}

	/**
	 * Get the report format for the given type.
	 *
	 * @param type Report format type ("text", "list", or "html").
	 * @return Report format
	 */
	public ReportFormat getReportFormat(String type) {
		if (type == null) throw new IllegalArgumentException("type");
		switch (type) {
			case "text":
				return new TextReportFormat();
			case "list":
				return new ListReportFormat();
			case "html":
				return new HtmlReportFormat();
			default:
				String message = String.format("Unknown report format: '%s'", type);
				throw new IllegalArgumentException(message);
		}
	}

}
