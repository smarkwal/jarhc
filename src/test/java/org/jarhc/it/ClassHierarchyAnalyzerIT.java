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

import org.jarhc.TestUtils;
import org.jarhc.analyzer.ClassHierarchyAnalyzer;
import org.jarhc.loader.ClasspathLoader;
import org.jarhc.loader.LoaderBuilder;
import org.jarhc.model.Classpath;
import org.jarhc.report.ReportSection;
import org.jarhc.report.ReportTable;
import org.jarhc.utils.StringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junitpioneer.jupiter.TempDirectory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(TempDirectory.class)
class ClassHierarchyAnalyzerIT {

	private final ClasspathLoader classpathLoader = LoaderBuilder.create().buildClasspathLoader();
	private ClassHierarchyAnalyzer analyzer = new ClassHierarchyAnalyzer();

	@Test
	void analyze_compatible(@TempDirectory.TempDir Path tempDir) throws IOException {

		// prepare
		File jarFile1 = TestUtils.getResourceAsFile("/ClassHierarchyAnalyzerIT/a.jar", tempDir);
		File jarFile2 = TestUtils.getResourceAsFile("/ClassHierarchyAnalyzerIT/b-1.jar", tempDir);
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
	void analyze_incompatible(@TempDirectory.TempDir Path tempDir) throws IOException {

		// prepare
		File jarFile1 = TestUtils.getResourceAsFile("/ClassHierarchyAnalyzerIT/a.jar", tempDir);
		File jarFile2 = TestUtils.getResourceAsFile("/ClassHierarchyAnalyzerIT/b-2.jar", tempDir);
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
				"Superclass is final: public final class b.B1",
				"Superclass is an interface: public interface b.B2",
				"Superclass is an annotation: public @interface b.B3",
				"Superclass is final: public enum b.B4",
				"Superclass is an enum: public enum b.B4",
				"Interface is a class: public class b.B5a",
				"Interface is an abstract class: public abstract class b.B5b",
				"Interface is an annotation: public @interface b.B5c",
				"Interface is an enum: public enum b.B5d",
				"Interface is a class: public class b.B6",
				"Interface is an annotation: public @interface b.B7",
				"Interface is an enum: public enum b.B8",
				"Interface is an abstract class: public abstract class b.B9"
		);
		assertEquals(expectedMessage, values[1]);

	}

}
