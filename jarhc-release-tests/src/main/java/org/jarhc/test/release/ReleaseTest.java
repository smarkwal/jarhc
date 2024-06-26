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
import java.util.Comparator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
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
		String versionFilePath = "build/VERSION";
		jarHcVersion = readProjectFile(versionFilePath);
		Assertions.assertThat(jarHcVersion).as("JarHC version").matches("^\\d+\\.\\d+\\.\\d+(-SNAPSHOT)?$");

	}

	protected String getDependencies(String... configurations) {
		File file = getProjectFile("build/configurations.properties");
		try {
			Properties properties = new Properties();
			properties.load(new FileInputStream(file));
			Set<String> dependencies = new TreeSet<>(DEPENDENCY_COMPARATOR);
			for (String configuration : configurations) {
				String value = properties.getProperty(configuration);
				if (value == null) {
					throw new IllegalArgumentException("Configuration not found: " + configuration);
				}
				dependencies.addAll(List.of(value.split(",")));
			}
			return String.join(",", dependencies);
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

	private static final Comparator<String> DEPENDENCY_COMPARATOR = new Comparator<>() {

		@Override
		public int compare(String dependency1, String dependency2) {
			String artifactId1 = getArtifactId(dependency1);
			String artifactId2 = getArtifactId(dependency2);
			int diff = artifactId1.compareToIgnoreCase(artifactId2);
			if (diff == 0) {
				diff = dependency1.compareTo(dependency2);
			}
			return diff;
		}

		private String getArtifactId(String dependency) {
			int index = dependency.indexOf(':');
			if (index == -1) return dependency;
			return dependency.substring(index + 1);
		}

	};

}
