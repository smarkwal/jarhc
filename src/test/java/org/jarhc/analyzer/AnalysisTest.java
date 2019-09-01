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

package org.jarhc.analyzer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;
import org.jarhc.model.Classpath;
import org.jarhc.report.Report;
import org.jarhc.report.ReportSection;
import org.jarhc.test.ClasspathBuilder;
import org.junit.jupiter.api.Test;

class AnalysisTest {

	@Test
	void test_run() {

		// prepare
		Analysis analysis = new Analysis(
				new TestAnalyzer("Title 1", "Description 1"),
				new TestAnalyzer("Title 2", "Description 2")
		);
		Classpath classpath = ClasspathBuilder.create(null).addJarFile("a.jar").addClassDef("a.A").build();
		Report report = new Report();

		// test
		analysis.run(classpath, report);

		// assert
		List<ReportSection> sections = report.getSections();
		assertNotNull(sections);
		assertEquals(2, sections.size());
		ReportSection section1 = sections.get(0);
		assertNotNull(section1);
		assertEquals("Title 1", section1.getTitle());
		assertEquals("Description 1", section1.getDescription());
		ReportSection section2 = sections.get(1);
		assertNotNull(section2);
		assertEquals("Title 2", section2.getTitle());
		assertEquals("Description 2", section2.getDescription());

	}

	private static class TestAnalyzer extends Analyzer {

		private final String title;
		private final String description;

		private TestAnalyzer(String title, String description) {
			this.title = title;
			this.description = description;
		}

		@Override
		public ReportSection analyze(Classpath classpath) {
			return new ReportSection(title, description);
		}

	}

}
