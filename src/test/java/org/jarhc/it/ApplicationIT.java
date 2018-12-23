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

import org.jarhc.Context;
import org.jarhc.TestUtils;
import org.jarhc.app.Application;
import org.jarhc.app.CommandLineParser;
import org.jarhc.test.ContextMock;
import org.jarhc.test.PrintStreamBuffer;
import org.jarhc.test.TextUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junitpioneer.jupiter.TempDirectory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junitpioneer.jupiter.TempDirectory.TempDir;

@ExtendWith(TempDirectory.class)
class ApplicationIT {

	@Test
	void test(@TempDir Path tempDir) throws IOException {

		// prepare
		PrintStreamBuffer out = new PrintStreamBuffer();
		PrintStreamBuffer err = new PrintStreamBuffer();
		CommandLineParser commandLineParser = new CommandLineParser(err);
		Context context = ContextMock.createContext();
		Application application = new Application(commandLineParser, context, out, err);
		File file = TestUtils.getResourceAsFile("/ApplicationIT/a.jar", tempDir);
		String[] args = {file.getAbsolutePath()};

		// test
		int exitCode = application.run(args);

		// assert
		assertEquals(0, exitCode);
		assertEquals("", err.getText());

		String output = out.getText();

		if (TestUtils.createResources()) {
			TestUtils.saveResource("/ApplicationIT/result.txt", output, "UTF-8");
			return;
		}

		String expectedOutput = TestUtils.getResourceAsString("/ApplicationIT/result.txt", "UTF-8");

		// normalize output
		output = TextUtils.toUnixLineSeparators(output);
		expectedOutput = TextUtils.toUnixLineSeparators(expectedOutput);

		assertEquals(expectedOutput, output);

	}

}
