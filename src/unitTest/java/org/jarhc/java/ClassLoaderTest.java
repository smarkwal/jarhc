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

package org.jarhc.java;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.function.Predicate;
import org.jarhc.model.ClassDef;
import org.jarhc.model.ClassRef;
import org.jarhc.model.JarFile;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class ClassLoaderTest {

	@Mock
	ClassLoader parentClassLoader;

	private AutoCloseable mocks;

	@BeforeEach
	void setUp() {
		mocks = MockitoAnnotations.openMocks(this);

		JarFile jarFile = JarFile.withName("parent.jar").build();
		when(parentClassLoader.getJarFile(any())).thenAnswer((invocation) -> {
			Predicate<JarFile> predicate = invocation.getArgument(0);
			if (predicate.test(jarFile)) {
				return jarFile;
			} else {
				return null;
			}
		});

		when(parentClassLoader.getClassDef(anyString())).thenAnswer((invocation) -> {
			String className = invocation.getArgument(0);
			if (className.startsWith("parent.")) {
				ClassDef classDef = new ClassDef(className);
				classDef.setClassLoader("Parent");
				return classDef;
			} else {
				return null;
			}
		});

		when(parentClassLoader.containsPackage("parent")).thenReturn(true);

	}

	@AfterEach
	void tearDown() throws Exception {
		mocks.close();
	}

	@Test
	void containsPackage_parentClass() {

		// prepare
		ClassLoader classLoader = new TestClassLoader(parentClassLoader, ClassLoaderStrategy.ParentFirst);

		// test
		boolean result = classLoader.containsPackage("parent");

		// assert
		assertTrue(result);

	}

	@Test
	void containsPackage_localClass() {

		// prepare
		ClassLoader classLoader = new TestClassLoader(parentClassLoader, ClassLoaderStrategy.ParentFirst);

		// test
		boolean result = classLoader.containsPackage("local");

		// assert
		assertTrue(result);

	}

	@Test
	void containsPackage_unknownClass() {

		// prepare
		ClassLoader classLoader = new TestClassLoader(parentClassLoader, ClassLoaderStrategy.ParentFirst);

		// test
		boolean result = classLoader.containsPackage("unknown");

		// assert
		assertFalse(result);

	}

	@Test
	void containsPackage_unknownClass_withoutParent() {

		// prepare
		ClassLoader classLoader = new TestClassLoader(null, ClassLoaderStrategy.ParentFirst);

		// test
		boolean result = classLoader.containsPackage("unknown");

		// assert
		assertFalse(result);

	}

	@Test
	void getClassDef_withClassRef() {

		// prepare
		ClassLoader classLoader = new TestClassLoader(null, ClassLoaderStrategy.ParentLast);
		ClassRef classRef = new ClassRef("local.Local");

		// test
		ClassDef classDef = classLoader.getClassDef(classRef);

		// assert
		assertNotNull(classDef);
		assertEquals("local.Local", classDef.getClassName());

	}

	@Test
	void getClassDef_parentFirst_parentClass() {

		// prepare
		ClassLoader classLoader = new TestClassLoader(parentClassLoader, ClassLoaderStrategy.ParentFirst);

		// test
		ClassDef classDef = classLoader.getClassDef("parent.Parent");

		// assert
		assertNotNull(classDef);
		assertEquals("Parent", classDef.getClassLoader());

	}

	@Test
	void getClassDef_parentFirst_localClass() {

		// prepare
		ClassLoader classLoader = new TestClassLoader(parentClassLoader, ClassLoaderStrategy.ParentFirst);

		// test
		ClassDef classDef = classLoader.getClassDef("local.Local");

		// assert
		assertNotNull(classDef);
		assertEquals("Local", classDef.getClassLoader());

	}

	@Test
	void getClassDef_parentFirst_unknownClass() {

		// prepare
		ClassLoader classLoader = new TestClassLoader(parentClassLoader, ClassLoaderStrategy.ParentFirst);

		// test
		ClassDef classDef = classLoader.getClassDef("unknown.Unknown");

		// assert
		assertNull(classDef);

	}

	@Test
	void getClassDef_parentLast_parentClass() {

		// prepare
		ClassLoader classLoader = new TestClassLoader(parentClassLoader, ClassLoaderStrategy.ParentLast);

		// test
		ClassDef classDef = classLoader.getClassDef("parent.Parent");

		// assert
		assertNotNull(classDef);
		assertEquals("Parent", classDef.getClassLoader());

	}

	@Test
	void getClassDef_parentLast_localClass() {

		// prepare
		ClassLoader classLoader = new TestClassLoader(parentClassLoader, ClassLoaderStrategy.ParentLast);

		// test
		ClassDef classDef = classLoader.getClassDef("local.Local");

		// assert
		assertNotNull(classDef);
		assertEquals("Local", classDef.getClassLoader());

	}

	@Test
	void getClassDef_parentLast_unknownClass() {

		// prepare
		ClassLoader classLoader = new TestClassLoader(parentClassLoader, ClassLoaderStrategy.ParentLast);

		// test
		ClassDef classDef = classLoader.getClassDef("unknown.Unknown");

		// assert
		assertNull(classDef);

	}

	@Test
	void getJarFile_parentFirst_parentJar() {

		// prepare
		ClassLoader classLoader = new TestClassLoader(parentClassLoader, ClassLoaderStrategy.ParentFirst);

		// test
		JarFile jarFile = classLoader.getJarFile(f -> f.getFileName().contains("parent"));

		// assert
		assertNotNull(jarFile);
		assertEquals("parent.jar", jarFile.getFileName());

	}

	@Test
	void getJarFile_parentFirst_localJar() {

		// prepare
		ClassLoader classLoader = new TestClassLoader(parentClassLoader, ClassLoaderStrategy.ParentFirst);

		// test
		JarFile jarFile = classLoader.getJarFile(f -> f.getFileName().contains("local"));

		// assert
		assertNotNull(jarFile);
		assertEquals("local.jar", jarFile.getFileName());

	}

	@Test
	void getJarFile_parentFirst_unknownJar() {

		// prepare
		ClassLoader classLoader = new TestClassLoader(parentClassLoader, ClassLoaderStrategy.ParentFirst);

		// test
		JarFile jarFile = classLoader.getJarFile(f -> false);

		// assert
		assertNull(jarFile);

	}

	@Test
	void getJarFile_parentLast_parentJar() {

		// prepare
		ClassLoader classLoader = new TestClassLoader(parentClassLoader, ClassLoaderStrategy.ParentLast);

		// test
		JarFile jarFile = classLoader.getJarFile(f -> f.getFileName().contains("parent"));

		// assert
		assertNotNull(jarFile);
		assertEquals("parent.jar", jarFile.getFileName());

	}

	@Test
	void getJarFile_parentLast_localJar() {

		// prepare
		ClassLoader classLoader = new TestClassLoader(parentClassLoader, ClassLoaderStrategy.ParentLast);

		// test
		JarFile jarFile = classLoader.getJarFile(f -> f.getFileName().contains("local"));

		// assert
		assertNotNull(jarFile);
		assertEquals("local.jar", jarFile.getFileName());

	}

	@Test
	void getJarFile_parentLast_unknownJar() {

		// prepare
		ClassLoader classLoader = new TestClassLoader(parentClassLoader, ClassLoaderStrategy.ParentLast);

		// test
		JarFile jarFile = classLoader.getJarFile(f -> false);

		// assert
		assertNull(jarFile);

	}

	private static class TestClassLoader extends ClassLoader {

		private final JarFile jarFile = JarFile.withName("local.jar").build();

		TestClassLoader(ClassLoader parent, ClassLoaderStrategy strategy) {
			super("Local", parent, strategy);
		}

		@Override
		public JarFile findJarFile(Predicate<JarFile> predicate) {
			if (predicate.test(jarFile)) {
				return jarFile;
			} else {
				return null;
			}
		}

		@Override
		protected boolean findPackage(String packageName) {
			return packageName.equals("local");
		}

		@Override
		protected ClassDef findClassDef(String className) {
			if (className.startsWith("local.")) {
				ClassDef classDef = new ClassDef(className);
				classDef.setClassLoader("Local");
				return classDef;
			} else {
				return null;
			}
		}

	}

}