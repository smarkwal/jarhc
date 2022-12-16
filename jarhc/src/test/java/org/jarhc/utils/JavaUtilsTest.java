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

package org.jarhc.utils;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.jarhc.test.AssertUtils;
import org.junit.jupiter.api.Test;

class JavaUtilsTest {

	@Test
	void test_JavaUtils() {
		AssertUtils.assertUtilityClass(JavaUtils.class);
	}

	@Test
	void test_getPackageName() {

		assertEquals("", JavaUtils.getPackageName("A"));
		assertEquals("a", JavaUtils.getPackageName("a.B"));
		assertEquals("a.b", JavaUtils.getPackageName("a.b.C"));
		assertEquals("a.b.c", JavaUtils.getPackageName("a.b.c.D"));
		assertEquals("java.lang", JavaUtils.getPackageName("java.lang.String"));
		assertEquals("java.io", JavaUtils.getPackageName("java.io.InputStream"));
		assertEquals("a.b.c.d", JavaUtils.getPackageName("a.b.c.d.E$F"));

	}

	@Test
	void getParentPackageName() {

		assertEquals("", JavaUtils.getParentPackageName("", 0));
		assertEquals("", JavaUtils.getParentPackageName("", 1));
		assertEquals("", JavaUtils.getParentPackageName("", 2));
		assertEquals("", JavaUtils.getParentPackageName("", 3));

		assertEquals("", JavaUtils.getParentPackageName("com", 0));
		assertEquals("com", JavaUtils.getParentPackageName("com", 1));
		assertEquals("com", JavaUtils.getParentPackageName("com", 2));
		assertEquals("com", JavaUtils.getParentPackageName("com", 3));
		
		assertEquals("", JavaUtils.getParentPackageName("java.lang", 0));
		assertEquals("java", JavaUtils.getParentPackageName("java.lang", 1));
		assertEquals("java.lang", JavaUtils.getParentPackageName("java.lang", 2));
		assertEquals("java.lang", JavaUtils.getParentPackageName("java.lang", 3));
		assertEquals("java.lang", JavaUtils.getParentPackageName("java.lang", 4));

		assertEquals("", JavaUtils.getParentPackageName("a.b.c.d.e.f", 0));
		assertEquals("a", JavaUtils.getParentPackageName("a.b.c.d.e.f", 1));
		assertEquals("a.b", JavaUtils.getParentPackageName("a.b.c.d.e.f", 2));
		assertEquals("a.b.c", JavaUtils.getParentPackageName("a.b.c.d.e.f", 3));
		assertEquals("a.b.c.d", JavaUtils.getParentPackageName("a.b.c.d.e.f", 4));
		assertEquals("a.b.c.d.e", JavaUtils.getParentPackageName("a.b.c.d.e.f", 5));
		assertEquals("a.b.c.d.e.f", JavaUtils.getParentPackageName("a.b.c.d.e.f", 6));
		assertEquals("a.b.c.d.e.f", JavaUtils.getParentPackageName("a.b.c.d.e.f", 7));

	}

	@Test
	void inSamePackage() {

		assertTrue(JavaUtils.inSamePackage("Main", "Main"));
		assertTrue(JavaUtils.inSamePackage("java.lang.String", "java.lang.Integer"));
		assertTrue(JavaUtils.inSamePackage("org.jarhc.utils.JavaUtils", "org.jarhc.utils.FileUtils"));

		assertFalse(JavaUtils.inSamePackage("Main", "java.lang.String"));
		assertFalse(JavaUtils.inSamePackage("java.lang.String", "java.io.File"));
		assertFalse(JavaUtils.inSamePackage("a.b.C", "a.b.c.D"));

	}

	@Test
	void inSameTopLevelClass() {

		assertTrue(JavaUtils.inSameTopLevelClass("Main", "Main"));
		assertTrue(JavaUtils.inSameTopLevelClass("java.lang.String", "java.lang.String"));
		assertTrue(JavaUtils.inSameTopLevelClass("a.b.C", "a.b.C$D"));
		assertTrue(JavaUtils.inSameTopLevelClass("a.b.C", "a.b.C$D$E"));
		assertTrue(JavaUtils.inSameTopLevelClass("a.b.C$D", "a.b.C$E"));
		assertTrue(JavaUtils.inSameTopLevelClass("a.b.C$D$E", "a.b.C$D$F"));
		assertTrue(JavaUtils.inSameTopLevelClass("a.b.C$D$E", "a.b.C$F$G"));

		assertFalse(JavaUtils.inSameTopLevelClass("Main", "java.lang.String"));
		assertFalse(JavaUtils.inSameTopLevelClass("java.lang.String", "java.io.File"));
		assertFalse(JavaUtils.inSameTopLevelClass("a.b.C", "a.b.C2"));
		assertFalse(JavaUtils.inSameTopLevelClass("a.b.C$X", "a.b.D$X"));

	}

	@Test
	void getSimpleClassName() {

		assertEquals("A", JavaUtils.getSimpleClassName("A"));
		assertEquals("B", JavaUtils.getSimpleClassName("a.B"));
		assertEquals("C", JavaUtils.getSimpleClassName("a.b.C"));
		assertEquals("D", JavaUtils.getSimpleClassName("a.b.c.D"));
		assertEquals("String", JavaUtils.getSimpleClassName("java.lang.String"));
		assertEquals("InputStream", JavaUtils.getSimpleClassName("java.io.InputStream"));
		assertEquals("E$F", JavaUtils.getSimpleClassName("a.b.c.d.E$F"));

	}

	@Test
	void getArrayElementType() {

		assertEquals("int", JavaUtils.getArrayElementType("int[]"));
		assertEquals("java.lang.String", JavaUtils.getArrayElementType("java.lang.String[]"));
		assertEquals("java.lang.Object", JavaUtils.getArrayElementType("java.lang.Object[][]"));

	}

	@Test
	void getArrayElementType_throwsIllegalArgumentException_forNonArrayType() {

		assertThrows(
				IllegalArgumentException.class,
				() -> JavaUtils.getArrayElementType("byte"),
				"Not an array type: byte"
		);

	}

	@Test
	void getParameterTypes() {

		assertArrayEquals(new String[0], JavaUtils.getParameterTypes("()V"));
		assertArrayEquals(new String[] { "int" }, JavaUtils.getParameterTypes("(I)V"));
		assertArrayEquals(new String[] { "int", "boolean" }, JavaUtils.getParameterTypes("(IZ)V"));
		assertArrayEquals(new String[] { "long[]", "byte[][]", "double[][][]" }, JavaUtils.getParameterTypes("([J[[B[[[D)V"));
		assertArrayEquals(new String[] { "java.lang.String", "java.util.List", "java.io.File[]" }, JavaUtils.getParameterTypes("(Ljava/lang/String;Ljava/util/List;[Ljava/io/File;)V"));

	}

}
