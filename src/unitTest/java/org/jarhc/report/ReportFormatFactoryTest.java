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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.jarhc.inject.Injector;
import org.jarhc.inject.InjectorException;
import org.jarhc.report.html.HtmlReportFormat;
import org.jarhc.report.list.ListReportFormat;
import org.jarhc.report.text.TextReportFormat;
import org.jarhc.utils.JarHcException;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.mockito.Mockito;

class ReportFormatFactoryTest {

	private final Injector injector = new Injector();

	@TestFactory
	Collection<DynamicTest> isSupportedFormat() {
		List<DynamicTest> tests = new ArrayList<>();
		tests.add(DynamicTest.dynamicTest("html", () -> isSupportedFormat("html", true)));
		tests.add(DynamicTest.dynamicTest("text", () -> isSupportedFormat("text", true)));
		tests.add(DynamicTest.dynamicTest("list", () -> isSupportedFormat("list", true)));
		tests.add(DynamicTest.dynamicTest("pdf", () -> isSupportedFormat("pdf", false)));
		return tests;
	}

	private void isSupportedFormat(String type, boolean expectedResult) {

		// test
		boolean result = ReportFormatFactory.isSupportedFormat(type);

		// assert
		assertEquals(result, expectedResult);

	}

	@Test
	void test_getReportFormat_null() {

		// prepare
		ReportFormatFactory factory = new ReportFormatFactory(injector);

		// test and assert
		assertThrows(
				IllegalArgumentException.class,
				() -> factory.getReportFormat(null),
				"type"
		);
	}

	@Test
	void test_getReportFormat_empty() {

		// prepare
		ReportFormatFactory factory = new ReportFormatFactory(injector);

		// test and assert
		assertThrows(
				JarHcException.class,
				() -> factory.getReportFormat(""),
				"Unknown report format: ''."
		);

	}

	@Test
	void test_getReportFormat_unknown() {

		// prepare
		ReportFormatFactory factory = new ReportFormatFactory(injector);

		// test and assert
		assertThrows(
				JarHcException.class,
				() -> factory.getReportFormat("pdf"),
				"Unknown report format: 'pdf'."
		);

	}

	@Test
	void test_getReportFormat_text() {

		// prepare
		ReportFormatFactory factory = new ReportFormatFactory(injector);

		// test
		ReportFormat format = factory.getReportFormat("text");

		// assert
		assertTrue(format instanceof TextReportFormat);

	}

	@Test
	void test_getReportFormat_list() {

		// prepare
		ReportFormatFactory factory = new ReportFormatFactory(injector);

		// test
		ReportFormat format = factory.getReportFormat("list");

		// assert
		assertTrue(format instanceof ListReportFormat);

	}

	@Test
	void test_getReportFormat_html() {

		// prepare
		ReportFormatFactory factory = new ReportFormatFactory(injector);

		// test
		ReportFormat format = factory.getReportFormat("html");

		// assert
		assertTrue(format instanceof HtmlReportFormat);

	}

	@Test
	@SuppressWarnings("unchecked")
	void getReportFormat_throwsRuntimeException_onInjectorException() throws InjectorException {

		// prepare injector
		Injector injector = Mockito.mock(Injector.class);
		when(injector.createInstance(any(Class.class))).thenThrow(new InjectorException("test"));

		// prepare
		ReportFormatFactory factory = new ReportFormatFactory(injector);

		// test
		assertThrows(
				RuntimeException.class,
				() -> factory.getReportFormat("html"),
				"Unable to create report format: html"
		);

	}

}
