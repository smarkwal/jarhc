/*
 * Copyright 2019 Stephan Markwalder
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

class DuplicateResourcesAnalyzerTest {

	@Test
	void test_analyze() {

		// prepare
		Classpath classpath = ClasspathBuilder.create()
				.addJarFile("a-1.jar").addResourceDef("a/b/C").addResourceDef("a/b/X")
				.addJarFile("a-2.jar").addResourceDef("a/b/C").addResourceDef("a/b/Y")
				.addJarFile("b.jar").addResourceDef("x/y/C")
				.build();

		// test
		DuplicateResourcesAnalyzer analyzer = new DuplicateResourcesAnalyzer();
		ReportSection section = analyzer.analyze(classpath);

		// assert
		assertNotNull(section);
		assertEquals("Duplicate Resources", section.getTitle());
		assertEquals("Resources found in multiple JAR files.", section.getDescription());
		assertEquals(1, section.getContent().size());
		assertTrue(section.getContent().get(0) instanceof ReportTable);

		ReportTable table = (ReportTable) section.getContent().get(0);

		String[] columns = table.getColumns();
		assertEquals(3, columns.length);
		assertEquals("Resource path", columns[0]);
		assertEquals("JAR files", columns[1]);
		assertEquals("Similarity", columns[2]);

		List<String[]> rows = table.getRows();
		assertEquals(1, rows.size());
		String[] values = rows.get(0);
		assertEquals(3, values.length);
		assertEquals("a/b/C", values[0]);
		assertEquals(joinLines("a-1.jar", "a-2.jar"), values[1]);
		assertEquals("[not implemented]", values[2]);

	}

}
