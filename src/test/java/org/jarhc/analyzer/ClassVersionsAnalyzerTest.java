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

import static org.junit.jupiter.api.Assertions.*;

class ClassVersionsAnalyzerTest {

	@Test
	void test_analyze() {

		// prepare
		Classpath classpath = ClasspathBuilder.create(null)
				.addJarFile("a.jar")
				.addClassDef("a.A", 52, 0)
				.addJarFile("b.jar")
				.addClassDef("b.B1", 51, 0)
				.addClassDef("b.B2", 52, 0)
				.addJarFile("c.jar")
				.addClassDef("c.C", 48, 0)
				.addJarFile("d.jar") // no class files
				.build();

		// test
		ClassVersionsAnalyzer analyzer = new ClassVersionsAnalyzer();
		ReportSection section = analyzer.analyze(classpath);

		// assert
		assertNotNull(section);
		assertEquals("Class Versions", section.getTitle());
		assertEquals("Java class file format information.", section.getDescription());
		assertEquals(1, section.getContent().size());
		assertTrue(section.getContent().get(0) instanceof ReportTable);

		ReportTable table = (ReportTable) section.getContent().get(0);

		String[] columns = table.getColumns();
		assertEquals(2, columns.length);
		assertEquals("JAR file", columns[0]);
		assertEquals("Java version", columns[1]);

		List<String[]> rows = table.getRows();
		assertEquals(5, rows.size());

		String[] values1 = rows.get(0);
		assertEquals(2, values1.length);
		assertEquals("a.jar", values1[0]);
		assertEquals("Java 8 (1)", values1[1]);

		String[] values2 = rows.get(1);
		assertEquals(2, values2.length);
		assertEquals("b.jar", values2[0]);
		assertEquals("Java 8 (1), Java 7 (1)", values2[1]);

		String[] values3 = rows.get(2);
		assertEquals(2, values3.length);
		assertEquals("c.jar", values3[0]);
		assertEquals("Java 1.4 (1)", values3[1]);

		String[] values4 = rows.get(3);
		assertEquals(2, values4.length);
		assertEquals("d.jar", values4[0]);
		assertEquals("[no class files]", values4[1]);

		String[] values5 = rows.get(4);
		assertEquals(2, values5.length);
		assertEquals("Classpath", values5[0]);
		assertEquals("Java 8 (2), Java 7 (1), Java 1.4 (1)", values5[1]);

	}

}
