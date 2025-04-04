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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import org.jarhc.Main;
import org.jarhc.TestUtils;
import org.jarhc.it.utils.MavenSearchApiMockServer;
import org.jarhc.test.SystemExitException;
import org.jarhc.test.SystemExitManager;
import org.jarhc.utils.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

@ExtendWith(MavenSearchApiMockServer.class)
class MainTest {

	private SecurityManager originalSecurityManager;
	private PrintStream originalSystemOut;
	private PrintStream originalSystemErr;

	private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
	private final PrintStream stream = new PrintStream(buffer);

	@BeforeEach
	void setUp() {

		// replace security manager to intercept System.exit(int) calls
		originalSecurityManager = System.getSecurityManager();
		System.setSecurityManager(new SystemExitManager(originalSecurityManager));

		// redirect STDOUT and STDERR to an in-memory buffer
		originalSystemOut = System.out;
		originalSystemErr = System.err;
		System.setOut(stream);
		System.setErr(stream);

	}

	@AfterEach
	void tearDown() {

		// restore original STDOUT and STDERR
		System.setOut(originalSystemOut);
		System.setErr(originalSystemErr);

		// restore original security manager (may be null)
		System.setSecurityManager(originalSecurityManager);

	}

	@Test
	void main_withDataDir(@TempDir Path tempDir) throws IOException {

		// prepare
		File reportFile = new File(tempDir.toFile(), "report.txt");
		File dataDir = new File(tempDir.toFile(), ".jarhc");

		String[] args = new String[] {
				"--title", "SLF4J 1.7.28",
				"--output", reportFile.getAbsolutePath(),
				"--sections", "-jr", // exclude section Java Runtime
				"--data", dataDir.getAbsolutePath(),
				"org.slf4j:slf4j-api:1.7.28"
		};

		// test
		Main.main(args);

		// assert
		assertTrue(reportFile.isFile());
		assertTrue(dataDir.isDirectory());

		String actualReport = FileUtils.readFileToString(reportFile);

		if (TestUtils.createResources()) {
			TestUtils.saveResource("integrationTest", "/org/jarhc/it/MainTest/report.txt", actualReport, "UTF-8");
			return;
		}

		assertReport(actualReport);
	}

	@Test
	void main_withTempDataDir(@TempDir Path tempDir) throws IOException {

		// prepare
		File reportFile = new File(tempDir.toFile(), "report.txt");

		String[] args = new String[] {
				"--title", "SLF4J 1.7.28",
				"--output", reportFile.getAbsolutePath(),
				"--sections", "-jr", // exclude section Java Runtime
				"--data", "TEMP",
				"org.slf4j:slf4j-api:1.7.28"
		};

		// test
		Main.main(args);

		// assert
		assertTrue(reportFile.isFile());

		// make sure that application has not created a directory with name "TEMP"
		assertFalse(new File("TEMP").exists());

		String actualReport = FileUtils.readFileToString(reportFile);

		if (TestUtils.createResources()) {
			TestUtils.saveResource("integrationTest", "/org/jarhc/it/MainTest/report.txt", actualReport, "UTF-8");
			return;
		}

		assertReport(actualReport);
	}

	private void assertReport(String actualReport) throws IOException {

		String expectedReport = TestUtils.getResourceAsString("/org/jarhc/it/MainTest/report.txt", "UTF-8");
		assertEquals(expectedReport, actualReport);
	}

	@Test
	void main_print_help() {

		// prepare
		String[] args = new String[] { "--help" };

		try {

			// test
			Main.main(args);

			fail("System.exit(...) not called");

		} catch (SystemExitException e) {
			assertEquals(0, e.getStatus());
		}

		// flush output to buffer
		stream.flush();

		String output = buffer.toString(StandardCharsets.UTF_8);
		assertTrue(output.startsWith("Usage: java -jar JarHC.jar [options] <artifact> [<artifact>]*"));

	}

	@Test
	void main_print_usage() {

		// prepare
		String[] args = new String[] { "--unknown-option" };

		try {

			// test
			Main.main(args);

			fail("System.exit(...) not called");

		} catch (SystemExitException e) {
			assertEquals(-100, e.getStatus());
		}

		// flush output to buffer
		stream.flush();

		String output = buffer.toString(StandardCharsets.UTF_8);
		assertTrue(output.startsWith("Unknown option: '--unknown-option'."));

	}

}