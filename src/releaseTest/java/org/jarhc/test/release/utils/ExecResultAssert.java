/*
 * Copyright 2021 Stephan Markwalder
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
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.util.diff.Delta;
import org.assertj.core.util.diff.DiffUtils;
import org.assertj.core.util.diff.Patch;
import org.testcontainers.containers.Container.ExecResult;

public class ExecResultAssert extends AbstractAssert<ExecResultAssert, ExecResult> {

	ExecResultAssert(ExecResult execResult) {
		super(execResult, ExecResultAssert.class);
	}

	public static ExecResultAssert assertThat(ExecResult actual) {
		return new ExecResultAssert(actual);
	}

	public void isEqualTo(int expectedExitCode, String expectedStdout, String expectedStderr) {

		int actualExitCode = actual.getExitCode();
		String actualStdout = actual.getStdout();
		String actualStderr = actual.getStderr();

		// replace signatures
		expectedStdout = replaceSignatures(expectedStdout);
		expectedStderr = replaceSignatures(expectedStderr);
		actualStdout = replaceSignatures(actualStdout);
		actualStderr = replaceSignatures(actualStderr);

		StringBuilder buffer = new StringBuilder();
		if (actualExitCode != expectedExitCode) {
			buffer.append("----------------------------------------------------------------------------------------------------------\n");
			buffer.append("Exit code:\n");
			buffer.append("expecting: ").append(expectedExitCode).append("\n");
			buffer.append("but was  : ").append(actualExitCode).append("\n");
		}
		if (!actualStdout.equals(expectedStdout)) {
			buffer.append("----------------------------------------------------------------------------------------------------------\n");
			buffer.append("STDOUT:\n");
			appendDiff(buffer, expectedStdout, actualStdout);
		}
		if (!actualStderr.equals(expectedStderr)) {
			buffer.append("----------------------------------------------------------------------------------------------------------\n");
			buffer.append("STDERR:\n");
			appendDiff(buffer, expectedStderr, actualStderr);
		}
		if (buffer.length() > 0) {
			buffer.append("----------------------------------------------------------------------------------------------------------\n");
			throw new AssertionError("ExecResult mismatch.\n" + buffer);
		}
	}

	// private helper methods --------------------------------------------------

	private String replaceSignatures(String expectedStdout) {
		return expectedStdout.replaceAll("\\| [0-9a-f]{40} \\|", "| 0000000000000000000000000000000000000000 |");
	}

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
