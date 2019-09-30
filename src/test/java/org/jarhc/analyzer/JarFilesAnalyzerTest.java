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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.jarhc.model.Classpath;
import org.jarhc.model.ModuleInfo;
import org.jarhc.report.ReportSection;
import org.jarhc.report.ReportTable;
import org.jarhc.test.ClasspathBuilder;
import org.junit.jupiter.api.Test;

class JarFilesAnalyzerTest {

	@Test
	void test_analyze() {

		// prepare
		Classpath classpath = ClasspathBuilder.create(null)
				.addJarFile("a.jar", 128)
				.addClassDef("a.A")
				.addResourceDef("a/a.txt")
				.addJarFile("b.jar", 4096)
				.addModuleInfo(ModuleInfo.forModuleName("b").addExport("b").addRequire("java.base"))
				.addClassDef("b.B1")
				.addClassDef("b.B2")
				.addJarFile("c.jar", 24000)
				.addRelease(9)
				.addRelease(11)
				.addClassDef("c.C")
				.addJarFile("d.jar", 1234567) // no class files
				.build();

		// test
		JarFilesAnalyzer analyzer = new JarFilesAnalyzer();
		ReportSection section = analyzer.analyze(classpath);

		// assert
		assertNotNull(section);
		assertEquals("JAR Files", section.getTitle());
		assertEquals("List of JAR files found in classpath.", section.getDescription());
		assertEquals(1, section.getContent().size());
		assertTrue(section.getContent().get(0) instanceof ReportTable);

		ReportTable table = (ReportTable) section.getContent().get(0);

		String[] columns = table.getColumns();
		assertEquals(8, columns.length);
		assertEquals("JAR file", columns[0]);
		assertEquals("Size", columns[1]);
		assertEquals("Classes", columns[2]);
		assertEquals("Resources", columns[3]);
		assertEquals("Multi-release", columns[4]);
		assertEquals("Module", columns[5]);
		assertEquals("Checksum (SHA-1)", columns[6]);
		assertEquals("Artifact coordinates", columns[7]);

		List<String[]> rows = table.getRows();
		assertEquals(5, rows.size());

		String[] values1 = rows.get(0);
		assertEquals(8, values1.length);
		assertEquals("a.jar", values1[0]);
		assertEquals("128 B", values1[1]);
		assertEquals("1", values1[2]);
		assertEquals("1", values1[3]);
		assertEquals("No", values1[4]);
		assertEquals("No", values1[5]);
		assertEquals("0a4c26b96ef92cceb7c2c7c0e19c808baeb8d696", values1[6]);
		assertEquals("org.jarhc:0a4c2:1.0:jar", values1[7]);

		String[] values2 = rows.get(1);
		assertEquals(8, values2.length);
		assertEquals("b.jar", values2[0]);
		assertEquals("4.00 KB", values2[1]);
		assertEquals("2", values2[2]);
		assertEquals("0", values2[3]);
		assertEquals("No", values2[4]);
		assertEquals("Yes (b)", values2[5]);
		assertEquals("1271677b4f55e181e4c8192f0edf87bb3ff9fde5", values2[6]);
		assertEquals("org.jarhc:12716:1.0:jar", values2[7]);

		String[] values3 = rows.get(2);
		assertEquals(8, values3.length);
		assertEquals("c.jar", values3[0]);
		assertEquals("23.4 KB", values3[1]);
		assertEquals("1", values3[2]);
		assertEquals("0", values3[3]);
		assertEquals("Yes (Java 9, Java 11)", values3[4]);
		assertEquals("No", values3[5]);
		assertEquals("fa2798370b42e2616cb0d374b2ae4be836439077", values3[6]);
		assertEquals("org.jarhc:fa279:1.0:jar", values3[7]);

		String[] values4 = rows.get(3);
		assertEquals(8, values4.length);
		assertEquals("d.jar", values4[0]);
		assertEquals("1.18 MB", values4[1]);
		assertEquals("0", values4[2]);
		assertEquals("0", values4[3]);
		assertEquals("No", values4[4]);
		assertEquals("No", values4[5]);
		assertEquals("458dea9210ea076f4c422be47390a9f2c0fcb0f8", values4[6]);
		assertEquals("org.jarhc:458de:1.0:jar", values4[7]);

		String[] values5 = rows.get(4);
		assertEquals(8, values5.length);
		assertEquals("Classpath", values5[0]);
		assertEquals("1.20 MB", values5[1]);
		assertEquals("4", values5[2]);
		assertEquals("1", values5[3]);
		assertEquals("-", values5[4]);
		assertEquals("-", values5[5]);
		assertEquals("-", values5[6]);
		assertEquals("-", values5[7]);

	}

}
