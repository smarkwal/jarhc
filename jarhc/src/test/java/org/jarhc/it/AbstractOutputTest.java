/*
 * Copyright 2025 Stephan Markwalder
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

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

abstract class AbstractOutputTest {

	private PrintStream originalSystemOut;
	private PrintStream originalSystemErr;

	private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
	private final PrintStream stream = new PrintStream(buffer);

	protected String getOutput() {

		// make sure the buffer is flushed
		stream.flush();

		// return the content of the buffer
		return buffer.toString(StandardCharsets.UTF_8);
	}

	@BeforeEach
	public void redirectOutput() {
		// redirect STDOUT and STDERR to an in-memory buffer
		originalSystemOut = System.out;
		originalSystemErr = System.err;
		System.setOut(stream);
		System.setErr(stream);
	}

	@AfterEach
	public void restoreOutput() {
		// restore original STDOUT and STDERR
		System.setOut(originalSystemOut);
		System.setErr(originalSystemErr);
	}

}
