/*
 * Copyright 2019 Stephan Markwalder
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

package org.jarhc.app;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class OptionsTest {

	private Options options = new Options();

	@Test
	void test_getReportFormat() {

		String result = options.getReportFormat();
		assertEquals("text", result);

		options.setReportFile("report.html");
		result = options.getReportFormat();
		assertEquals("html", result);

		options.setReportFile("report.txt");
		result = options.getReportFormat();
		assertEquals("text", result);

		options.setReportFile("report.out");
		result = options.getReportFormat();
		assertEquals("text", result);

		options.setReportFormat("html");
		result = options.getReportFormat();
		assertEquals("html", result);

		options.setReportFormat("text");
		result = options.getReportFormat();
		assertEquals("text", result);

		options.setReportFormat("csv");
		result = options.getReportFormat();
		assertEquals("csv", result);

	}

}
