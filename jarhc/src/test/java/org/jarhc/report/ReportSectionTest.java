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

package org.jarhc.report;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class ReportSectionTest {

	@Test
	void add_CharSequence() {
		// prepare
		ReportSection section = new ReportSection("JAR Files", "List of JAR files.");
		// test
		section.add("Lorem ipsum.");
		// assert
		assertEquals(1, section.getContent().size());
		assertEquals("Lorem ipsum.", section.getContent().get(0));
	}

	@Test
	void add_ReportTable() {
		// prepare
		ReportSection section = new ReportSection("JAR Files", "List of JAR files.");
		ReportTable table = new ReportTable("JAR file", "File size");
		// test
		section.add(table);
		// assert
		assertEquals(1, section.getContent().size());
		assertSame(table, section.getContent().get(0));
	}

	@Test
	void getTitle() {
		// prepare
		ReportSection section = new ReportSection("JAR Files", "List of JAR files.");
		// test
		String result = section.getTitle();
		// assert
		assertEquals("JAR Files", result);
	}

	@Test
	void getId() {
		// prepare
		ReportSection section = new ReportSection("JAR Files", "List of JAR files.");
		// test
		String result = section.getId();
		// assert
		assertEquals("JARFiles", result);
	}

	@Test
	void getDescription() {
		// prepare
		ReportSection section = new ReportSection("JAR Files", "List of JAR files.");
		// test
		String result = section.getDescription();
		// assert
		assertEquals("List of JAR files.", result);
	}

	@Test
	void getContent() {
		// prepare
		ReportSection section = new ReportSection("JAR Files", "List of JAR files.");
		section.add("File sizes.");
		ReportTable table1 = new ReportTable("JAR file", "File size");
		section.add(table1);
		section.add("Java classes.");
		ReportTable table2 = new ReportTable("JAR file", "Java classes");
		section.add(table2);
		// test
		List<Object> content = section.getContent();
		// assert
		assertEquals(4, content.size());
		assertEquals("File sizes.", content.get(0));
		assertSame(table1, content.get(1));
		assertEquals("Java classes.", content.get(2));
		assertSame(table2, content.get(3));
	}

	@Test
	void isEmpty_empty() {
		// prepare
		ReportSection section = new ReportSection("JAR Files", "List of JAR files.");
		// test
		boolean result = section.isEmpty();
		// assert
		assertTrue(result);
	}

	@Test
	void isEmpty_onlyText() {
		// prepare
		ReportSection section = new ReportSection("JAR Files", "List of JAR files.");
		section.add("Lorem ipsum.");
		// test
		boolean result = section.isEmpty();
		// assert
		assertFalse(result);
	}

	@Test
	void isEmpty_emptyTable() {
		// prepare
		ReportSection section = new ReportSection("JAR Files", "List of JAR files.");
		section.add("Lorem ipsum.");
		section.add(new ReportTable("JAR file", "File size"));
		// test
		boolean result = section.isEmpty();
		// assert
		assertTrue(result);
	}

	@Test
	void isEmpty_nonEmptyTable() {
		// prepare
		ReportSection section = new ReportSection("JAR Files", "List of JAR files.");
		section.add("Lorem ipsum.");
		ReportTable table = new ReportTable("JAR file", "File size");
		section.add(table);
		table.addRow("a.jar", "12 KB");
		// test
		boolean result = section.isEmpty();
		// assert
		assertFalse(result);
	}

}