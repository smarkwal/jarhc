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

import static org.jarhc.TestUtils.assertValuesEquals;
import static org.jarhc.utils.StringUtils.joinLines;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;
import org.jarhc.Main;
import org.jarhc.app.Options;
import org.jarhc.env.JavaRuntime;
import org.jarhc.model.Classpath;
import org.jarhc.report.ReportSection;
import org.jarhc.report.ReportTable;
import org.jarhc.test.ClasspathBuilder;
import org.jarhc.test.JavaRuntimeMock;
import org.junit.jupiter.api.Test;

class DuplicateClassesAnalyzerTest {

	private final Options options = new Options();
	private final DuplicateClassesAnalyzer analyzer = new DuplicateClassesAnalyzer(options);

	@Test
	void test_analyze() {

		// prepare
		JavaRuntime javaRuntime = JavaRuntimeMock.getOracleRuntime();

		Classpath provided = ClasspathBuilder.create("Provided", javaRuntime)
				.addJarFile("a.jar").addClassDef("a.A").addClassDef("a.A3").addClassDef("java.lang.Integer").addResourceDef("a/A.txt").addResourceDef("a/A3.txt")
				.build();

		Classpath classpath = ClasspathBuilder.create(provided)
				.addJarFile("a.jar").addClassDef("a.A").addClassDef("a.A2").addResourceDef("a/A.txt").addResourceDef("a/A2.txt")
				.addJarFile("r.jar").addClassDef("java.lang.String")
				.addJarFile("x.jar").addClassDef("x.X").addClassDef("z.Z").addResourceDef("x/X.txt").addResourceDef("z/Z.txt", "checksum1")
				.addJarFile("y.jar").addClassDef("y.Y").addClassDef("z.Z").addResourceDef("y/Y.txt").addResourceDef("z/Z.txt", "checksum2")
				.build();

		// test
		ReportSection section = analyzer.analyze(classpath);

		// assert
		assertNotNull(section);
		assertEquals("Duplicate Classes", section.getTitle());
		assertEquals("Duplicate classes, shadowed classes, and duplicate resources.", section.getDescription());
		assertEquals(1, section.getContent().size());
		assertInstanceOf(ReportTable.class, section.getContent().get(0));

		ReportTable table = (ReportTable) section.getContent().get(0);

		String[] columns = table.getColumns();
		assertValuesEquals(columns, "Class/Resource", "Sources", "Similarity");

		List<String[]> rows = table.getRows();
		assertEquals(4, rows.size());
		assertValuesEquals(rows.get(0), "`a.A`", joinLines("a (Classpath)", "a (Provided)"), "Exact copy");
		assertValuesEquals(rows.get(1), "`java.lang.String`", joinLines("r (Classpath)", "Runtime"), "Different API\n(2/94 = 2% similar)");
		assertValuesEquals(rows.get(2), "`z.Z`", joinLines("x (Classpath)", "y (Classpath)"), "Exact copy");
		assertValuesEquals(rows.get(3), "`z/Z.txt`", joinLines("x", "y"), "Different content");
	}

	@Test
	void test_analyze_duplicate_classes() {

		// prepare
		Classpath classpath = ClasspathBuilder.create(null)
				.addJarFile("a.jar").addClassDef("a.b.C").addClassDef("a.b.X")
				.addJarFile("b.jar").addClassDef("a.b.C").addClassDef("a.b.Y")
				.addJarFile("c.jar").addClassDef("x.y.C")
				.build();

		// test
		ReportSection section = analyzer.analyze(classpath);

		// assert
		assertNotNull(section);
		assertEquals("Duplicate Classes", section.getTitle());
		assertEquals("Duplicate classes, shadowed classes, and duplicate resources.", section.getDescription());
		assertEquals(1, section.getContent().size());
		assertInstanceOf(ReportTable.class, section.getContent().get(0));

		ReportTable table = (ReportTable) section.getContent().get(0);

		String[] columns = table.getColumns();
		assertValuesEquals(columns, "Class/Resource", "Sources", "Similarity");

		List<String[]> rows = table.getRows();
		assertEquals(1, rows.size());
		assertValuesEquals(rows.get(0), "`a.b.C`", joinLines("a (Classpath)", "b (Classpath)"), "Exact copy");
	}

