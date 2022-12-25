/*
 * Copyright 2022 Stephan Markwalder
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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;

class ReportTableTest {

	@Test
	void sortRows() {

		// prepare
		ReportTable table = new ReportTable("JSP file", "Issue");
		table.addRow("unknown.jar", "JSP file is unknown");
		table.addRow("empty.jar", "JSP file is empty");
		table.addRow("Invalid.jar", "JSP file is invalid");
		table.addRow("Classpath", "3 issues");

		// test
		table.sortRows();

		// assert
		List<String[]> rows = table.getRows();
		assertEquals("empty.jar", rows.get(0)[0]);
		assertEquals("Invalid.jar", rows.get(1)[0]);
		assertEquals("unknown.jar", rows.get(2)[0]);
		assertEquals("Classpath", rows.get(3)[0]);
	}

}