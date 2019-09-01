/*
 * Copyright 2019 Stephan Markwalder
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import org.jarhc.TestUtils;
import org.jarhc.app.FileSource;
import org.jarhc.app.JarSource;
import org.jarhc.model.JarFile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.stubbing.Answer;

class WarFileLoaderTest {

	@Mock
	JarFileLoader jarFileLoader;

	@BeforeEach
	void setUp() throws IOException {
		MockitoAnnotations.initMocks(this);

		// JarFileLoader mock creates a dummy JarFile whenever it is invoked
		when(jarFileLoader.load(anyString(), any()))
				.thenAnswer((Answer<List<JarFile>>) invocation ->
				{
					String fileName = invocation.getArgument(0);
					JarFile jarFile = JarFile.withName(fileName).build();
					return Collections.singletonList(jarFile);
				});
	}

	@Test
	void load_File_throwsIllegalArgumentException_forNullValue() {

		// prepare
		WarFileLoader loader = new WarFileLoader(jarFileLoader);

		// test
		assertThrows(IllegalArgumentException.class, () -> loader.load((File) null));

	}

	@Test
	void load_File_throwsFileNotFoundException_ifFileDoesNotExist() {

		// prepare
		WarFileLoader loader = new WarFileLoader(jarFileLoader);
		File file = new File("unknown.war");

		// test
		assertThrows(FileNotFoundException.class, () -> loader.load(file));

	}

	@Test
	void load_File(@TempDir Path tempDir) throws IOException {

		// prepare
		WarFileLoader loader = new WarFileLoader(jarFileLoader);
		File file = TestUtils.getResourceAsFile("/WarFileLoaderTest/test.war", tempDir);

		// test
		List<JarFile> result = loader.load(file);

		// assert
		assertEquals(4, result.size());
		assertEquals("a.jar", result.get(0).getFileName());
		assertEquals("b.jar", result.get(1).getFileName());
		assertEquals("c.jar", result.get(2).getFileName());
		assertEquals("x.jar", result.get(3).getFileName());

	}

	@Test
	void load_JarSource_throwsIllegalArgumentException_forNullValue() {

		// prepare
		WarFileLoader loader = new WarFileLoader(jarFileLoader);

		// test
		assertThrows(IllegalArgumentException.class, () -> loader.load((JarSource) null));

	}

	@Test
	void load_JarSource(@TempDir Path tempDir) throws IOException {

		// prepare
		WarFileLoader loader = new WarFileLoader(jarFileLoader);
		File file = TestUtils.getResourceAsFile("/WarFileLoaderTest/test.war", tempDir);
		JarSource jarSource = new FileSource(file);

		// test
		List<JarFile> result = loader.load(jarSource);

		// assert
		assertEquals(4, result.size());
		assertEquals("a.jar", result.get(0).getFileName());
		assertEquals("b.jar", result.get(1).getFileName());
		assertEquals("c.jar", result.get(2).getFileName());
		assertEquals("x.jar", result.get(3).getFileName());

	}

	@Test
	void load_InputStream_throwsIllegalArgumentException_forNullValue() {

		// prepare
		WarFileLoader loader = new WarFileLoader(jarFileLoader);

		// test
		assertThrows(IllegalArgumentException.class, () -> loader.load((InputStream) null));

	}

	@Test
	void load_InputStream(@TempDir Path tempDir) throws IOException {

		// prepare
		WarFileLoader loader = new WarFileLoader(jarFileLoader);
		File file = TestUtils.getResourceAsFile("/WarFileLoaderTest/test.war", tempDir);

		List<JarFile> result;
		try (InputStream stream = new FileInputStream(file)) {

			// test
			result = loader.load(stream);

		}

		// assert
		assertEquals(4, result.size());
		assertEquals("a.jar", result.get(0).getFileName());
		assertEquals("b.jar", result.get(1).getFileName());
		assertEquals("c.jar", result.get(2).getFileName());
		assertEquals("x.jar", result.get(3).getFileName());

	}

}