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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LocalTestRunner extends AbstractTestRunner {

	private static final Logger LOGGER = LoggerFactory.getLogger(DockerTestRunner.class);

	private final String javaVersion;
	private final String javaHome;

	public LocalTestRunner(File workDir, File dataDir) {
		super(workDir, dataDir);
		this.javaVersion = TestUtils.getJavaVersion();
		this.javaHome = TestUtils.getJavaHome();
	}

	@Override
	public String getName() {
		return "Local Java " + javaVersion;
	}

	@Override
	public String getJavaVendor() {
		return "local";
	}

	@Override
	protected String getJavaVersion() {
		return javaVersion;
	}

	@Override
	public TestResult execute(Command command) {

		prepareWorkDir();

		// prepare output and error stream buffers
		ByteArrayOutputStream stdOutBuffer = new ByteArrayOutputStream(1024);
		ByteArrayOutputStream stdErrBuffer = new ByteArrayOutputStream(1024);

		try {

			// append path to JarHD data directory
			if (command.isJarHcCommand()) {
				command.addJarHcArguments("--data", dataDir.getAbsolutePath());
			}

			// build Java/JarHD command
			String[] cmd = command.build();

			// use same Java installation used to execute release tests
			cmd[0] = javaHome + "/bin/" + cmd[0];

			// start JVM with given command
			LOGGER.info("Run command: {}", command);
			Runtime runtime = Runtime.getRuntime();
			Process process = runtime.exec(cmd, null, workDir);

			// immediately close STDIN of JVM
			process.getOutputStream().close();

			// read data from STDOUT and STDERR of JVM
			try (InputStream outStream = process.getInputStream()) {
				try (InputStream errStream = process.getErrorStream()) {

					// prepare temporary buffer
					byte[] buffer = new byte[1024];

					// read streams while JVM is running
					while (process.isAlive()) {
						readFromStream(outStream, stdOutBuffer, buffer);
						readFromStream(errStream, stdErrBuffer, buffer);
					}

					// read remaining bytes
					readFromStream(outStream, stdOutBuffer, buffer);
					readFromStream(errStream, stdErrBuffer, buffer);

				}
			}

			int exitCode = process.exitValue();
			LOGGER.info("Exit code: {}", exitCode);
			String stdOut = new String(stdOutBuffer.toByteArray(), StandardCharsets.UTF_8);
			String stdErr = new String(stdErrBuffer.toByteArray(), StandardCharsets.UTF_8);

			return new TestResult(exitCode, stdOut, stdErr);

		} catch (IOException e) {
			throw new AssertionError("Unexpected I/O error.", e);
		}

	}

	private void prepareWorkDir() {
		try {
			if (!workDir.exists()) {
				FileUtils.forceMkdir(workDir);
			}
			File reportsDir = new File(workDir, "reports");
			if (!reportsDir.exists()) {
				FileUtils.forceMkdir(reportsDir);
			}
			if (!dataDir.exists()) {
				FileUtils.forceMkdir(dataDir);
			}
			for (String path : files.keySet()) {
				File file = files.get(path);
				File targetFile = new File(workDir, path);
				if (!targetFile.exists()) {
					FileUtils.copyFile(file, targetFile);
				}
			}
		} catch (IOException e) {
			throw new AssertionError("Unexpected I/O error.", e);
		}
	}

	private void readFromStream(InputStream outStream, ByteArrayOutputStream stdOutBuffer, byte[] buffer) throws IOException {
		if (outStream.available() > 0) {
			int bytes = outStream.read(buffer);
			if (bytes > 0) {
				stdOutBuffer.write(buffer, 0, bytes);
			}
		}
	}

}
