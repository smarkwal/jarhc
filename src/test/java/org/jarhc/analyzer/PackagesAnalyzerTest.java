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

import org.jarhc.model.Classpath;
import org.jarhc.report.ReportSection;
import org.jarhc.report.ReportTable;
import org.jarhc.test.ClasspathBuilder;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.jarhc.utils.StringUtils.joinLines;
import static org.junit.jupiter.api.Assertions.*;

class PackagesAnalyzerTest {

	@Test
	void test_analyze() {

		// prepare
		Classpath classpath = ClasspathBuilder.create()
				.addJarFile("a.jar")
				.addClassDef("a/A")
				.addClassDef("a/a1/A")
				.addClassDef("a/a2/A1")
				.addClassDef("a/a2/A2")
				.addJarFile("b.jar")
				.addClassDef("b/B")
				.addJarFile("c.jar")
				.build();

		// test
		PackagesAnalyzer analyzer = new PackagesAnalyzer();
		ReportSection section = analyzer.analyze(classpath);

		// assert
		assertNotNull(section);
		assertEquals("Packages", section.getTitle());
		assertEquals("List of packages per JAR file.", section.getDescription());
		assertEquals(1, section.getContent().size());
		assertTrue(section.getContent().get(0) instanceof ReportTable);

		ReportTable table = (ReportTable) section.getContent().get(0);

		String[] columns = table.getColumns();
		assertEquals(2, columns.length);
		assertEquals("JAR file", columns[0]);
		assertEquals("Packages", columns[1]);

		List<String[]> rows = table.getRows();
		assertEquals(2, rows.size());

		String[] values1 = rows.get(0);
		assertEquals(2, values1.length);
		assertEquals("a.jar", values1[0]);
		assertEquals(joinLines("a", "a.a1", "a.a2"), values1[1]);

		String[] values2 = rows.get(1);
		assertEquals(2, values2.length);
		assertEquals("b.jar", values2[0]);
		assertEquals("b", values2[1]);

	}

}
