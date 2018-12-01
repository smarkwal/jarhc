package org.jarcheck;

import org.junit.jupiter.api.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

@Disabled
class MainTest {

	private PrintStream out;
	private ByteArrayOutputStream buffer;

	@BeforeEach
	void before() {

		// remember original STDOUT
		out = System.out;

		// redirect STDOUT to a buffer
		buffer = new ByteArrayOutputStream();
		PrintStream tmp = new PrintStream(buffer);
		System.setOut(tmp);

	}

	@AfterEach
	void after() {

		// flush output
		System.out.flush();

		// restore STDOUT
		System.setOut(out);

	}

	@Test
	void test_main() {

		// test
		Main.main(new String[0]);

		// assert
		String output = new String(buffer.toByteArray(), StandardCharsets.UTF_8);
		String expectedOutput = "JarCheck 1.0-SNAPSHOT" + System.lineSeparator();
		Assertions.assertEquals(expectedOutput, output);

	}

}
