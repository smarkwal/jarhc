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

class JarFilesListAnalyzerTest {

	@Test
	void test_analyze() {

		// prepare
		Classpath classpath = ClasspathBuilder.create()
				.addJarFile("a.jar", 128)
				.addClassDef("a/A")
				.addJarFile("b.jar", 4096)
				.addClassDef("b/B1")
				.addClassDef("b/B2")
				.addJarFile("c.jar", 24000)
				.addRelease(9)
				.addRelease(11)
				.addClassDef("c/C")
				.addJarFile("d.jar", 1234567) // no class files
				.build();

		// test
		JarFilesListAnalyzer analyzer = new JarFilesListAnalyzer();
		ReportSection section = analyzer.analyze(classpath);

		// assert
		assertNotNull(section);
		assertEquals("JAR Files", section.getTitle());
		assertEquals("List of JAR files found in classpath.", section.getDescription());
		assertEquals(1, section.getContent().size());
		assertTrue(section.getContent().get(0) instanceof ReportTable);

		ReportTable table = (ReportTable) section.getContent().get(0);

		String[] columns = table.getColumns();
		assertEquals(4, columns.length);
		assertEquals("JAR file", columns[0]);
		assertEquals("Size", columns[1]);
		assertEquals("Class files", columns[2]);
		assertEquals("Multi-release", columns[3]);

		List<String[]> rows = table.getRows();
		assertEquals(5, rows.size());

		String[] values1 = rows.get(0);
		assertEquals(4, values1.length);
		assertEquals("a.jar", values1[0]);
		assertEquals("128 B", values1[1]);
		assertEquals("1", values1[2]);
		assertEquals("No", values1[3]);

		String[] values2 = rows.get(1);
		assertEquals(4, values2.length);
		assertEquals("b.jar", values2[0]);
		assertEquals("4.00 KB", values2[1]);
		assertEquals("2", values2[2]);
		assertEquals("No", values2[3]);

		String[] values3 = rows.get(2);
		assertEquals(4, values3.length);
		assertEquals("c.jar", values3[0]);
		assertEquals("23.4 KB", values3[1]);
		assertEquals("1", values3[2]);
		assertEquals("Yes (Java 9, Java 11)", values3[3]);

		String[] values4 = rows.get(3);
		assertEquals(4, values4.length);
		assertEquals("d.jar", values4[0]);
		assertEquals("1.18 MB", values4[1]);
		assertEquals("0", values4[2]);
		assertEquals("No", values4[3]);

		String[] values5 = rows.get(4);
		assertEquals(4, values5.length);
		assertEquals("Classpath", values5[0]);
		assertEquals("1.20 MB", values5[1]);
		assertEquals("4", values5[2]);
		assertEquals("-", values5[3]);

	}

}
