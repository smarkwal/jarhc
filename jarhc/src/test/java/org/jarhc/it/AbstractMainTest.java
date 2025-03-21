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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.regex.Pattern;
import org.jarhc.Main;
import org.jarhc.TestUtils;
import org.jarhc.utils.FileUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

abstract class AbstractMainTest extends AbstractOutputTest {

	private final String reportFileName;
	private final String[] extraArgs;

	AbstractMainTest(String reportFileName, String... extraArgs) {
		this.reportFileName = reportFileName;
		this.extraArgs = extraArgs;
	}

	@Test
	void main(@TempDir Path tempDir) throws IOException {

		// prepare
		File reportFile = new File(tempDir.toFile(), reportFileName);
		File dataDir = new File(tempDir.toFile(), ".jarhc");

		String[] commonArgs = new String[] {
				"--title", this.getClass().getSimpleName(),
				"--output", reportFile.getAbsolutePath(),
				"--sections", "-jr", // exclude section Java Runtime
				"--data", dataDir.getAbsolutePath(),
		};

		// combine common arguments and extra arguments
		String[] args = new String[commonArgs.length + extraArgs.length];
		System.arraycopy(commonArgs, 0, args, 0, commonArgs.length);
		System.arraycopy(extraArgs, 0, args, commonArgs.length, extraArgs.length);

		// test
		Main.main(args);

		// assert
		assertTrue(reportFile.isFile());
		assertTrue(dataDir.isDirectory());

		String actualReport = FileUtils.readFileToString(reportFile);

		// remove embedded JSON report data (different JDKs and Java versions use different compression parameters)
		actualReport = Pattern.compile("<!-- JSON REPORT DATA.*-->", Pattern.DOTALL).matcher(actualReport).replaceAll("<!-- JSON REPORT DATA\n[REMOVED]\n-->");

		String resource = "/org/jarhc/it/" + this.getClass().getSimpleName() + "/" + reportFileName;
		if (TestUtils.createResources()) {
			TestUtils.saveResource("test", resource, actualReport, "UTF-8");
			return;
		}

		String expectedReport = TestUtils.getResourceAsString(resource, "UTF-8");
		assertEquals(expectedReport, actualReport);

		String output = getOutput();
		assertThat(output).startsWith("JarHC - JAR Health Check 0.0.1");
	}

}