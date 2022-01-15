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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.jarhc.TestUtils;
import org.jarhc.analyzer.BinaryCompatibilityAnalyzer;
import org.jarhc.env.JavaRuntime;
import org.jarhc.loader.ClasspathLoader;
import org.jarhc.loader.LoaderBuilder;
import org.jarhc.model.Classpath;
import org.jarhc.model.FieldRef;
import org.jarhc.report.ReportSection;
import org.jarhc.report.ReportTable;
import org.jarhc.test.JavaRuntimeMock;
import org.jarhc.test.TextUtils;
import org.jarhc.utils.StringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class FieldRefAnalyzerTest {

	private final JavaRuntime javaRuntime = JavaRuntimeMock.getOracleRuntime();
	private final ClasspathLoader classpathLoader = LoaderBuilder.create().withParentClassLoader(javaRuntime).buildClasspathLoader();
	private BinaryCompatibilityAnalyzer analyzer = new BinaryCompatibilityAnalyzer(false, false);

	@Test
	void test_fieldrefs(@TempDir Path tempDir) throws IOException {

		// prepare
		File jarFile1 = TestUtils.getResourceAsFile("/org/jarhc/it/FieldRefAnalyzerTest/a.jar", tempDir);
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
	void test_compatible(@TempDir Path tempDir) throws IOException {

		// prepare
		File jarFile1 = TestUtils.getResourceAsFile("/org/jarhc/it/FieldRefAnalyzerTest/a.jar", tempDir);
		File jarFile2 = TestUtils.getResourceAsFile("/org/jarhc/it/FieldRefAnalyzerTest/b-1.jar", tempDir);
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
	void test_incompatible(@TempDir Path tempDir) throws IOException {

		// prepare
		File jarFile1 = TestUtils.getResourceAsFile("/org/jarhc/it/FieldRefAnalyzerTest/a.jar", tempDir);
		File jarFile2 = TestUtils.getResourceAsFile("/org/jarhc/it/FieldRefAnalyzerTest/b-2.jar", tempDir);
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

		String value = values[1];
		String expectedValue = TestUtils.getResourceAsString("/org/jarhc/it/FieldRefAnalyzerTest/result.txt", "UTF-8");

		// normalize
		value = TextUtils.toUnixLineSeparators(value);
		expectedValue = TextUtils.toUnixLineSeparators(expectedValue);

		assertEquals(expectedValue, value);
	}

	@Test
	void test_reportOwnerClassNotFound_false(@TempDir Path tempDir) throws IOException {

		// prepare
		File jarFile1 = TestUtils.getResourceAsFile("/org/jarhc/it/FieldRefAnalyzerTest/a.jar", tempDir);
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
				"a.A",
				"\u2022 Class not found: b.B (package not found)",
				"\u2022 Class not found: b.E (package not found)"
		);
		assertEquals(expectedMessage, values[1]);

	}

	@Test
	void test_reportOwnerClassNotFound_true(@TempDir Path tempDir) throws IOException {

		// prepare: analyzer reporting missing owner classes
		analyzer = new BinaryCompatibilityAnalyzer(false, true);

		// prepare
		File jarFile1 = TestUtils.getResourceAsFile("/org/jarhc/it/FieldRefAnalyzerTest/a.jar", tempDir);
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
				"a.A",
				"\u2022 Class not found: b.B (package not found)",
				"\u2022 Class not found: b.E (package not found)",
				"\u2022 Method not found: void b.B.<init>()",
				"> b.B (owner class not found)",
				"\u2022 Field not found: int[] b.B.arrayField",
				"> b.B (owner class not found)",
				"\u2022 Field not found: int b.B.existingField",
				"> b.B (owner class not found)",
				"\u2022 Field not found: int b.B.intField",
				"> b.B (owner class not found)",
				"\u2022 Field not found: static java.lang.Object b.B.interfaceObjectField",
				"> b.B (owner class not found)",
				"\u2022 Field not found: int b.B.nonFinalField",
				"> b.B (owner class not found)",
				"\u2022 Field not found: int b.B.nonStaticField",
				"> b.B (owner class not found)",
				"\u2022 Field not found: int b.B.nonStaticSuperField",
				"> b.B (owner class not found)",
				"\u2022 Field not found: int b.B.publicField",
				"> b.B (owner class not found)",
				"\u2022 Field not found: static int b.B.staticField",
				"> b.B (owner class not found)",
				"\u2022 Field not found: static int b.B.staticSuperField",
				"> b.B (owner class not found)",
				"\u2022 Field not found: int b.B.superField",
				"> b.B (owner class not found)",
				"\u2022 Field not found: static b.E b.E.E3",
				"> b.E (owner class not found)"
		);
		assertEquals(expectedMessage, values[1]);
	}

}
