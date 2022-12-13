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

class PackagesAnalyzerTest {

	@Test
	void test_analyze() {

		// prepare
		Classpath classpath = ClasspathBuilder.create(null)
				.addJarFile("a.jar")
				.addClassDef("a.A")
				.addClassDef("a.a1.A")
				.addClassDef("a.a2.A1")
				.addClassDef("a.a2.A2")
				.addJarFile("b.jar")
				.addClassDef("b.B")
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
		assertValuesEquals(columns, "JAR file", "Count", "Packages", "Issues");

		List<String[]> rows = table.getRows();
		assertEquals(2, rows.size());
		assertValuesEquals(rows.get(0), "a.jar", "3", "a (+2 subpackages)", "");
		assertValuesEquals(rows.get(1), "b.jar", "1", "b", "");
	}

	@Test
	void test_getParentPackageLength() {

		assertEquals(0, PackagesAnalyzer.getParentPackageLength("", ""));
		assertEquals(0, PackagesAnalyzer.getParentPackageLength("a", ""));
		assertEquals(0, PackagesAnalyzer.getParentPackageLength("", "a"));
		assertEquals(0, PackagesAnalyzer.getParentPackageLength("a", "x"));
		assertEquals(1, PackagesAnalyzer.getParentPackageLength("a", "a"));

		assertEquals(0, PackagesAnalyzer.getParentPackageLength("a.b.c", ""));
		assertEquals(1, PackagesAnalyzer.getParentPackageLength("a.b.c", "a"));
		assertEquals(2, PackagesAnalyzer.getParentPackageLength("a.b.c", "a.b"));
		assertEquals(3, PackagesAnalyzer.getParentPackageLength("a.b.c", "a.b.c"));

		assertEquals(0, PackagesAnalyzer.getParentPackageLength("a.b.c", "x.y.z"));
		assertEquals(1, PackagesAnalyzer.getParentPackageLength("a.b.c", "a.y.z"));
		assertEquals(2, PackagesAnalyzer.getParentPackageLength("a.b.c", "a.b.z"));

		assertEquals(0, PackagesAnalyzer.getParentPackageLength("a.b.c", "abc"));
		assertEquals(1, PackagesAnalyzer.getParentPackageLength("a.b.c", "a.bc"));
		assertEquals(0, PackagesAnalyzer.getParentPackageLength("a.b.c", "ab.c"));

		assertEquals(0, PackagesAnalyzer.getParentPackageLength("a", "ax"));
		assertEquals(1, PackagesAnalyzer.getParentPackageLength("a.b", "a.bx"));
		assertEquals(2, PackagesAnalyzer.getParentPackageLength("a.b.c", "a.b.cx"));

	}

}
