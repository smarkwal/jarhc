package org.jarcheck.app;

import org.jarcheck.TestUtils;
import org.jarcheck.test.PrintStreamBuffer;
import org.jarcheck.test.TextUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ApplicationIT {

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
