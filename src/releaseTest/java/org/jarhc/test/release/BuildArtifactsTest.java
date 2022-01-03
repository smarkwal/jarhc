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
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.testcontainers.shaded.org.apache.commons.io.FileUtils;
import org.testcontainers.shaded.org.apache.commons.lang.StringUtils;

class BuildArtifactsTest extends ReleaseTest {

	@Test
	void version() throws IOException {

		// get expected version from Gradle project file
		File buildFile = getProjectFile("build.gradle.kts");
		String buildScript = FileUtils.readFileToString(buildFile, StandardCharsets.UTF_8);
		String expectedVersion = StringUtils.substringBetween(buildScript, "\nversion = \"", "\"\n");

		assertEquals(expectedVersion, getJarHcVersion());
	}

}
