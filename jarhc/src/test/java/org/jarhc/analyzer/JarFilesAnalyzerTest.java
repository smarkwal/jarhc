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
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;
import org.jarhc.model.Classpath;
import org.jarhc.report.ReportSection;
import org.jarhc.report.ReportTable;
import org.jarhc.test.ClasspathBuilder;
import org.junit.jupiter.api.Test;

class JarFilesAnalyzerTest {

	@Test
	void test_analyze() {

		// prepare
		Classpath classpath = ClasspathBuilder.create(null)
				.addJarFile("a.jar", "org.jarhc:a:1.0", 128)
				.addClassDef("a.A")
				.addResourceDef("a/a.txt")
				.addJarFile("b.jar", "org.jarhc:b:2.0", 4096)
				.addClassDef("b.B1")
				.addClassDef("b.B2")
				.addJarFile("c-3.2.1-SNAPSHOT-test.jar", null, 24000)
				.addRelease(9)
				.addRelease(11)
				.addClassDef("c.C")
				.addJarFile("d.jar", null, 1234567) // no class files
				.build();

		// test
		JarFilesAnalyzer analyzer = new JarFilesAnalyzer();
		ReportSection section = analyzer.analyze(classpath);

		// assert
		assertNotNull(section);
		assertEquals("JAR Files", section.getTitle());
		assertEquals("List of JAR files found in classpath.", section.getDescription());
		assertEquals(1, section.getContent().size());
		assertInstanceOf(ReportTable.class, section.getContent().get(0));

		ReportTable table = (ReportTable) section.getContent().get(0);

		String[] columns = table.getColumns();
		assertValuesEquals(columns, "Artifact", "Version", "Source", "Size", "Classes", "Resources", "Checksum (SHA-1)", "Coordinates");

		List<String[]> rows = table.getRows();
		assertEquals(5, rows.size());
		assertValuesEquals(rows.get(0), "a", "1.0", "[[org.jarhc:a:1.0]]", "128 B", "1", "1", "0a4c26b96ef92cceb7c2c7c0e19c808baeb8d696", "[[org.jarhc:a:1.0]]");
		assertValuesEquals(rows.get(1), "b", "2.0", "[[org.jarhc:b:2.0]]", "4.00 KB", "2", "0", "1271677b4f55e181e4c8192f0edf87bb3ff9fde5", "[[org.jarhc:b:2.0]]");
		assertValuesEquals(rows.get(2), "c-test", "3.2.1-SNAPSHOT", "c-3.2.1-SNAPSHOT-test.jar", "23.4 KB", "1", "0", "3ee0f133a54067f5200caafa674437460648ce16", "[unknown]");
		assertValuesEquals(rows.get(3), "d", "[unknown]", "d.jar", "1.18 MB", "0", "0", "458dea9210ea076f4c422be47390a9f2c0fcb0f8", "[unknown]");
		assertValuesEquals(rows.get(4), "Classpath", "-", "-", "1.20 MB", "4", "1", "-", "-");
	}

}
