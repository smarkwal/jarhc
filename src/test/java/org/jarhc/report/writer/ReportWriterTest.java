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

package org.jarhc.report.writer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ReportWriterTest {

	private final TestReportWriter reportWriter = new TestReportWriter();

	@Test
	void test_print_text() {

		// test
		reportWriter.print("Hello");

		// assert
		String result = reportWriter.getText();
		assertEquals("Hello", result);

	}

	@Test
	void test_print_format() {

		// test
		reportWriter.print("Hello %s", "World");

		// assert
		String result = reportWriter.getText();
		assertEquals("Hello World", result);

	}

	@Test
	void test_println() {

		// test
		reportWriter.println();

		// assert
		String result = reportWriter.getText();
		assertEquals(System.lineSeparator(), result);

	}

	@Test
	void test_println_text() {

		// test
		reportWriter.println("Hello");

		// assert
		String result = reportWriter.getText();
		assertEquals("Hello" + System.lineSeparator(), result);

	}

	@Test
	void test_println_format() {

		// test
		reportWriter.println("Hello %s", "World");

		// assert
		String result = reportWriter.getText();
		assertEquals("Hello World" + System.lineSeparator(), result);

	}

	@Test
	void test_close() {

		// assume
		assertFalse(reportWriter.isClosed());

		// test
		reportWriter.close();

		// assert
		assertTrue(reportWriter.isClosed());

	}

	private static class TestReportWriter implements ReportWriter {

		private StringBuffer buffer = new StringBuffer();

		@Override
		public void print(String text) {
			buffer.append(text);
		}

		String getText() {
			return buffer.toString();
		}

		@Override
		public void close() {
			buffer = null;
		}

		boolean isClosed() {
			return buffer == null;
		}

	}

}
