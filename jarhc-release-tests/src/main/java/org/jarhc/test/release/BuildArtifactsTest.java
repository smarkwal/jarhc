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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.jarhc.test.release.utils.TestUtils;
import org.junit.jupiter.api.Test;

class BuildArtifactsTest extends ReleaseTest {

	// TODO: find a better solution than relative paths
	private static final String JARHC_BUILD = "../jarhc/build";

	@Test
	void version() throws IOException {

		// get expected version from Gradle properties file
		File propertiesFile = getProjectFile("../gradle.properties"); // TODO: find a better solution than a relative path
		Properties properties = new Properties();
		properties.load(new FileReader(propertiesFile));
		String expectedVersion = properties.getProperty("version");

		assertEquals(expectedVersion, getJarHcVersion());
	}

	@Test
	void jar() throws IOException {
		File jarFile = getProjectFile(JARHC_BUILD + "/libs/jarhc-" + getJarHcVersion() + ".jar");
		testJar(jarFile, "jar-toc.txt");
	}

	@Test
	void jarApp() throws IOException {
		File jarFile = getProjectFile(JARHC_BUILD + "/libs/jarhc-" + getJarHcVersion() + "-app.jar");
		testJar(jarFile, "jar-app-toc.txt");
	}

	private void testJar(File jarFile, String resourceName) throws IOException {

		String actualToC = buildJarToC(jarFile);
		String expectedToC = readResource(resourceName);

		if (!actualToC.equals(expectedToC)) {
			if (TestUtils.createResources()) {
				writeProjectFile("src/main/resources/" + resourceName, actualToC);
				return;
			}
			String diff = TestUtils.getDiff(expectedToC, actualToC);
			throw new AssertionError("Unexpected content in JAR file.\n" + diff);
		}
	}

	@Test
	void pom() {

		String actualPom = readProjectFile(JARHC_BUILD + "/publications/maven/pom-default.xml");
		String expectedPom = readResource("pom.xml");

		if (!actualPom.equals(expectedPom)) {
			if (TestUtils.createResources()) {
				writeProjectFile("src/main/resources/pom.xml", actualPom);
				return;
			}
			String diff = TestUtils.getDiff(expectedPom, actualPom);
			throw new AssertionError("Unexpected content in POM file.\n" + diff);
		}
	}

	// private helper methods --------------------------------------------------

	private String buildJarToC(File file) throws IOException {

		List<String> toc = new ArrayList<>();

		try (ZipFile zipFile = new ZipFile(file)) {

			Enumeration<? extends ZipEntry> entries = zipFile.entries();
			while (entries.hasMoreElements()) {
				ZipEntry entry = entries.nextElement();
				if (entry.isDirectory()) continue; // ignore directories

				String name = entry.getName();
				long size = entry.getSize();
				long crc = entry.getCrc();
				toc.add(String.format("%s | size=%d | crc=%d", name, size, crc));
			}

		}

		toc.sort(BuildArtifactsTest::compareToCLines);
		return String.join("\n", toc);
	}

	private static int compareToCLines(String line1, String line2) {
		int group1 = getToCLineGroup(line1);
		int group2 = getToCLineGroup(line2);
		if (group1 != group2) return group1 - group2;
		int diff = line1.compareToIgnoreCase(line2);
		if (diff == 0) {
			diff = line1.compareTo(line2);
		}
		return diff;
	}

	private static int getToCLineGroup(String line) {
		if (line.contains(".class")) {
			return 4;
		} else if (line.startsWith("META-INF/")) {
			return 3;
		} else if (line.contains("/")) {
			return 2;
		} else {
			return 1;
		}
	}

}
