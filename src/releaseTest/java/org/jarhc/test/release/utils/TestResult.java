/*
 * Copyright 2022 Stephan Markwalder
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

package org.jarhc.test.release.utils;

import java.util.Arrays;
import java.util.List;
import org.assertj.core.util.diff.Delta;
import org.assertj.core.util.diff.DiffUtils;
import org.assertj.core.util.diff.Patch;
import org.testcontainers.containers.Container;

public class TestResult {

	private final int exitCode;
	private final String stdOut;
	private final String stdErr;

	TestResult(int exitCode, String stdOut, String stdErr) {
		this.exitCode = exitCode;
		this.stdOut = stdOut;
		this.stdErr = stdErr;
	}

	TestResult(Container.ExecResult result) {
		this(
				result.getExitCode(),
				result.getStdout(),
				result.getStderr()
		);
	}

	public int getExitCode() {
		return exitCode;
	}

	public String getStdOut() {
		return stdOut;
	}

	public String getStdErr() {
		return stdErr;
	}

	public void assertEquals(int expectedExitCode, String expectedStdOut, String expectedStdErr) {

		StringBuilder buffer = new StringBuilder();

		if (exitCode != expectedExitCode) {
			buffer.append("----------------------------------------------------------------------------------------------------------\n");
			buffer.append("Exit code:\n");
			buffer.append("expecting: ").append(expectedExitCode).append("\n");
			buffer.append("but was  : ").append(exitCode).append("\n");
		}

		if (!stdOut.equals(expectedStdOut)) {
			buffer.append("----------------------------------------------------------------------------------------------------------\n");
			buffer.append("STDOUT:\n");
			appendDiff(buffer, expectedStdOut, stdOut);
		}

		if (!stdErr.equals(expectedStdErr)) {
			buffer.append("----------------------------------------------------------------------------------------------------------\n");
			buffer.append("STDERR:\n");
			appendDiff(buffer, expectedStdErr, stdErr);
		}

		if (buffer.length() > 0) {
			buffer.append("----------------------------------------------------------------------------------------------------------\n");
			throw new AssertionError("Unexpected result.\n" + buffer);
		}
	}

	// private helper methods --------------------------------------------------

	private static void appendDiff(StringBuilder buffer, String expected, String actual) {
		List<String> lines1 = splitLines(expected);
		List<String> lines2 = splitLines(actual);
		Patch<String> patch = DiffUtils.diff(lines1, lines2);
		for (Delta<String> delta : patch.getDeltas()) {
			buffer.append(delta).append("\n");
		}
	}

	private static List<String> splitLines(String text) {
		String[] lines = text.split("\\r?\\n");
		return Arrays.asList(lines);
	}

}
