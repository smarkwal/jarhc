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

import static org.jarhc.TestUtils.assertValuesEquals;
import static org.jarhc.utils.StringUtils.joinLines;
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
				.addModuleInfo(ModuleInfo.forModuleName("b").addExports("b.x").addExports("b.y").addRequires("a").addRequires("java.base"))
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
		assertValuesEquals(columns, "JAR file", "Module name", "Definition", "Automatic", "Requires", "Exports");

		List<String[]> rows = table.getRows();
		assertEquals(4, rows.size());
		assertValuesEquals(rows.get(0), "a-core.jar", "a", "Manifest", "Yes", "-", "[all packages]");
		assertValuesEquals(rows.get(1), "b.jar", "b", "Module-Info", "No", joinLines("a", "java.base"), "b.x\nb.y");
		assertValuesEquals(rows.get(2), "C-1.0.1.jar", "c", "Auto-generated", "Yes", "-", "[all packages]");
		assertValuesEquals(rows.get(3), "d.jar", "d", "Auto-generated", "Yes", "-", "[all packages]");
	}

}
