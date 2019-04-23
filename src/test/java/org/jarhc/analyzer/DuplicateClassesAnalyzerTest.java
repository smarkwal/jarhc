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

import org.jarhc.Main;
import org.jarhc.env.JavaRuntime;
import org.jarhc.model.Classpath;
import org.jarhc.report.ReportSection;
import org.jarhc.report.ReportTable;
import org.jarhc.test.ClasspathBuilder;
import org.jarhc.test.JavaRuntimeMock;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.jarhc.utils.StringUtils.joinLines;
import static org.junit.jupiter.api.Assertions.*;

class DuplicateClassesAnalyzerTest {

	private DuplicateClassesAnalyzer analyzer = new DuplicateClassesAnalyzer();

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
		assertTrue(section.getContent().get(0) instanceof ReportTable);

		ReportTable table = (ReportTable) section.getContent().get(0);

		String[] columns = table.getColumns();
		assertEquals(3, columns.length);
		assertEquals("Class/Resource", columns[0]);
		assertEquals("Sources", columns[1]);
		assertEquals("Similarity", columns[2]);

		List<String[]> rows = table.getRows();
		assertEquals(4, rows.size());
		String[] values = rows.get(0);
		assertEquals(3, values.length);
		assertEquals("a.A", values[0]);
		assertEquals(joinLines("a.jar (Classpath)", "a.jar (Provided)"), values[1]);
		assertEquals("Exact copy", values[2]);

		values = rows.get(1);
		assertEquals(3, values.length);
		assertEquals("java.lang.String", values[0]);
		assertEquals(joinLines("r.jar (Classpath)", "Runtime (rt.jar)"), values[1]);
		assertEquals("Different API", values[2]);

		values = rows.get(2);
		assertEquals(3, values.length);
		assertEquals("z.Z", values[0]);
		assertEquals(joinLines("x.jar (Classpath)", "y.jar (Classpath)"), values[1]);
		assertEquals("Exact copy", values[2]);

		values = rows.get(3);
		assertEquals(3, values.length);
		assertEquals("z/Z.txt", values[0]);
		assertEquals(joinLines("x.jar", "y.jar"), values[1]);
		assertEquals("Different content", values[2]);

	}

	@Test
	void test_analyze_duplicate_classes() {

		// prepare
		Classpath classpath = ClasspathBuilder.create(null)
				.addJarFile("a-1.jar").addClassDef("a.b.C").addClassDef("a.b.X")
				.addJarFile("a-2.jar").addClassDef("a.b.C").addClassDef("a.b.Y")
				.addJarFile("b.jar").addClassDef("x.y.C")
				.build();

		// test
		ReportSection section = analyzer.analyze(classpath);

		// assert
		assertNotNull(section);
		assertEquals("Duplicate Classes", section.getTitle());
		assertEquals("Duplicate classes, shadowed classes, and duplicate resources.", section.getDescription());
		assertEquals(1, section.getContent().size());
		assertTrue(section.getContent().get(0) instanceof ReportTable);

		ReportTable table = (ReportTable) section.getContent().get(0);

		String[] columns = table.getColumns();
		assertEquals(3, columns.length);
		assertEquals("Class/Resource", columns[0]);
		assertEquals("Sources", columns[1]);
		assertEquals("Similarity", columns[2]);

		List<String[]> rows = table.getRows();
		assertEquals(1, rows.size());
		String[] values = rows.get(0);
		assertEquals(3, values.length);
		assertEquals("a.b.C", values[0]);
		assertEquals(joinLines("a-1.jar (Classpath)", "a-2.jar (Classpath)"), values[1]);
		assertEquals("Exact copy", values[2]);

	}

	@Test
	void test_analyze_duplicate_resources() {

		// prepare
		Classpath classpath = ClasspathBuilder.create(null)
				.addJarFile("a-1.jar").addResourceDef("a/b/C").addResourceDef("a/b/X")
				.addJarFile("a-2.jar").addResourceDef("a/b/C").addResourceDef("a/b/Y")
				.addJarFile("b.jar").addResourceDef("x/y/C")
				.build();

		// test
		ReportSection section = analyzer.analyze(classpath);

		// assert
		assertNotNull(section);
		assertEquals("Duplicate Classes", section.getTitle());
		assertEquals("Duplicate classes, shadowed classes, and duplicate resources.", section.getDescription());
		assertEquals(1, section.getContent().size());
		assertTrue(section.getContent().get(0) instanceof ReportTable);

		ReportTable table = (ReportTable) section.getContent().get(0);

		String[] columns = table.getColumns();
		assertEquals(3, columns.length);
		assertEquals("Class/Resource", columns[0]);
		assertEquals("Sources", columns[1]);
		assertEquals("Similarity", columns[2]);

		List<String[]> rows = table.getRows();
		assertEquals(1, rows.size());
		String[] values = rows.get(0);
		assertEquals(3, values.length);
		assertEquals("a/b/C", values[0]);
		assertEquals(joinLines("a-1.jar", "a-2.jar"), values[1]);
		assertEquals("Exact copy", values[2]);

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
		assertTrue(section.getContent().get(0) instanceof ReportTable);

		ReportTable table = (ReportTable) section.getContent().get(0);

		String[] columns = table.getColumns();
		assertEquals(3, columns.length);
		assertEquals("Class/Resource", columns[0]);
		assertEquals("Sources", columns[1]);
		assertEquals("Similarity", columns[2]);

		List<String[]> rows = table.getRows();
		assertEquals(1, rows.size());
		String[] values = rows.get(0);
		assertEquals(3, values.length);
		assertEquals("java.lang.String", values[0]);
		assertEquals(joinLines("a.jar (Classpath)", "Runtime (rt.jar)"), values[1]);
		assertEquals("Different API", values[2]);

	}

}
