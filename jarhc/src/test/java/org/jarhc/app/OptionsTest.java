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

import java.util.List;
import org.junit.jupiter.api.Test;

class OptionsTest {

	private final Options options = new Options();

	@Test
	void test_getReportFormat() {

		List<String> result = options.getReportFiles();
		assertEquals(0, result.size());

		options.addReportFile("report.html");
		result = options.getReportFiles();
		assertEquals(1, result.size());
		assertEquals("report.html", result.get(0));

		options.addReportFile("report.txt");
		result = options.getReportFiles();
		assertEquals(2, result.size());
		assertEquals("report.html", result.get(0));
		assertEquals("report.txt", result.get(1));

	}

}
