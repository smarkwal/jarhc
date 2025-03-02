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

package org.jarhc.report.html;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.jarhc.report.AbstractReportFormatTest;
import org.jarhc.utils.JarHcException;
import org.junit.jupiter.api.Test;

class HtmlReportFormatTest extends AbstractReportFormatTest {

	public HtmlReportFormatTest() {
		super(new HtmlReportFormat());
	}

	@Test
	void extractJsonData() {

		// prepare
		String text = "<html><head><title>Test</title></head><body><h1>Test</h1></body>\n" +
				"<!-- JSON REPORT DATA\n" +
				"H4sIAAAAAAAAAKtWKsks\n" +
				"yUlVslIKSS0uUdJRSspP\n" +
				"qQTybDIM7UAiNvpAhlIt\n" +
				"AB9ComonAAAA\n" +
				"-->\n" +
				"</html>";

		// test
		String result = HtmlReportFormat.extractJsonData(text);

		// assert
		assertEquals("{\"title\":\"Test\",\"body\":\"<h1>Test</h1>\"}", result);
	}

	@Test
	void extractJsonData_startMarkerNotFound() {

		// prepare
		String text = "<html><head><title>Test</title></head><body><h1>Test</h1></body></html>";

		// test
		Exception result = assertThrows(JarHcException.class, () -> HtmlReportFormat.extractJsonData(text));
		assertEquals("JSON data not found in file.", result.getMessage());
	}

	@Test
	void extractJsonData_endMarkerNotFound() {

		// prepare
		String text = "<html><head><title>Test</title></head><body><h1>Test</h1></body>\n" +
				"<!-- JSON REPORT DATA\n" +
				"SomeBase64EncodedData\n" +
				"</html>";

		// test
		Exception result = assertThrows(JarHcException.class, () -> HtmlReportFormat.extractJsonData(text));
		assertEquals("Invalid JSON data in file.", result.getMessage());
	}

}
