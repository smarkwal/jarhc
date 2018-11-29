package net.markwalder.jarcc;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

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
		String expectedOutput = "JarCC 1.0-SNAPSHOT" + System.lineSeparator();
		Assertions.assertEquals(expectedOutput, output);

	}

}
