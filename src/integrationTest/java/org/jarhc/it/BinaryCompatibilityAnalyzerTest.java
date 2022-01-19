/*
 * Copyright 2022 Stephan Markwalder
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
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import org.jarhc.TestUtils;
import org.jarhc.analyzer.BinaryCompatibilityAnalyzer;
import org.jarhc.app.Options;
import org.jarhc.env.DefaultJavaRuntime;
import org.jarhc.loader.ClasspathLoader;
import org.jarhc.loader.LoaderBuilder;
import org.jarhc.model.Classpath;
import org.jarhc.report.ReportSection;
import org.jarhc.report.ReportTable;
import org.jarhc.test.log.LoggerBuilder;
import org.jarhc.utils.JavaUtils;
import org.jarhc.utils.StringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class BinaryCompatibilityAnalyzerTest {

	private final DefaultJavaRuntime javaRuntime = new DefaultJavaRuntime(LoggerBuilder.noop());
	private final ClasspathLoader classpathLoader = LoaderBuilder.create().withParentClassLoader(javaRuntime).buildClasspathLoader();
	private final BinaryCompatibilityAnalyzer analyzer = new BinaryCompatibilityAnalyzer(new Options());

	@Test
	void analyze_jdkInternalClass(@TempDir Path tempDir) throws IOException {
		int javaVersion = JavaUtils.getJavaVersion();
		assumeTrue(javaVersion == 8 || javaVersion == 11 || javaVersion == 17, "Test is only designed for Java 8, 11, and 17.");

		// prepare
		File jarFile = TestUtils.getResourceAsFile("/org/jarhc/it/BinaryCompatibilityAnalyzerTest/a.jar", tempDir);
		Classpath classpath = classpathLoader.load(Collections.singletonList(jarFile));

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

		if (javaVersion == 8) {
			assertEquals(StringUtils.joinLines("a.A", "\u2022 Class not found: jdk.internal.util.ArraysSupport (package not found)"), values[1]);
		} else if (javaVersion == 11) {
			assertEquals(StringUtils.joinLines("a.A", "\u2022 Class is not exported by module java.base: public class jdk.internal.util.ArraysSupport", "\u2022 Class is not exported by module java.base: public final class sun.text.IntHashtable"), values[1]);
		} else {
			assertEquals(StringUtils.joinLines("a.A", "\u2022 Class is not exported by module java.base: public final class sun.text.IntHashtable"), values[1]);
		}

	}

}
