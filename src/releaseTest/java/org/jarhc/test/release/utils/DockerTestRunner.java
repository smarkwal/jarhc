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

import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.Container;

public class DockerTestRunner extends AbstractTestRunner {

	private static final Logger LOGGER = LoggerFactory.getLogger(DockerTestRunner.class);

	protected final JavaImage javaImage;

	public DockerTestRunner(JavaImage javaImage, File workDir, File dataDir) {
		super(workDir, dataDir);
		this.javaImage = javaImage;
	}

	@Override
	public String getName() {
		return javaImage.getImageName();
	}

	@Override
	public String getJavaVendor() {
		return javaImage.getVendor() + "-" + javaImage.getProduct();
	}

	@Override
	protected String getJavaVersion() {
		return javaImage.getVersion();
	}

	@Override
	public TestResult execute(Command command) {

		JavaContainer container = createJavaContainer();

		Container.ExecResult result;
		try {

			LOGGER.info("Start container: {}", getName());
			container.start();

			LOGGER.info("Run command: {}", command);
			result = container.exec(command);

			int exitCode = result.getExitCode();
			LOGGER.info("Exit code: {}", exitCode);

		} finally {

			LOGGER.info("Stop container: {}", getName());
			container.stop();
		}

		return new TestResult(result);
	}

	protected JavaContainer createJavaContainer() {

		prepareWorkDir();

		// create a new container with the given Java image
		JavaContainer container = new JavaContainer(javaImage);

		// map JarHC work directory into container
		container.withFileSystemBind(workDir.getAbsolutePath(), "/jarhc");

		// map installed files into container
		for (String path : files.keySet()) {
			File file = files.get(path);
			container.withFileSystemBind(file.getAbsolutePath(), "/jarhc/" + path);
		}

		// map JarHC data directory into container
		container.withFileSystemBind(dataDir.getAbsolutePath(), "/jarhc/data");

		// set path to JarHC data directory
		container.withEnv("JARHC_DATA", "/jarhc/data");

		// set working directory
		container.withWorkingDirectory("/jarhc");

		// override default container command
		// (otherwise, a JShell may be started and consume valuable memory)
		container.withCommand("sleep", "1h");

		// get UID and GID of current user
		String uid = SysUtils.getUID();
		String gid = SysUtils.getGID();

		// use same user in container
		String user = uid + ":" + gid;
		container.withCreateContainerCmdModifier(cmd -> cmd.withUser(user));

		return container;
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
		} catch (IOException e) {
			throw new AssertionError("Unexpected I/O error.", e);
		}
	}

}
