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

import java.io.PrintWriter;
import java.io.StringWriter;

public interface ReportFormat {

	void format(Report report, PrintWriter out);

	default String format(Report report) {
		StringWriter writer = new StringWriter();
		PrintWriter out = new PrintWriter(writer);
		format(report, out);
		out.flush();
		return writer.toString();
	}

}
