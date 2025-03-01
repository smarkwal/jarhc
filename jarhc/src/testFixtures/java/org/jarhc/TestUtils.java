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

package org.jarhc;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import org.jarhc.artifacts.MavenRepository;
import org.jarhc.test.TestDataException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;

/**
 * Test utility methods.
 */
public class TestUtils {

	public static InputStream getResourceAsStream(String resource) throws IOException {
		if (resource == null) throw new IllegalArgumentException("resource");
		InputStream stream = TestUtils.class.getResourceAsStream(resource);
		if (stream == null) throw new IOException("Resource not found: " + resource);
		return stream;
	}

	public static String getResourceAsString(String resource, String encoding) throws IOException {
		if (resource == null) throw new IllegalArgumentException("resource");
		ByteArrayOutputStream result = new ByteArrayOutputStream();
		try (InputStream stream = getResourceAsStream(resource)) {
			byte[] buffer = new byte[1024];
			while (true) {
				int len = stream.read(buffer);
				if (len < 0) break;
				result.write(buffer, 0, len);
			}
		}
		return result.toString(encoding);
	}

	public static List<String> getResourceAsLines(String resource, String encoding) throws IOException {
		List<String> lines = new ArrayList<>();
		try (InputStream stream = getResourceAsStream(resource)) {
			BufferedReader reader = new BufferedReader(new InputStreamReader(stream, encoding));
			while (true) {
				String line = reader.readLine();
				if (line == null) break;
				lines.add(line);
			}
		}
		return lines;
	}

	public static File getResourceAsFile(String resource, Path directory) throws IOException {
		if (resource == null) throw new IllegalArgumentException("resource");
		if (directory == null) throw new IllegalArgumentException("directory");
		InputStream stream = getResourceAsStream(resource);
		String fileName = getFileName(resource);
		File file = new File(directory.toFile(), fileName);
		Files.copy(stream, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
		return file;
	}

	private static String getFileName(String resource) {
		if (resource.contains("/")) {
			int post = resource.lastIndexOf('/');
			return resource.substring(post + 1);
		} else {
			return resource;
		}
	}

	/**
	 * Checks if the system property "jarhc.test.resources.generate" is set.
	 * This property is used as flag to instruct tests to re-generate their test resources.
	 *
	 * @return <code>true</code> if test resources should be generated.
	 */
	public static boolean createResources() {
		Properties properties = System.getProperties();
		return properties.containsKey("jarhc.test.resources.generate");
	}

	/**
	 * Save the given text as test resource.
	 * <p>
	 * Note: This method works as expected only if the current working directory
	 * is set to the root repository directory (where the "src" directory is located).
	 *
	 * @param sourceSet Test source set
	 * @param resource  Resource path
	 * @param text      Text to save
	 * @param encoding  File encoding
	 */
	public static void saveResource(String sourceSet, String resource, String text, String encoding) throws IOException {
		Path path = Paths.get("src", sourceSet, "resources", resource);
		byte[] data = text.getBytes(encoding);

		// check if file content has changed
		if (Files.exists(path)) {
			byte[] data2 = Files.readAllBytes(path);
			if (Arrays.equals(data, data2)) {
				return;
			}
		}

		// create parent directory (if it does not yet exist)
		Path directory = path.getParent();
		if (!Files.exists(directory)) {
			Files.createDirectories(directory);
		}

		try {
			Files.write(path, data);
		} catch (IOException e) {
			throw new TestDataException(e);
		}

		//noinspection ConstantConditions
		Assumptions.assumeTrue(false, "Test resource generated.");
	}

	public static String getFileRepositoryURL() {
		File directory = new File("src/integrationTest/resources/repository");
		return "file://" + directory.getAbsolutePath();
	}

	public static MavenRepository.Settings getFileRepositorySettings() {
		return new MavenRepository.Settings() {
			@Override
			public String getRepositoryUrl() {
				return getFileRepositoryURL();
			}

			@Override
			public String getRepositoryUsername() {
				return null;
			}

			@Override
			public String getRepositoryPassword() {
				return null;
			}
		};
	}

	/**
	 * Assert that the given values are equals to the expected values.
	 *
	 * @param actualValues   Actual values
	 * @param expectedValues Expected values
	 */
	public static void assertValuesEquals(String[] actualValues, String... expectedValues) {
		Assertions.assertNotNull(actualValues);
		Assertions.assertNotNull(expectedValues);
		Assertions.assertEquals(expectedValues.length, actualValues.length);
		for (int i = 0; i < actualValues.length; i++) {
			String actualValue = actualValues[i];
			String expectedValue = expectedValues[i];
			Assertions.assertEquals(expectedValue, actualValue);
		}
	}

}
