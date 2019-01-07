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

package org.jarhc.model;

import org.jarhc.test.ClasspathBuilder;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ClasspathTest {

	@Test
	void test_toString() {

		// prepare
		Classpath classpath = ClasspathBuilder.create()
				.addJarFile("a.jar").addClassDef("a.A")
				.addJarFile("b.jar").addClassDef("b.B")
				.build();

		// test
		String result = classpath.toString();

		// assert
		assertEquals("Classpath[2]", result);

	}

	@Test
	void test_getJarFiles() {

		// prepare
		Classpath classpath = ClasspathBuilder.create()
				.addJarFile("a.jar").addClassDef("a.A")
				.addJarFile("b.jar").addClassDef("b.B")
				.build();

		// test
		List<JarFile> result = classpath.getJarFiles();

		// assert
		assertNotNull(result);
		assertEquals(2, result.size());
		assertEquals("a.jar", result.get(0).getFileName());
		assertEquals("b.jar", result.get(1).getFileName());

	}

	@Test
	void test_getJarFile() {

		// prepare
		Classpath classpath = ClasspathBuilder.create()
				.addJarFile("a.jar").addClassDef("a.A")
				.addJarFile("b.jar").addClassDef("b.B")
				.build();

		// test
		JarFile result = classpath.getJarFile("b.jar");

		// assert
		assertNotNull(result);
		assertEquals("b.jar", result.getFileName());

		// test
		result = classpath.getJarFile("c.jar");

		// assert
		assertNull(result);

	}

	@Test
	void test_getClassDefs() {

		// prepare
		Classpath classpath = ClasspathBuilder.create()
				.addJarFile("a.jar").addClassDef("a.A")
				.addJarFile("b.jar").addClassDef("b.B")
				.build();

		// test
		Set<ClassDef> result = classpath.getClassDefs("a.A");

		// assert
		assertNotNull(result);
		assertEquals(1, result.size());

	}

}
