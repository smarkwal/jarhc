/*
 * Copyright 2025 Stephan Markwalder
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

package org.jarhc.app;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class PropertiesManagerImplTest {

	private final PropertiesManager propertiesManager = new PropertiesManagerImpl();

	@Test
	void test(@TempDir Path tempDir) throws IOException {

		// override user.home property
		String userHome = System.getProperty("user.home");
		try {
			System.setProperty("user.home", tempDir.toString());

			// prepare
			Path path = tempDir.resolve(".jarhc").resolve("jarhc.properties");
			Files.createDirectories(path.getParent());
			Files.writeString(path, "aaa = AAA\nbbb = BBB\nccc = CCC");

			// test
			Properties properties = propertiesManager.loadProperties();

			// assert
			assertEquals(3, properties.size());
			assertEquals("AAA", properties.getProperty("aaa"));
			assertEquals("BBB", properties.getProperty("bbb"));
			assertEquals("CCC", properties.getProperty("ccc"));

		} finally {
			// restore user.home property
			System.setProperty("user.home", userHome);
		}

	}

}
