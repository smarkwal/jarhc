/*
 * Copyright 2018 Stephan Markwalder
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

package org.jarhc.loader;

import org.jarhc.TestUtils;
import org.jarhc.model.JarFile;
import org.jarhc.model.ModuleInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junitpioneer.jupiter.TempDirectory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.junitpioneer.jupiter.TempDirectory.TempDir;

@ExtendWith(TempDirectory.class)
class JarFileLoaderTest {

	private final JarFileLoader jarFileLoader = LoaderBuilder.create().buildJarFileLoader();

	@Test
	void test_load(@TempDir Path tempDir) throws IOException {

		// prepare
		String resource = "/JarFileLoaderTest/a.jar";
		File file = TestUtils.getResourceAsFile(resource, tempDir);

		// test
		JarFile jarFile = jarFileLoader.load(file);

		// assert
		assertNotNull(jarFile);
		assertEquals(file.getName(), jarFile.getFileName());
		assertEquals(1, jarFile.getClassDefs().size());
		assertEquals("a/A", jarFile.getClassDefs().get(0).getClassName());

		assertFalse(jarFile.isMultiRelease());
		Set<Integer> releases = jarFile.getReleases();
		assertNotNull(releases);
		assertEquals(0, releases.size());

		assertFalse(jarFile.isModule());
		assertNull(jarFile.getModuleInfo());

	}

	@Test
	void test_load_multi_release(@TempDir Path tempDir) throws IOException {

		// prepare
		String resource = "/JarFileLoaderTest/b.jar";
		File file = TestUtils.getResourceAsFile(resource, tempDir);

		// test
		JarFile jarFile = jarFileLoader.load(file);

		// assert
		assertNotNull(jarFile);
		assertEquals(file.getName(), jarFile.getFileName());
		assertEquals(1, jarFile.getClassDefs().size());
		assertEquals("b/B", jarFile.getClassDefs().get(0).getClassName());

		assertTrue(jarFile.isMultiRelease());
		Set<Integer> releases = jarFile.getReleases();
		assertNotNull(releases);
		assertEquals(1, releases.size());
		assertTrue(releases.contains(11));

		assertFalse(jarFile.isModule());
		assertNull(jarFile.getModuleInfo());

	}

	@Test
	void test_load_module(@TempDir Path tempDir) throws IOException {

		// prepare
		String resource = "/JarFileLoaderTest/c.jar";
		File file = TestUtils.getResourceAsFile(resource, tempDir);

		// test
		JarFile jarFile = jarFileLoader.load(file);

		// assert
		assertNotNull(jarFile);
		assertEquals(file.getName(), jarFile.getFileName());
		assertEquals(2, jarFile.getClassDefs().size());
		assertEquals("c/C", jarFile.getClassDefs().get(0).getClassName());
		assertEquals("module-info", jarFile.getClassDefs().get(1).getClassName());

		assertFalse(jarFile.isMultiRelease());
		Set<Integer> releases = jarFile.getReleases();
		assertNotNull(releases);
		assertEquals(0, releases.size());

		assertTrue(jarFile.isModule());
		ModuleInfo moduleInfo = jarFile.getModuleInfo();
		assertNotNull(moduleInfo);
		assertEquals("c", moduleInfo.getModuleName());
		List<String> exports = moduleInfo.getExports();
		assertEquals(1, exports.size());
		assertEquals("c", exports.get(0));
		List<String> requires = moduleInfo.getRequires();
		assertEquals(2, requires.size());
		assertEquals("java.base", requires.get(0));
		assertEquals("b", requires.get(1));

	}

}
