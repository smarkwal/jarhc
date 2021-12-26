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
import org.jarhc.report.ReportSection;
import org.jarhc.report.ReportTable;
import org.jarhc.test.ClasspathBuilder;
import org.junit.jupiter.api.Test;

class JarDependenciesAnalyzerTest {

	@Test
	void test_analyze() {

		// prepare
		Classpath classpath = ClasspathBuilder.create(null)
				.addJarFile("a.jar")
				.addClassDef("a.A").addClassRef("b.B1").addClassRef("c.C").addClassRef("x.X")
				.addJarFile("b.jar")
				.addClassDef("b.B1")
				.addClassDef("b.B2").addClassRef("c.C").addClassRef("b.B1")
				.addJarFile("c.jar")
				.addClassDef("c.C")
				.addJarFile("d.jar")
				.build();

		// test
		JarDependenciesAnalyzer analyzer = new JarDependenciesAnalyzer();
		ReportSection section = analyzer.analyze(classpath);

		// assert
		assertNotNull(section);
		assertEquals("JAR Dependencies", section.getTitle());
		assertEquals("Dependencies between JAR files.", section.getDescription());
		assertEquals(1, section.getContent().size());
		assertTrue(section.getContent().get(0) instanceof ReportTable);

		ReportTable table = (ReportTable) section.getContent().get(0);

		String[] columns = table.getColumns();
		assertValuesEquals(columns, "JAR file", "Uses", "Used by");

		List<String[]> rows = table.getRows();
		assertEquals(4, rows.size());
		assertValuesEquals(rows.get(0), "a.jar", joinLines("b.jar", "c.jar"), "[none]");
		assertValuesEquals(rows.get(1), "b.jar", "c.jar", "a.jar");
		assertValuesEquals(rows.get(2), "c.jar", "[none]", joinLines("a.jar", "b.jar"));
		assertValuesEquals(rows.get(3), "d.jar", "[none]", "[none]");
	}

}
