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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.jarhc.TestUtils;
import org.jarhc.app.FileSource;
import org.jarhc.app.JarSource;
import org.jarhc.java.ClassLoader;
import org.jarhc.java.ClassLoaderStrategy;
import org.jarhc.model.Classpath;
import org.jarhc.model.JarFile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.stubbing.Answer;

class ClasspathLoaderTest {

	@Mock
	JarFileLoader jarFileLoader;

	@Mock
	WarFileLoader warFileLoader;

	@Mock
	ClassLoader parentClassLoader;

	ClasspathLoader classpathLoader;

	@BeforeEach
	void setUp() throws IOException {

		MockitoAnnotations.initMocks(this);

		// JarFileLoader mock creates a dummy JarFile whenever it is invoked
		when(jarFileLoader.load(any(JarSource.class)))
				.thenAnswer((Answer<List<JarFile>>) invocation ->
				{
					JarSource jarSource = invocation.getArgument(0);
					String fileName = jarSource.getName();
					if (fileName.equals("not-found.jar")) {
						throw new FileNotFoundException(fileName);
					}
					JarFile jarFile = JarFile.withName(fileName).build();
					return Collections.singletonList(jarFile);
				});

		// WarFileLoader mock creates two dummy JarFile whenever it is invoked
		when(warFileLoader.load(any(JarSource.class)))
				.thenAnswer((Answer<List<JarFile>>) invocation ->
				{
					JarSource jarSource = invocation.getArgument(0);
					String fileName = jarSource.getName();
					JarFile jarFile1 = JarFile.withName(fileName.replaceAll(".war", "-1.jar")).build();
					JarFile jarFile2 = JarFile.withName(fileName.replaceAll(".war", "-2.jar")).build();
					return Arrays.asList(jarFile1, jarFile2);
				});

		classpathLoader = new ClasspathLoader(jarFileLoader, warFileLoader, parentClassLoader, ClassLoaderStrategy.ParentLast);
	}

	@Test
	void test_load_file(@TempDir Path tempDir) throws IOException {

		// prepare
		classpathLoader = LoaderBuilder.create().buildClasspathLoader();
		String resource = "/ClasspathLoaderTest/a.jar";
		File file = TestUtils.getResourceAsFile(resource, tempDir);
		List<File> files = Collections.singletonList(file);

		// test
		Classpath classpath = classpathLoader.load(files);

		// assert
		assertNotNull(classpath);
		assertEquals(1, classpath.getJarFiles().size());
		assertEquals(file.getName(), classpath.getJarFiles().get(0).getFileName());
	}

	@Test
	void load_Files() {

		// prepare
		Collection<File> files = Arrays.asList(
				new File("slf4j.jar"),
				new File("test.war"),
				new File("not-found.jar"),
				new File("asm.jar"),
				new File("run.sh")
		);

		// test
		Classpath result = classpathLoader.load(files);

		// assert
		List<JarFile> jarFiles = result.getJarFiles();
		assertEquals(4, jarFiles.size());
		assertEquals("slf4j.jar", jarFiles.get(0).getFileName());
		assertEquals("test-1.jar", jarFiles.get(1).getFileName());
		assertEquals("test-2.jar", jarFiles.get(2).getFileName());
		assertEquals("asm.jar", jarFiles.get(3).getFileName());

	}

	@Test
	void load_JarSources() {

		// prepare
		List<JarSource> jarSources = Arrays.asList(
				new FileSource(new File("slf4j.jar")),
				new FileSource(new File("test.war")),
				new FileSource(new File("not-found.jar")),
				new FileSource(new File("asm.jar")),
				new FileSource(new File("run.sh"))
		);

		// test
		Classpath result = classpathLoader.load(jarSources);

		// assert
		List<JarFile> jarFiles = result.getJarFiles();
		assertEquals(4, jarFiles.size());
		assertEquals("slf4j.jar", jarFiles.get(0).getFileName());
		assertEquals("test-1.jar", jarFiles.get(1).getFileName());
		assertEquals("test-2.jar", jarFiles.get(2).getFileName());
		assertEquals("asm.jar", jarFiles.get(3).getFileName());

	}

}
