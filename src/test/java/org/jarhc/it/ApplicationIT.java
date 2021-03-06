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

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import org.jarhc.TestUtils;
import org.jarhc.app.Application;
import org.jarhc.app.Options;
import org.jarhc.pom.POMLoader;
import org.jarhc.pom.resolver.RepositoryDependencyResolver;
import org.jarhc.test.JavaRuntimeMock;
import org.jarhc.test.PrintStreamBuffer;
import org.jarhc.test.RepositoryMock;
import org.jarhc.test.TextUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ApplicationIT {

	@Test
	void test(@TempDir Path tempDir) throws IOException {

		// prepare
		PrintStreamBuffer out = new PrintStreamBuffer();

		Application application = new Application();
		application.setOut(out);
		application.setJavaRuntimeFactory(JavaRuntimeMock::getOracleRuntime);
		RepositoryMock repository = RepositoryMock.createRepository();
		application.setRepository(repository);
		application.setDependencyResolver(new RepositoryDependencyResolver(new POMLoader(repository)));

		Options options = new Options();
		File file = TestUtils.getResourceAsFile("/org/jarhc/it/ApplicationIT/a.jar", tempDir);
		options.addClasspathJarPath(file.getAbsolutePath());

		// test
		int exitCode = application.run(options);

		// assert
		assertEquals(0, exitCode);

		String output = out.getText();

		if (TestUtils.createResources()) {
			TestUtils.saveResource("/org/jarhc/it/ApplicationIT/result.txt", output, "UTF-8");
			return;
		}

		String expectedOutput = TestUtils.getResourceAsString("/org/jarhc/it/ApplicationIT/result.txt", "UTF-8");

		// normalize output
		output = TextUtils.toUnixLineSeparators(output);
		expectedOutput = TextUtils.toUnixLineSeparators(expectedOutput);

		assertEquals(expectedOutput, output);

	}

}
