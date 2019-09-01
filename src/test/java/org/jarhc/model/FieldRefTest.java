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

class FieldRefTest {

	@Test
	void test_constructor_getters() {

		// test
		FieldRef fieldRef = new FieldRef("org.test.Test", "java.lang.String", "NAME", true, false);

		// assert
		assertEquals("org.test.Test", fieldRef.getFieldOwner());
		assertEquals("java.lang.String", fieldRef.getFieldType());
		assertEquals("NAME", fieldRef.getFieldName());
		assertTrue(fieldRef.isStaticAccess());
		assertFalse(fieldRef.isWriteAccess());
		assertTrue(fieldRef.isReadAccess());

		assertEquals("static java.lang.String org.test.Test.NAME", fieldRef.getDisplayName());
		assertEquals("FieldRef[static java.lang.String org.test.Test.NAME]", fieldRef.toString());

		// test
		fieldRef = new FieldRef("java.lang.Boolean", "boolean", "value", false, true);

		// assert
		assertEquals("java.lang.Boolean", fieldRef.getFieldOwner());
		assertEquals("boolean", fieldRef.getFieldType());
		assertEquals("value", fieldRef.getFieldName());
		assertFalse(fieldRef.isStaticAccess());
		assertTrue(fieldRef.isWriteAccess());
		assertFalse(fieldRef.isReadAccess());

		assertEquals("boolean java.lang.Boolean.value", fieldRef.getDisplayName());
		assertEquals("FieldRef[boolean java.lang.Boolean.value]", fieldRef.toString());

	}

	@Test
	void test_equals() {

		FieldRef fieldRef1 = new FieldRef("java.lang.Boolean", "boolean", "value", false, true);
		FieldRef fieldRef2 = new FieldRef("java.lang.Boolean", "boolean", "value", false, true);
		assertEquals(fieldRef1, fieldRef2);

		fieldRef1 = new FieldRef("java.lang.Boolean", "boolean", "value", false, true);
		fieldRef2 = new FieldRef("java.lang.AtomicBoolean", "boolean", "value", false, true);
		assertNotEquals(fieldRef1, fieldRef2);

		fieldRef1 = new FieldRef("java.lang.Boolean", "boolean", "value", false, true);
		fieldRef2 = new FieldRef("java.lang.Boolean", "int", "value", false, true);
		assertNotEquals(fieldRef1, fieldRef2);

		fieldRef1 = new FieldRef("java.lang.Boolean", "boolean", "value", false, true);
		fieldRef2 = new FieldRef("java.lang.Boolean", "boolean", "val", false, true);
		assertNotEquals(fieldRef1, fieldRef2);

		fieldRef1 = new FieldRef("java.lang.Boolean", "boolean", "value", false, true);
		fieldRef2 = new FieldRef("java.lang.Boolean", "boolean", "value", true, true);
		assertNotEquals(fieldRef1, fieldRef2);

		fieldRef1 = new FieldRef("java.lang.Boolean", "boolean", "value", false, true);
		fieldRef2 = new FieldRef("java.lang.Boolean", "boolean", "value", false, false);
		assertNotEquals(fieldRef1, fieldRef2);

	}

	@Test
	void test_hashCode() {

		FieldRef fieldRef1 = new FieldRef("java.lang.Boolean", "boolean", "value", false, true);
		FieldRef fieldRef2 = new FieldRef("java.lang.Boolean", "boolean", "value", false, true);
		assertEquals(fieldRef1.hashCode(), fieldRef2.hashCode());

		fieldRef1 = new FieldRef("java.lang.Boolean", "boolean", "value", false, true);
		fieldRef2 = new FieldRef("java.lang.AtomicBoolean", "boolean", "value", false, true);
		assertNotEquals(fieldRef1.hashCode(), fieldRef2.hashCode());

		fieldRef1 = new FieldRef("java.lang.Boolean", "boolean", "value", false, true);
		fieldRef2 = new FieldRef("java.lang.Boolean", "int", "value", false, true);
		assertNotEquals(fieldRef1.hashCode(), fieldRef2.hashCode());

		fieldRef1 = new FieldRef("java.lang.Boolean", "boolean", "value", false, true);
		fieldRef2 = new FieldRef("java.lang.Boolean", "boolean", "val", false, true);
		assertNotEquals(fieldRef1.hashCode(), fieldRef2.hashCode());

		fieldRef1 = new FieldRef("java.lang.Boolean", "boolean", "value", false, true);
		fieldRef2 = new FieldRef("java.lang.Boolean", "boolean", "value", true, true);
		assertNotEquals(fieldRef1.hashCode(), fieldRef2.hashCode());

		fieldRef1 = new FieldRef("java.lang.Boolean", "boolean", "value", false, true);
		fieldRef2 = new FieldRef("java.lang.Boolean", "boolean", "value", false, false);
		assertNotEquals(fieldRef1.hashCode(), fieldRef2.hashCode());

	}

	@Test
	void compareTo() {

		FieldRef fieldRef1 = new FieldRef("java.lang.Boolean", "boolean", "value", false, true);
		FieldRef fieldRef2 = new FieldRef("java.lang.Boolean", "boolean", "value", false, true);
		assertEquals(0, fieldRef1.compareTo(fieldRef2));
		assertEquals(0, fieldRef2.compareTo(fieldRef1));

		fieldRef1 = new FieldRef("java.lang.Boolean", "boolean", "value", false, true);
		fieldRef2 = new FieldRef("java.lang.AtomicBoolean", "boolean", "value", false, true);
		assertTrue(fieldRef1.compareTo(fieldRef2) > 0);
		assertTrue(fieldRef2.compareTo(fieldRef1) < 0);

		fieldRef1 = new FieldRef("java.lang.Boolean", "boolean", "value", false, true);
		fieldRef2 = new FieldRef("java.lang.Boolean", "int", "value", false, true);
		assertTrue(fieldRef1.compareTo(fieldRef2) < 0);
		assertTrue(fieldRef2.compareTo(fieldRef1) > 0);

		fieldRef1 = new FieldRef("java.lang.Boolean", "boolean", "value", false, true);
		fieldRef2 = new FieldRef("java.lang.Boolean", "boolean", "val", false, true);
		assertTrue(fieldRef1.compareTo(fieldRef2) > 0);
		assertTrue(fieldRef2.compareTo(fieldRef1) < 0);

		fieldRef1 = new FieldRef("java.lang.Boolean", "boolean", "value", false, true);
		fieldRef2 = new FieldRef("java.lang.Boolean", "boolean", "value", true, true);
		assertTrue(fieldRef1.compareTo(fieldRef2) < 0);
		assertTrue(fieldRef2.compareTo(fieldRef1) > 0);

		fieldRef1 = new FieldRef("java.lang.Boolean", "boolean", "value", false, true);
		fieldRef2 = new FieldRef("java.lang.Boolean", "boolean", "value", false, false);
		assertTrue(fieldRef1.compareTo(fieldRef2) > 0);
		assertTrue(fieldRef2.compareTo(fieldRef1) < 0);

	}

}