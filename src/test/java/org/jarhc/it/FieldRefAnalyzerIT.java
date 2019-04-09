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

package org.jarhc.it;

import org.jarhc.TestUtils;
import org.jarhc.analyzer.FieldRefAnalyzer;
import org.jarhc.env.JavaRuntime;
import org.jarhc.loader.ClasspathLoader;
import org.jarhc.loader.LoaderBuilder;
import org.jarhc.model.Classpath;
import org.jarhc.model.FieldRef;
import org.jarhc.report.ReportSection;
import org.jarhc.report.ReportTable;
import org.jarhc.test.JavaRuntimeMock;
import org.jarhc.utils.StringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junitpioneer.jupiter.TempDirectory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(TempDirectory.class)
class FieldRefAnalyzerIT {

	private final JavaRuntime javaRuntime = JavaRuntimeMock.getOracleRuntime();
	private final ClasspathLoader classpathLoader = LoaderBuilder.create().withParentClassLoader(javaRuntime).buildClasspathLoader();
	private FieldRefAnalyzer analyzer = new FieldRefAnalyzer(false);

	@Test
	void test_fieldrefs(@TempDirectory.TempDir Path tempDir) throws IOException {

		// prepare
		File jarFile1 = TestUtils.getResourceAsFile("/FieldRefAnalyzerIT/a.jar", tempDir);
		Classpath classpath = classpathLoader.load(Collections.singletonList(jarFile1));

		List<FieldRef> fieldRefs = classpath.getJarFile("a.jar").getClassDef("a.A").getFieldRefs();

		String list = fieldRefs.stream().map(FieldRef::getDisplayName).sorted().collect(StringUtils.joinLines());

		String expectedList = StringUtils.joinLines(
				"int b.B.existingField",
				"int b.B.intField",
				"int b.B.nonFinalField",
				"int b.B.nonStaticField",
				"int b.B.nonStaticSuperField",
				"int b.B.publicField",
				"int b.B.superField",
				"int[] b.B.arrayField",
				"static b.E b.E.E3",
				"static int b.B.staticField",
				"static int b.B.staticSuperField",
				"static java.lang.Boolean java.lang.Boolean.TRUE",
				"static java.lang.Object b.B.interfaceObjectField",
				"static java.lang.String java.io.File.separator"
		);
		assertEquals(expectedList, list);
	}

	@Test
	void test_compatible(@TempDirectory.TempDir Path tempDir) throws IOException {

		// prepare
		File jarFile1 = TestUtils.getResourceAsFile("/FieldRefAnalyzerIT/a.jar", tempDir);
		File jarFile2 = TestUtils.getResourceAsFile("/FieldRefAnalyzerIT/b-1.jar", tempDir);
		Classpath classpath = classpathLoader.load(Arrays.asList(jarFile1, jarFile2));

		// test
		ReportSection section = analyzer.analyze(classpath);

		// assert
		List<Object> content = section.getContent();
		assertEquals(1, content.size());
		Object object = content.get(0);
		assertTrue(object instanceof ReportTable);
		ReportTable table = (ReportTable) object;
		List<String[]> rows = table.getRows();
		assertEquals(0, rows.size());

	}

