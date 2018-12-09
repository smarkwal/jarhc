package org.jarhc.app;

import org.jarhc.TestUtils;
import org.jarhc.env.JavaRuntime;
import org.jarhc.test.JavaRuntimeMock;
import org.jarhc.test.PrintStreamBuffer;
import org.jarhc.test.TextUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ApplicationIT {

	private static JavaRuntime defaultRuntime;

	@BeforeAll
	static void beforeAll() {

		// remember default Java runtime
		defaultRuntime = JavaRuntime.getDefault();

		// install a Java runtime mock
		JavaRuntime.setDefault(JavaRuntimeMock.createOracleRuntime());

	}

	@AfterAll
	static void afterAll() {

		// restore original Java runtime
		JavaRuntime.setDefault(defaultRuntime);

	}

	@Test
	void test() throws IOException {

		// prepare
		PrintStreamBuffer out = new PrintStreamBuffer();
		PrintStreamBuffer err = new PrintStreamBuffer();
		CommandLineParser commandLineParser = new CommandLineParser(err);
		Application application = new Application(commandLineParser, out, err);
		File file = TestUtils.getResourceAsFile("/test2/a.jar", "ApplicationTest-");
		String[] args = {file.getAbsolutePath()};

		// test
		int exitCode = application.run(args);

		// assert
		assertEquals(0, exitCode);
		assertEquals("", err.getText());

		String output = out.getText();
		String expectedOutput = TestUtils.getResourceAsString("/ApplicationIT_result.txt", "UTF-8");

		// normalize output
		output = TextUtils.toUnixLineSeparators(output);
		expectedOutput = TextUtils.toUnixLineSeparators(expectedOutput);

		assertEquals(expectedOutput, output);

	}

}
