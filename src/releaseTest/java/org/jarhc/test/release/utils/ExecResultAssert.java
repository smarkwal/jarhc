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

import java.util.function.Consumer;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.AbstractIntegerAssert;
import org.assertj.core.api.AbstractStringAssert;
import org.assertj.core.api.Assertions;
import org.testcontainers.containers.Container.ExecResult;

@SuppressWarnings({ "unused", "UnusedReturnValue" })
public class ExecResultAssert extends AbstractAssert<ExecResultAssert, ExecResult> {

	ExecResultAssert(ExecResult execResult) {
		super(execResult, ExecResultAssert.class);
	}

	public static ExecResultAssert assertThat(ExecResult actual) {
		return new ExecResultAssert(actual);
	}

	// exit code ---------------------------------------------------------------

	public ExecResultAssert hasExitCode(int exitCode) {
		assertExitCode().isEqualTo(exitCode);
		return this;
	}

	private AbstractIntegerAssert<?> assertExitCode() {
		return Assertions.assertThat(actual.getExitCode()).as("exit code");
	}

	// STDOUT ------------------------------------------------------------------

	public ExecResultAssert hasNoStdout() {
		assertStdout().isEmpty();
		return this;
	}

	public ExecResultAssert hasStdout(String stdout) {
		assertStdout().isEqualTo(stdout);
		return this;
	}

	public ExecResultAssert hasStdout(String format, Object... args) {
		assertStdout().isEqualTo(format, args);
		return this;
	}

	public ExecResultAssert hasStdout(Consumer<AbstractStringAssert<?>> consumer) {
		consumer.accept(assertStdout());
		return this;
	}

	private AbstractStringAssert<?> assertStdout() {
		return Assertions.assertThat(actual.getStdout()).as("STDOUT");
	}

	// STDERR ------------------------------------------------------------------

	public ExecResultAssert hasNoStderr() {
		assertStderr().isEmpty();
		return this;
	}

	public ExecResultAssert hasStderr(String stderr) {
		assertStderr().isEqualTo(stderr);
		return this;
	}

	public ExecResultAssert hasStderr(String format, Object... args) {
		assertStderr().isEqualTo(format, args);
		return this;
	}

	public ExecResultAssert hasStderr(Consumer<AbstractStringAssert<?>> consumer) {
		consumer.accept(assertStderr());
		return this;
	}

	private AbstractStringAssert<?> assertStderr() {
		return Assertions.assertThat(actual.getStderr()).as("STDERR");
	}

}