	@Test
	void test_incompatible(@TempDirectory.TempDir Path tempDir) throws IOException {

		// prepare
		File jarFile1 = TestUtils.getResourceAsFile("/FieldRefAnalyzerIT/a.jar", tempDir);
		File jarFile2 = TestUtils.getResourceAsFile("/FieldRefAnalyzerIT/b-2.jar", tempDir);
		Classpath classpath = classpathLoader.load(Arrays.asList(jarFile1, jarFile2));

		// test
		ReportSection section = analyzer.analyze(classpath);

		// assert
		List<Object> content = section.getContent();
		assertEquals(1, content.size());
		Object object = content.get(0);
		assertTrue(object instanceof ReportTable);
		ReportTable table = (ReportTable) object;
		List<String[]> rows = table.getRows();
		assertEquals(1, rows.size());

		String[] values = rows.get(0);
		assertEquals(2, values.length);
		assertEquals("a.jar", values[0]);

		String expectedMessage = StringUtils.joinLines(
				"Field not found: int b.B.existingField",
				"\u2022 b.B (field not found)",
				"\u2022 b.I (field not found)",
				"\u2022 java.lang.Object (field not found)",
				"\u2022 b.S (field not found)",
				"",
				"Incompatible field type: int b.B.intField -> public boolean intField",
				"\u2022 b.B (field found)",
				"",
				"Field not found: static java.lang.Object b.B.interfaceObjectField",
				"\u2022 b.B (field not found)",
				"\u2022 b.I (field not found)",
				"\u2022 java.lang.Object (field not found)",
				"\u2022 b.S (field not found)",
				"",
				"Write access to final field: int b.B.nonFinalField -> public final int nonFinalField",
				"\u2022 b.B (field found)",
				"",
				"Instance access to static field: int b.B.nonStaticField -> public static int nonStaticField",
				"\u2022 b.B (field found)",
				"",
				"Instance access to static field: int b.B.nonStaticSuperField -> public static int nonStaticSuperField",
				"\u2022 b.B (field not found)",
				"\u2022 b.I (field not found)",
				"\u2022 java.lang.Object (field not found)",
				"\u2022 b.S (field found)",
				"",
				"Illegal access from a.A: int b.B.publicField -> private int publicField",
				"\u2022 b.B (field found)",
				"",
				"Static access to instance field: static int b.B.staticField -> public int staticField",
				"\u2022 b.B (field found)",
				"",
				"Static access to instance field: static int b.B.staticSuperField -> public int staticSuperField",
				"\u2022 b.B (field not found)",
				"\u2022 b.I (field not found)",
				"\u2022 java.lang.Object (field not found)",
				"\u2022 b.S (field found)",
				"",
				"Field not found: int b.B.superField",
				"\u2022 b.B (field not found)",
				"\u2022 b.I (field not found)",
				"\u2022 java.lang.Object (field not found)",
				"\u2022 b.S (field not found)",
				"",
				"Field not found: static b.E b.E.E3",
				"\u2022 b.E (field not found)",
				"\u2022 java.lang.Enum (field not found)",
				"\u2022 java.lang.Comparable (field not found)",
				"\u2022 java.lang.Object (field not found)",
				"\u2022 java.io.Serializable (field not found)"
		);
		assertEquals(expectedMessage, values[1]);

	}

	@Test
	void test_reportOwnerClassNotFound_false(@TempDirectory.TempDir Path tempDir) throws IOException {

		// prepare
		File jarFile1 = TestUtils.getResourceAsFile("/FieldRefAnalyzerIT/a.jar", tempDir);
		Classpath classpath = classpathLoader.load(Collections.singletonList(jarFile1));

		// test
		ReportSection section = analyzer.analyze(classpath);

		// assert
		List<Object> content = section.getContent();
		assertEquals(1, content.size());
		Object object = content.get(0);
		assertTrue(object instanceof ReportTable);
		ReportTable table = (ReportTable) object;
		List<String[]> rows = table.getRows();
		assertEquals(0, rows.size());

	}

	@Test
	void test_reportOwnerClassNotFound_true(@TempDirectory.TempDir Path tempDir) throws IOException {

		// prepare: analyzer reporting missing owner classes
		analyzer = new FieldRefAnalyzer(true);

		// prepare
		File jarFile1 = TestUtils.getResourceAsFile("/FieldRefAnalyzerIT/a.jar", tempDir);
		Classpath classpath = classpathLoader.load(Collections.singletonList(jarFile1));

		// test
		ReportSection section = analyzer.analyze(classpath);

		// assert
		List<Object> content = section.getContent();
		assertEquals(1, content.size());
		Object object = content.get(0);
		assertTrue(object instanceof ReportTable);
		ReportTable table = (ReportTable) object;
		List<String[]> rows = table.getRows();
		assertEquals(1, rows.size());

		String[] values = rows.get(0);
		assertEquals(2, values.length);
		assertEquals("a.jar", values[0]);

		String expectedMessage = StringUtils.joinLines(
				"Field not found: int[] b.B.arrayField",
				"\u2022 b.B (owner class not found)",
				"",
				"Field not found: int b.B.existingField",
				"\u2022 b.B (owner class not found)",
				"",
				"Field not found: int b.B.intField",
				"\u2022 b.B (owner class not found)",
				"",
				"Field not found: static java.lang.Object b.B.interfaceObjectField",
				"\u2022 b.B (owner class not found)",
				"",
				"Field not found: int b.B.nonFinalField",
				"\u2022 b.B (owner class not found)",
				"",
				"Field not found: int b.B.nonStaticField",
				"\u2022 b.B (owner class not found)",
				"",
				"Field not found: int b.B.nonStaticSuperField",
				"\u2022 b.B (owner class not found)",
				"",
				"Field not found: int b.B.publicField",
				"\u2022 b.B (owner class not found)",
				"",
				"Field not found: static int b.B.staticField",
				"\u2022 b.B (owner class not found)",
				"",
				"Field not found: static int b.B.staticSuperField",
				"\u2022 b.B (owner class not found)",
				"",
				"Field not found: int b.B.superField",
				"\u2022 b.B (owner class not found)",
				"",
				"Field not found: static b.E b.E.E3",
				"\u2022 b.E (owner class not found)"
		);
		assertEquals(expectedMessage, values[1]);
	}

}
