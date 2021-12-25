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

class ModulesAnalyzerTest {

	@Test
	void test_analyze() {

		// prepare
		Classpath classpath = ClasspathBuilder.create(null)
				.addJarFile("a-core.jar")
				.addModuleInfo(ModuleInfo.forModuleName("a").setAutomatic(true))
				.addJarFile("b.jar")
				.addModuleInfo(ModuleInfo.forModuleName("b").addExport("b.x").addExport("b.y").addRequire("a").addRequire("java.base"))
				.addJarFile("C-1.0.1.jar")
				.addJarFile("d.jar")
				.build();

		// test
		ModulesAnalyzer analyzer = new ModulesAnalyzer();
		ReportSection section = analyzer.analyze(classpath);

		// assert
		assertNotNull(section);
		assertEquals("Modules", section.getTitle());
		assertEquals("List of Java Modules found in classpath.", section.getDescription());
		assertEquals(1, section.getContent().size());
		assertTrue(section.getContent().get(0) instanceof ReportTable);

		ReportTable table = (ReportTable) section.getContent().get(0);

		String[] columns = table.getColumns();
		assertEquals(6, columns.length);
		assertEquals("JAR file", columns[0]);
		assertEquals("Module name", columns[1]);
		assertEquals("Definition", columns[2]);
		assertEquals("Automatic", columns[3]);
		assertEquals("Requires", columns[4]);
		assertEquals("Exports", columns[5]);

		List<String[]> rows = table.getRows();
		assertEquals(4, rows.size());

		String[] values1 = rows.get(0);
		assertEquals(6, values1.length);
		assertEquals("a-core.jar", values1[0]);
		assertEquals("a", values1[1]);
		assertEquals("Manifest", values1[2]);
		assertEquals("Yes", values1[3]);
		assertEquals("-", values1[4]);
		assertEquals("[all packages]", values1[5]);

		String[] values2 = rows.get(1);
		assertEquals(6, values2.length);
		assertEquals("b.jar", values2[0]);
		assertEquals("b", values2[1]);
		assertEquals("Module-Info", values2[2]);
		assertEquals("No", values2[3]);
		assertEquals("a\njava.base", values2[4]);
		assertEquals("b.x\nb.y", values2[5]);

		String[] values3 = rows.get(2);
		assertEquals(6, values3.length);
		assertEquals("C-1.0.1.jar", values3[0]);
		assertEquals("c", values3[1]);
		assertEquals("Auto-generated", values3[2]);
		assertEquals("Yes", values3[3]);
		assertEquals("-", values3[4]);
		assertEquals("[all packages]", values3[5]);

		String[] values4 = rows.get(3);
		assertEquals(6, values4.length);
		assertEquals("d.jar", values4[0]);
		assertEquals("d", values4[1]);
		assertEquals("Auto-generated", values4[2]);
		assertEquals("Yes", values4[3]);
		assertEquals("-", values4[4]);
		assertEquals("[all packages]", values4[5]);

	}

}
