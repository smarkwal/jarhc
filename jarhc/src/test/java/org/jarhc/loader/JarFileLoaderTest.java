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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import org.jarhc.TestUtils;
import org.jarhc.model.JarFile;
import org.jarhc.model.ModuleInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class JarFileLoaderTest {

	private final JarFileLoader jarFileLoader = LoaderBuilder.create().buildJarFileLoader();

	@Test
	void test_load(@TempDir Path tempDir) throws IOException {

		// prepare
		String resource = "/org/jarhc/loader/JarFileLoaderTest/a.jar";
		File file = TestUtils.getResourceAsFile(resource, tempDir);

		// test
		List<JarFile> jarFiles = jarFileLoader.load(file);

		// assert
		assertNotNull(jarFiles);
		assertEquals(1, jarFiles.size());
		JarFile jarFile = jarFiles.get(0);
		assertNotNull(jarFile);
		assertEquals(file.getName(), jarFile.getFileName());
		assertEquals(1, jarFile.getClassDefs().size());
		assertEquals("a.A", jarFile.getClassDefs().get(0).getClassName());

		assertFalse(jarFile.isMultiRelease());
		Set<Integer> releases = jarFile.getReleases();
		assertNotNull(releases);
		assertEquals(0, releases.size());

		ModuleInfo moduleInfo = jarFile.getModuleInfo();
		assertNotNull(moduleInfo);
		assertTrue(moduleInfo.isUnnamed());

	}

	@Test
	void test_load_multi_release(@TempDir Path tempDir) throws IOException {

		// prepare
		String resource = "/org/jarhc/loader/JarFileLoaderTest/b.jar";
		File file = TestUtils.getResourceAsFile(resource, tempDir);

		// test
		List<JarFile> jarFiles = jarFileLoader.load(file);

		// assert
		assertNotNull(jarFiles);
		assertEquals(1, jarFiles.size());
		JarFile jarFile = jarFiles.get(0);
		assertNotNull(jarFile);
		assertEquals(file.getName(), jarFile.getFileName());
		assertEquals(1, jarFile.getClassDefs().size());
		assertEquals("b.B", jarFile.getClassDefs().get(0).getClassName());

		assertTrue(jarFile.isMultiRelease());
		Set<Integer> releases = jarFile.getReleases();
		assertNotNull(releases);
		assertEquals(1, releases.size());
		assertTrue(releases.contains(11));

		ModuleInfo moduleInfo = jarFile.getModuleInfo();
		assertNotNull(moduleInfo);
		assertTrue(moduleInfo.isUnnamed());


	}

	@Test
	void test_load_module(@TempDir Path tempDir) throws IOException {

		// prepare
		String resource = "/org/jarhc/loader/JarFileLoaderTest/c.jar";
		File file = TestUtils.getResourceAsFile(resource, tempDir);

		// test
		List<JarFile> jarFiles = jarFileLoader.load(file);

		// assert
		assertNotNull(jarFiles);
		assertEquals(1, jarFiles.size());
		JarFile jarFile = jarFiles.get(0);
		assertNotNull(jarFile);
		assertEquals(file.getName(), jarFile.getFileName());
		assertEquals(2, jarFile.getClassDefs().size());
		assertEquals("c.C", jarFile.getClassDefs().get(0).getClassName());
		assertEquals("module-info", jarFile.getClassDefs().get(1).getClassName());

		assertFalse(jarFile.isMultiRelease());
		Set<Integer> releases = jarFile.getReleases();
		assertNotNull(releases);
		assertEquals(0, releases.size());

		ModuleInfo moduleInfo = jarFile.getModuleInfo();
		assertNotNull(moduleInfo);
		assertTrue(moduleInfo.isNamed());
		assertEquals("c", moduleInfo.getModuleName());
		List<String> exports = moduleInfo.getExports();
		assertEquals(1, exports.size());
		assertEquals("c", exports.get(0));
		List<String> requires = moduleInfo.getRequires();
		assertEquals(2, requires.size());
		assertEquals("java.base", requires.get(0));
		assertEquals("b", requires.get(1));

	}

	@Test
	void test_load_jar_in_jar(@TempDir Path tempDir) throws IOException {

		// prepare
		String resource = "/org/jarhc/loader/JarFileLoaderTest/x.jar";
		File file = TestUtils.getResourceAsFile(resource, tempDir);

		// test
		List<JarFile> jarFiles = jarFileLoader.load(file);

		// assert
		assertNotNull(jarFiles);
		assertEquals(3, jarFiles.size());

		JarFile jarFile = jarFiles.get(0);
		assertNotNull(jarFile);
		assertEquals(file.getName(), jarFile.getFileName());
		assertEquals(1, jarFile.getClassDefs().size());
		assertEquals("c.C", jarFile.getClassDefs().get(0).getClassName());
		assertFalse(jarFile.isMultiRelease());
		Set<Integer> releases = jarFile.getReleases();
		assertNotNull(releases);
		assertEquals(0, releases.size());
		ModuleInfo moduleInfo = jarFile.getModuleInfo();
		assertNotNull(moduleInfo);
		assertTrue(moduleInfo.isUnnamed());

		jarFile = jarFiles.get(1);
		assertNotNull(jarFile);
		assertEquals("x.jar!/a.jar", jarFile.getFileName());
		assertEquals(1, jarFile.getClassDefs().size());
		assertEquals("a.A", jarFile.getClassDefs().get(0).getClassName());
		assertFalse(jarFile.isMultiRelease());
		releases = jarFile.getReleases();
		assertNotNull(releases);
		assertEquals(0, releases.size());
		moduleInfo = jarFile.getModuleInfo();
		assertNotNull(moduleInfo);
		assertTrue(moduleInfo.isUnnamed());

		jarFile = jarFiles.get(2);
		assertNotNull(jarFile);
		assertEquals("x.jar!/b.jar", jarFile.getFileName());
		assertEquals(1, jarFile.getClassDefs().size());
		assertEquals("b.B", jarFile.getClassDefs().get(0).getClassName());
		assertTrue(jarFile.isMultiRelease());
		releases = jarFile.getReleases();
		assertNotNull(releases);
		assertEquals(1, releases.size());
		moduleInfo = jarFile.getModuleInfo();
		assertNotNull(moduleInfo);
		assertTrue(moduleInfo.isUnnamed());

	}

}
