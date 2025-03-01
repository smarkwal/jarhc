/*
 * Copyright 2025 Stephan Markwalder
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

import java.io.IOException;
import org.jarhc.TestUtils;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

class ReportTest {

	@Test
	void fromJSON_toJSON() throws IOException {

		// prepare
		String resource = "/org/jarhc/report/test.json";
		String text = TestUtils.getResourceAsString(resource, "UTF-8");
		JSONObject json = new JSONObject(text);

		// test
		Report report = Report.fromJSON(json);
		json = report.toJSON();

		String text2 = json.toString(3);
		if (TestUtils.createResources()) {
			TestUtils.saveResource("test", resource, text2, "UTF-8");
			return;
		}

		// assert
		assertEquals(text, text2);
	}

}