	@Test
	void test_analyze_duplicate_resources() {

		// prepare
		Classpath classpath = ClasspathBuilder.create(null)
				.addJarFile("a.jar").addResourceDef("a/b/C").addResourceDef("a/b/X")
				.addJarFile("b.jar").addResourceDef("a/b/C").addResourceDef("a/b/Y")
				.addJarFile("c.jar").addResourceDef("x/y/C")
				.build();

		// test
		ReportSection section = analyzer.analyze(classpath);

		// assert
		assertNotNull(section);
		assertEquals("Duplicate Classes", section.getTitle());
		assertEquals("Duplicate classes, shadowed classes, and duplicate resources.", section.getDescription());
		assertEquals(1, section.getContent().size());
		assertInstanceOf(ReportTable.class, section.getContent().get(0));

		ReportTable table = (ReportTable) section.getContent().get(0);

		String[] columns = table.getColumns();
		assertValuesEquals(columns, "Class/Resource", "Sources", "Similarity");

		List<String[]> rows = table.getRows();
		assertEquals(1, rows.size());
		assertValuesEquals(rows.get(0), "`a/b/C`", joinLines("a", "b"), "Exact copy");
	}

	@Test
	void test_analyze_shadowed_classes() {

		// prepare
		JavaRuntime javaRuntime = JavaRuntimeMock.getOracleRuntime();
		String mainClassName = Main.class.getName();
		Classpath classpath = ClasspathBuilder.create(javaRuntime)
				.addJarFile("a.jar").addClassDef("a.A").addClassDef("java.lang.String")
				.addJarFile("b.jar").addClassDef("b.B").addClassDef(mainClassName)
				.build();

		// test
		ReportSection section = analyzer.analyze(classpath);

		// assert
		assertNotNull(section);
		assertEquals("Duplicate Classes", section.getTitle());
		assertEquals("Duplicate classes, shadowed classes, and duplicate resources.", section.getDescription());
		assertEquals(1, section.getContent().size());
		assertInstanceOf(ReportTable.class, section.getContent().get(0));

		ReportTable table = (ReportTable) section.getContent().get(0);

		String[] columns = table.getColumns();
		assertValuesEquals(columns, "Class/Resource", "Sources", "Similarity");

		List<String[]> rows = table.getRows();
		assertEquals(1, rows.size());
		assertValuesEquals(rows.get(0), "`java.lang.String`", joinLines("a (Classpath)", "Runtime"), "Different API\n(2/94 = 2% similar)");
	}

	@Test
	void calculateApiDiff() {

		int result = DuplicateClassesAnalyzer.calculateApiDiff(
				new String[0],
				new String[0]
		);
		assertEquals(0, result);

		result = DuplicateClassesAnalyzer.calculateApiDiff(
				new String[] { "line1" },
				new String[0]
		);
		assertEquals(1, result);

		result = DuplicateClassesAnalyzer.calculateApiDiff(
				new String[0],
				new String[] { "line1" }
		);
		assertEquals(1, result);

		result = DuplicateClassesAnalyzer.calculateApiDiff(
				new String[] { "line1" },
				new String[] { "line1" }
		);
		assertEquals(0, result);

		result = DuplicateClassesAnalyzer.calculateApiDiff(
				new String[] { "line1", "line2", "line3" },
				new String[] { "line1", "line2", "line3" }
		);
		assertEquals(0, result);

		result = DuplicateClassesAnalyzer.calculateApiDiff(
				new String[] { "line1", "line2", "line3" },
				new String[] { "line2" }
		);
		assertEquals(2, result);

		result = DuplicateClassesAnalyzer.calculateApiDiff(
				new String[] { "line2" },
				new String[] { "line1", "line2", "line3" }
		);
		assertEquals(2, result);

		result = DuplicateClassesAnalyzer.calculateApiDiff(
				new String[] { "line1", "line2", "line3" },
				new String[] { "line4", "line5", "line6" }
		);
		assertEquals(3, result);

	}
}
