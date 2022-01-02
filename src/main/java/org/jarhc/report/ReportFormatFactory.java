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

import java.util.HashMap;
import java.util.Map;
import org.jarhc.inject.Injector;
import org.jarhc.inject.InjectorException;
import org.jarhc.report.html.HtmlReportFormat;
import org.jarhc.report.list.ListReportFormat;
import org.jarhc.report.text.TextReportFormat;
import org.jarhc.utils.JarHcException;

/**
 * Factory for creating a {@link ReportFormat} given the type of report.
 * <p>
 * Supported types are:
 * <ul>
 * <li>"text": Text report in table format</li>
 * <li>"list": Text report in list format</li>
 * <li>"html": HTML report</li>
 * </ul>
 */
public class ReportFormatFactory {

	private static final Map<String, Class<? extends ReportFormat>> reportFormatClasses = new HashMap<>();

	static {
		reportFormatClasses.put("text", TextReportFormat.class);
		reportFormatClasses.put("list", ListReportFormat.class);
		reportFormatClasses.put("html", HtmlReportFormat.class);
	}

	private final Injector injector;

	public ReportFormatFactory(Injector injector) {
		this.injector = injector;
	}

	public static boolean isSupportedFormat(String format) {
		return reportFormatClasses.containsKey(format);
	}

	/**
	 * Get the report format for the given type.
	 *
	 * @param type Report format type ("text", "list", or "html").
	 * @return Report format
	 */
	public ReportFormat getReportFormat(String type) {
		if (type == null) throw new IllegalArgumentException("type");

		// try to find class for the given report type
		Class<? extends ReportFormat> reportFormatClass = reportFormatClasses.get(type);
		if (reportFormatClass == null) {
			String message = String.format("Unknown report format: '%s'", type);
			throw new JarHcException(message);
		}

		// try to create instance of report format
		try {
			return injector.createInstance(reportFormatClass);
		} catch (InjectorException e) {
			throw new JarHcException("Unable to create report format: " + type, e);
		}

	}

}
