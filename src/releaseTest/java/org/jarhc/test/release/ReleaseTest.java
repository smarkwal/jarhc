package org.jarhc.test.release;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.assertj.core.api.Assertions;
import org.jarhc.test.release.utils.JarHcContainer;
import org.testcontainers.shaded.org.apache.commons.io.FileUtils;
import org.testcontainers.shaded.org.apache.commons.io.IOUtils;

abstract class ReleaseTest {

	private String jarHcVersion;
	private File projectDir;
	private File jarFile;

	ReleaseTest() {
		findProjectFiles();
	}

	private void findProjectFiles() {

		projectDir = new File(".").getAbsoluteFile();
		// TODO: check if current directory is project dir

		// read JarHC version from VERSION file (if it exists)
		String versionFilePath = "build/resources/main/VERSION";
		jarHcVersion = readProjectFile(versionFilePath);
		Assertions.assertThat(jarHcVersion).as("JarHC version").matches("^[1-9]\\.[1-9][0-9]*(-SNAPSHOT)?$");

		// check if fat/uber JAR file has been built
		String jarFilePath = String.format("build/libs/jarhc-%s-with-deps.jar", jarHcVersion);
		jarFile = getProjectFile(jarFilePath);
	}

	/**
	 * Get JarHC version.
	 *
	 * @return JarHC version, for example "1.6-SNAPSHOT".
	 */
	String getJarHcVersion() {
		return jarHcVersion;
	}

	/**
	 * Get project file given its path.
	 *
	 * @param path File path, relative to project root.
	 * @return Project file.
	 */
	File getProjectFile(String path) {
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
	String readProjectFile(String path) {
		File file = getProjectFile(path);
		try {
			return FileUtils.readFileToString(file, StandardCharsets.UTF_8);
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
	String readResource(String path) {
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

	JarHcContainer createJarHcContainer(String javaImageName) {

		// create a new container with the given Java image
		JarHcContainer container = new JarHcContainer(javaImageName);

		// map JarHC fat/uber JAR file into container
		container.withFileSystemBind(jarFile.getAbsolutePath(), "/jarhc/jarhc.jar");

		// set path to JarHC data directory
		container.withEnv("JARHC_DATA", "/jarhc/data");

		// set working directory so that JAR is easily accessible
		container.withWorkingDirectory("/jarhc");

		// override default container command
		// (otherwise, a JShell may be started and consume valuable memory)
		container.withCommand("sleep", "1h");

		return container;
	}

}
