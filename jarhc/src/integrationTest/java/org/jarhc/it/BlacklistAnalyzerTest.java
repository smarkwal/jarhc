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
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import org.jarhc.TestUtils;
import org.jarhc.analyzer.BlacklistAnalyzer;
import org.jarhc.loader.ClasspathLoader;
import org.jarhc.loader.LoaderBuilder;
import org.jarhc.model.Classpath;
import org.jarhc.report.ReportSection;
import org.jarhc.report.ReportTable;
import org.jarhc.test.log.LoggerBuilder;
import org.jarhc.utils.StringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.Logger;

class BlacklistAnalyzerTest {

	private final ClasspathLoader classpathLoader = LoaderBuilder.create().buildClasspathLoader();
	private final Logger logger = LoggerBuilder.reject(BlacklistAnalyzer.class);
	private final BlacklistAnalyzer analyzer = new BlacklistAnalyzer(logger);

	@Test
	void test_analyze(@TempDir Path tempDir) throws IOException {

		// prepare
		File jarFile = TestUtils.getResourceAsFile("/org/jarhc/it/BlacklistAnalyzerTest/a.jar", tempDir);
		Classpath classpath = classpathLoader.load(Collections.singletonList(jarFile));

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

		String expectedMessage = StringUtils.joinLines(
				"`a.Runtime`",
				"\u2022 `java.lang.Process java.lang.Runtime.exec(java.lang.String)`",
				"\u2022 `java.lang.Process java.lang.Runtime.exec(java.lang.String,java.lang.String[])`",
				"\u2022 `java.lang.Process java.lang.Runtime.exec(java.lang.String,java.lang.String[],java.io.File)`",
				"\u2022 `java.lang.Process java.lang.Runtime.exec(java.lang.String[])`",
				"\u2022 `java.lang.Process java.lang.Runtime.exec(java.lang.String[],java.lang.String[])`",
				"\u2022 `java.lang.Process java.lang.Runtime.exec(java.lang.String[],java.lang.String[],java.io.File)`",
				"\u2022 `void java.lang.Runtime.exit(int)`",
				"\u2022 `void java.lang.Runtime.halt(int)`",
				"\u2022 `void java.lang.Runtime.load(java.lang.String)`",
				"\u2022 `void java.lang.Runtime.loadLibrary(java.lang.String)`",
				"",
				"`a.System`",
				"\u2022 `static void java.lang.System.exit(int)`",
				"\u2022 `static void java.lang.System.load(java.lang.String)`",
				"\u2022 `static void java.lang.System.loadLibrary(java.lang.String)`",
				"",
				"`a.Unsafe`",
				"\u2022 `int sun.misc.Unsafe.addressSize()`",
				"\u2022 `static sun.misc.Unsafe sun.misc.Unsafe.getUnsafe()`",
				"",
				"`a/start.bat`",
				"`a/start.sh`"
		);
		assertEquals(expectedMessage, values[1]);

	}

}
