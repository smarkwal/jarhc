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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import org.jarhc.Main;
import org.jarhc.TestUtils;
import org.jarhc.test.TextUtils;
import org.jarhc.utils.FileUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

abstract class AbstractMainIT {

	private String[] extraArgs;

	AbstractMainIT(String... extraArgs) {
		this.extraArgs = extraArgs;
	}

	@Test
	void main(@TempDir Path tempDir) throws IOException {

		// prepare
		File reportFile = new File(tempDir.toFile(), "report.txt");
		File dataDir = new File(tempDir.toFile(), ".jarhc");

		String[] commonArgs = new String[]{
				"--title", this.getClass().getSimpleName(),
				"--format", "text",
				"--output", reportFile.getAbsolutePath(),
				"--sections", "-jr", // exclude Java Runtime section (depends on platform)
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

		String resource = "/" + this.getClass().getSimpleName() + "/report.txt";
		if (TestUtils.createResources()) {
			TestUtils.saveResource(resource, actualReport, "UTF-8");
			return;
		}

		String expectedReport = TestUtils.getResourceAsString(resource, "UTF-8");

		// normalize
		actualReport = TextUtils.toUnixLineSeparators(actualReport);
		expectedReport = TextUtils.toUnixLineSeparators(expectedReport);

		assertEquals(expectedReport, actualReport);

	}

}