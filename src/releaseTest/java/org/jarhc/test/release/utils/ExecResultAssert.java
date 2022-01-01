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
