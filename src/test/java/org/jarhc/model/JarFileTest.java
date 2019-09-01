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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class JarFileTest {

	@Test
	void test_toString() {

		// prepare
		ArrayList<ClassDef> classDefs = new ArrayList<>();
		classDefs.add(buildClassDef("a.A"));
		classDefs.add(buildClassDef("b.B"));
		JarFile jarFile = JarFile.withName("abc.jar").withFileSize(1024).withClassDefs(classDefs).build();

		// test
		String result = jarFile.toString();

		// assert
		assertEquals("JarFile[abc.jar,2]", result);

	}

	@Test
	void test_getClassDefs() {

		// prepare
		ArrayList<ClassDef> classDefs = new ArrayList<>();
		classDefs.add(buildClassDef("a.A"));
		classDefs.add(buildClassDef("b.B"));
		JarFile jarFile = JarFile.withName("abc.jar").withFileSize(1024).withClassDefs(classDefs).build();

		// test
		List<ClassDef> result = jarFile.getClassDefs();

		// assert
		assertEquals(2, result.size());
		assertEquals("a.A", result.get(0).getClassName());
		assertEquals("b.B", result.get(1).getClassName());

	}

	@Test
	void test_getClassDef() {

		// prepare
		ArrayList<ClassDef> classDefs = new ArrayList<>();
		classDefs.add(buildClassDef("a.A"));
		classDefs.add(buildClassDef("b.B"));
		JarFile jarFile = JarFile.withName("abc.jar").withFileSize(1024).withClassDefs(classDefs).build();

		// test
		ClassDef result = jarFile.getClassDef("b.B");

		// assert
		assertNotNull(result);
		assertEquals("b.B", result.getClassName());

		// test
		result = jarFile.getClassDef("c.C");

		// assert
		assertNull(result);

	}

	private ClassDef buildClassDef(String className) {
		return ClassDef.forClassName(className);
	}

}
