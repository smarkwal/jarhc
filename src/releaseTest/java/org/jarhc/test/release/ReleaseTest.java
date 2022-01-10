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

package org.jarhc.test.release;

import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.assertj.core.api.Assertions;

abstract class ReleaseTest {

	private String jarHcVersion;
	private File projectDir;

	ReleaseTest() {
		findProjectFiles();
	}

	private void findProjectFiles() {

		// get current working directory
		projectDir = new File(".").getAbsoluteFile();

		// read JarHC version from VERSION file (if it exists)
		String versionFilePath = "build/resources/main/VERSION";
		jarHcVersion = readProjectFile(versionFilePath);
		Assertions.assertThat(jarHcVersion).as("JarHC version").matches("^[1-9]\\.[1-9][0-9]*(-SNAPSHOT)?$");

	}

	protected String getDependencies(String configuration) {
		File file = getProjectFile("build/configurations.properties");
		try {
			Properties properties = new Properties();
			properties.load(new FileInputStream(file));
			return properties.getProperty(configuration, "");
		} catch (IOException e) {
			throw new AssertionError("Unexpected I/O error.", e);
		}
	}

	/**
	 * Get JarHC version.
	 *
	 * @return JarHC version, for example "1.4" or "1.5-SNAPSHOT".
	 */
	protected String getJarHcVersion() {
		return jarHcVersion;
	}

	/**
	 * Get project directory given its path.
	 *
	 * @param path Directory path, relative to project root.
	 * @return Project directory.
	 */
	protected File getProjectDirectory(String path) {
		File directory = new File(projectDir, path);
		Assertions.assertThat(directory).isDirectory();
		return directory;
	}

	/**
	 * Get project file given its path.
	 *
	 * @param path File path, relative to project root.
	 * @return Project file.
	 */
	protected File getProjectFile(String path) {
		File file = new File(projectDir, path);
		Assertions.assertThat(file).isFile().canRead();
		return file;
	}

	/**
	 * Read the text file from the given project file path.
	 *
	 * @param path File path, relative to project root.
	 * @return Content of text file.
	 */
	protected String readProjectFile(String path) {
		File file = getProjectFile(path);
		try {
			return FileUtils.readFileToString(file, StandardCharsets.UTF_8);
		} catch (IOException e) {
			throw new AssertionError("Unexpected I/O error.", e);
		}
	}

	protected void writeProjectFile(String path, String text) {
		File file = getProjectFile(path);
		try {
			FileUtils.writeStringToFile(file, text, StandardCharsets.UTF_8);
		} catch (IOException e) {
			throw new AssertionError("Unexpected I/O error.", e);
		}
	}

	/**
	 * Read the text resource from the given path.
	 *
	 * @param path Resource path, relative to classpath root.
	 * @return Content of text resource.
	 */
	protected String readResource(String path) {
		try (InputStream stream = this.getClass().getClassLoader().getResourceAsStream(path)) {
			if (stream == null) {
				String message = String.format("Resource not found: %s", path);
				throw new AssertionError(message);
			}
			return IOUtils.toString(stream, StandardCharsets.UTF_8);
		} catch (IOException e) {
			throw new AssertionError("Unexpected I/O error.", e);
		}
	}

	protected void createDirectory(File directory) {
		if (!directory.exists()) {
			boolean created = directory.mkdirs();
			assumeTrue(created, "Directory has been created: " + directory.getAbsolutePath());
		}
	}

}
