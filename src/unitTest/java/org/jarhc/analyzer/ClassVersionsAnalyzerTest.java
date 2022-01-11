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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.jarhc.model.Classpath;
import org.jarhc.report.ReportSection;
import org.jarhc.report.ReportTable;
import org.jarhc.test.ClasspathBuilder;
import org.junit.jupiter.api.Test;

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
				.addRelease(9)
				.addRelease(11)
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
		assertValuesEquals(columns, "JAR file", "Multi-release", "Class files by Java version");

		List<String[]> rows = table.getRows();
		assertEquals(5, rows.size());
		assertValuesEquals(rows.get(0), "a.jar", "No", "Java 8 (1)");
		assertValuesEquals(rows.get(1), "b.jar", "Yes (Java 9, Java 11)", "Java 8 (1), Java 7 (1)");
		assertValuesEquals(rows.get(2), "c.jar", "No", "Java 1.4 (1)");
		assertValuesEquals(rows.get(3), "d.jar", "No", "[no class files]");
		assertValuesEquals(rows.get(4), "Classpath", "-", "Java 8 (2), Java 7 (1), Java 1.4 (1)");
	}

}
