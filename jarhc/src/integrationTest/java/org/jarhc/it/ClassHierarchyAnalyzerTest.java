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

package org.jarhc.it;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import org.jarhc.TestUtils;
import org.jarhc.analyzer.BinaryCompatibilityAnalyzer;
import org.jarhc.app.Options;
import org.jarhc.env.JavaRuntime;
import org.jarhc.loader.ClasspathLoader;
import org.jarhc.loader.LoaderBuilder;
import org.jarhc.model.Classpath;
import org.jarhc.report.ReportSection;
import org.jarhc.report.ReportTable;
import org.jarhc.test.JavaRuntimeMock;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ClassHierarchyAnalyzerTest {

	private final JavaRuntime javaRuntime = JavaRuntimeMock.getOracleRuntime();
	private final ClasspathLoader classpathLoader = LoaderBuilder.create().withParentClassLoader(javaRuntime).buildClasspathLoader();
	private final BinaryCompatibilityAnalyzer analyzer = new BinaryCompatibilityAnalyzer(new Options());

	@Test
	void analyze_compatible(@TempDir Path tempDir) throws IOException {

		// prepare
		File jarFile1 = TestUtils.getResourceAsFile("/org/jarhc/it/ClassHierarchyAnalyzerTest/a.jar", tempDir);
		File jarFile2 = TestUtils.getResourceAsFile("/org/jarhc/it/ClassHierarchyAnalyzerTest/b-1.jar", tempDir);
		Classpath classpath = classpathLoader.load(List.of(jarFile1, jarFile2));

		// test
		ReportSection section = analyzer.analyze(classpath);

		// assert
		List<Object> content = section.getContent();
		assertEquals(1, content.size());
		Object object = content.get(0);
		assertInstanceOf(ReportTable.class, object);
		ReportTable table = (ReportTable) object;
		List<String[]> rows = table.getRows();
		assertEquals(0, rows.size());

	}

	@Test
	void analyze_incompatible(@TempDir Path tempDir) throws IOException {

		// prepare
		File jarFile1 = TestUtils.getResourceAsFile("/org/jarhc/it/ClassHierarchyAnalyzerTest/a.jar", tempDir);
		File jarFile2 = TestUtils.getResourceAsFile("/org/jarhc/it/ClassHierarchyAnalyzerTest/b-2.jar", tempDir);
		Classpath classpath = classpathLoader.load(List.of(jarFile1, jarFile2));

		// test
		ReportSection section = analyzer.analyze(classpath);

		// assert
		List<Object> content = section.getContent();
		assertEquals(1, content.size());
		Object object = content.get(0);
		assertInstanceOf(ReportTable.class, object);
		ReportTable table = (ReportTable) object;
		List<String[]> rows = table.getRows();
		assertEquals(1, rows.size());

		String[] values = rows.get(0);
		assertEquals(2, values.length);
		assertEquals("a", values[0]);

		String value = values[1];
		String expectedValue = TestUtils.getResourceAsString("/org/jarhc/it/ClassHierarchyAnalyzerTest/result.txt", "UTF-8");
		assertEquals(expectedValue, value);

	}

}
