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

package org.jarhc.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class MethodRefTest {

	@Test
	void test_constructor_getters() {

		// test
		MethodRef methodRef = new MethodRef("org.test.Test", "(Ljava/lang/String;Ljava/lang/String;)I", "compareNames", true, true);

		// assert
		assertEquals("org.test.Test", methodRef.getMethodOwner());
		assertEquals("(Ljava/lang/String;Ljava/lang/String;)I", methodRef.getMethodDescriptor());
		assertEquals("compareNames", methodRef.getMethodName());
		assertTrue(methodRef.isInterfaceMethod());
		assertTrue(methodRef.isStaticAccess());

		assertEquals("static int org.test.Test.compareNames(java.lang.String,java.lang.String)", methodRef.getDisplayName());
		assertEquals("MethodRef[static int org.test.Test.compareNames(java.lang.String,java.lang.String)]", methodRef.toString());

		// test
		methodRef = new MethodRef("java.util.HashMap", "()V", "<init>", false, false);

		// assert
		assertEquals("java.util.HashMap", methodRef.getMethodOwner());
		assertEquals("()V", methodRef.getMethodDescriptor());
		assertEquals("<init>", methodRef.getMethodName());
		assertFalse(methodRef.isInterfaceMethod());
		assertFalse(methodRef.isStaticAccess());

		assertEquals("void java.util.HashMap.<init>()", methodRef.getDisplayName());
		assertEquals("MethodRef[void java.util.HashMap.<init>()]", methodRef.toString());

	}

	@Test
	void test_equals() {

		MethodRef methodRef1 = new MethodRef("java.util.HashMap", "()V", "<init>", false, false);
		MethodRef methodRef2 = new MethodRef("java.util.HashMap", "()V", "<init>", false, false);
		assertEquals(methodRef1, methodRef2);

		methodRef1 = new MethodRef("java.util.HashMap", "()V", "<init>", false, false);
		methodRef2 = new MethodRef("java.util.TreeMap", "()V", "<init>", false, false);
		assertNotEquals(methodRef1, methodRef2);

		methodRef1 = new MethodRef("java.util.HashMap", "()V", "<init>", false, false);
		methodRef2 = new MethodRef("java.util.HashMap", "(I)V", "<init>", false, false);
		assertNotEquals(methodRef1, methodRef2);

		methodRef1 = new MethodRef("java.util.HashMap", "()V", "<init>", false, false);
		methodRef2 = new MethodRef("java.util.HashMap", "()V", "clear", false, false);
		assertNotEquals(methodRef1, methodRef2);

		methodRef1 = new MethodRef("java.util.HashMap", "()V", "<init>", false, false);
		methodRef2 = new MethodRef("java.util.HashMap", "()V", "<init>", true, false);
		assertNotEquals(methodRef1, methodRef2);

		methodRef1 = new MethodRef("java.util.HashMap", "()V", "<init>", false, false);
		methodRef2 = new MethodRef("java.util.HashMap", "()V", "<init>", false, true);
		assertNotEquals(methodRef1, methodRef2);

	}

	@Test
	void test_hashCode() {

		MethodRef methodRef1 = new MethodRef("java.util.HashMap", "()V", "<init>", false, false);
		MethodRef methodRef2 = new MethodRef("java.util.HashMap", "()V", "<init>", false, false);
		assertEquals(methodRef1.hashCode(), methodRef2.hashCode());

		methodRef1 = new MethodRef("java.util.HashMap", "()V", "<init>", false, false);
		methodRef2 = new MethodRef("java.util.TreeMap", "()V", "<init>", false, false);
		assertNotEquals(methodRef1.hashCode(), methodRef2.hashCode());

		methodRef1 = new MethodRef("java.util.HashMap", "()V", "<init>", false, false);
		methodRef2 = new MethodRef("java.util.HashMap", "(I)V", "<init>", false, false);
		assertNotEquals(methodRef1.hashCode(), methodRef2.hashCode());

		methodRef1 = new MethodRef("java.util.HashMap", "()V", "<init>", false, false);
		methodRef2 = new MethodRef("java.util.HashMap", "()V", "clear", false, false);
		assertNotEquals(methodRef1.hashCode(), methodRef2.hashCode());

		methodRef1 = new MethodRef("java.util.HashMap", "()V", "<init>", false, false);
		methodRef2 = new MethodRef("java.util.HashMap", "()V", "<init>", true, false);
		assertNotEquals(methodRef1.hashCode(), methodRef2.hashCode());

		methodRef1 = new MethodRef("java.util.HashMap", "()V", "<init>", false, false);
		methodRef2 = new MethodRef("java.util.HashMap", "()V", "<init>", false, true);
		assertNotEquals(methodRef1.hashCode(), methodRef2.hashCode());

	}

	@Test
	void compareTo() {

		MethodRef methodRef1 = new MethodRef("java.util.HashMap", "()V", "<init>", false, false);
		MethodRef methodRef2 = new MethodRef("java.util.HashMap", "()V", "<init>", false, false);
		assertEquals(0, methodRef1.compareTo(methodRef2));
		assertEquals(0, methodRef2.compareTo(methodRef1));

		methodRef1 = new MethodRef("java.util.HashMap", "()V", "<init>", false, false);
		methodRef2 = new MethodRef("java.util.TreeMap", "()V", "<init>", false, false);
		assertTrue(methodRef1.compareTo(methodRef2) < 0);
		assertTrue(methodRef2.compareTo(methodRef1) > 0);

		methodRef1 = new MethodRef("java.util.HashMap", "()V", "<init>", false, false);
		methodRef2 = new MethodRef("java.util.HashMap", "(I)V", "<init>", false, false);
		assertTrue(methodRef1.compareTo(methodRef2) < 0);
		assertTrue(methodRef2.compareTo(methodRef1) > 0);

		methodRef1 = new MethodRef("java.util.HashMap", "()V", "<init>", false, false);
		methodRef2 = new MethodRef("java.util.HashMap", "()V", "clear", false, false);
		assertTrue(methodRef1.compareTo(methodRef2) < 0);
		assertTrue(methodRef2.compareTo(methodRef1) > 0);

		methodRef1 = new MethodRef("java.util.HashMap", "()V", "<init>", false, false);
		methodRef2 = new MethodRef("java.util.HashMap", "()V", "<init>", true, false);
		assertTrue(methodRef1.compareTo(methodRef2) < 0);
		assertTrue(methodRef2.compareTo(methodRef1) > 0);

		methodRef1 = new MethodRef("java.util.HashMap", "()V", "<init>", false, false);
		methodRef2 = new MethodRef("java.util.HashMap", "()V", "<init>", false, true);
		assertTrue(methodRef1.compareTo(methodRef2) < 0);
		assertTrue(methodRef2.compareTo(methodRef1) > 0);

	}

}