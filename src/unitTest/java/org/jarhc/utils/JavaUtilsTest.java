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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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

}
