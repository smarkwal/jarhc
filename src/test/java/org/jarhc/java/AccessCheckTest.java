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

package org.jarhc.java;

import org.jarhc.env.JavaRuntime;
import org.jarhc.model.ClassDef;
import org.jarhc.model.FieldDef;
import org.jarhc.test.JavaRuntimeMock;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class AccessCheckTest {

	private final JavaRuntime javaRuntime = JavaRuntimeMock.getOracleRuntime();
	private final AccessCheck accessCheck = new AccessCheck(javaRuntime);

	@Test
	void test_same_class() {

		// prepare
		ClassDef classDef = getClassDef("java.lang.String");

		// assume
		assumeTrue(classDef.isPublic());

		// test
		boolean result = accessCheck.hasAccess(classDef, classDef);

		// assert
		assertTrue(result);

	}

	@Test
	void test_public_class() {

		// prepare
		ClassDef classDef1 = getClassDef("java.io.StringWriter");
		ClassDef classDef2 = getClassDef("java.lang.String");

		// assume
		assumeTrue(classDef2.isPublic());

		// test
		boolean result = accessCheck.hasAccess(classDef1, classDef2);

		// assert
		assertTrue(result);

	}

	@Test
	void test_package_private_class() {

		// prepare
		ClassDef classDef1 = getClassDef("java.lang.String");
		ClassDef classDef2 = getClassDef("java.io.FileSystem");

		// assume
		assumeTrue(classDef2.isPackagePrivate());

		// test
		boolean result = accessCheck.hasAccess(classDef1, classDef2);

		// assert
		assertFalse(result);

	}

	@Test
	void test_package_private_class_same_package() {

		// prepare
		ClassDef classDef1 = getClassDef("java.io.StringWriter");
		ClassDef classDef2 = getClassDef("java.io.FileSystem");

		// assume
		assumeTrue(classDef2.isPackagePrivate());

		// test
		boolean result = accessCheck.hasAccess(classDef1, classDef2);

		// assert
		assertTrue(result);

	}

	// ---------------------------------------------------------------------------------------

	@Test
	void test_public_class_private_field_same_class() {

		// prepare
		ClassDef classDef = ClassDef.forClassName("java.lang.String");
		FieldDef fieldDef = getFieldDef("java.lang.String", "value");

		// assume
		assumeTrue(fieldDef.getClassDef().isPublic());
		assumeTrue(fieldDef.isPrivate());

		// test
		boolean result = accessCheck.hasAccess(classDef, fieldDef);

		// assert
		assertTrue(result);

	}

	@Test
	void test_public_class_private_field() {

		// prepare
		ClassDef classDef = ClassDef.forClassName("org.jarhc.Main");
		FieldDef fieldDef = getFieldDef("java.lang.String", "value");

		// assume
		assumeTrue(fieldDef.getClassDef().isPublic());
		assumeTrue(fieldDef.isPrivate());

		// test
		boolean result = accessCheck.hasAccess(classDef, fieldDef);

		// assert
		assertFalse(result);

	}

	@Test
	void test_public_class_public_field() {

		// prepare
		ClassDef classDef = ClassDef.forClassName("org.jarhc.Main");
		FieldDef fieldDef = getFieldDef("java.lang.Boolean", "TRUE");

		// assume
		assumeTrue(fieldDef.getClassDef().isPublic());
		assumeTrue(fieldDef.isPublic());

		// test
		boolean result = accessCheck.hasAccess(classDef, fieldDef);

		// assert
		assertTrue(result);

	}

	@Test
	void test_package_private_class_public_field() {

		// prepare
		ClassDef classDef = ClassDef.forClassName("org.jarhc.Main");
		FieldDef fieldDef = getFieldDef("java.io.FileSystem", "ACCESS_READ");

		// assume
		assumeTrue(fieldDef.getClassDef().isPackagePrivate());
		assumeTrue(fieldDef.isPublic());

		// test
		boolean result = accessCheck.hasAccess(classDef, fieldDef);

		// assert
		assertTrue(result);

	}

	@Test
	void test_package_private_class_public_field_same_package() {

		// prepare
		ClassDef classDef = ClassDef.forClassName("java.io.Win32FileSystem");
		FieldDef fieldDef = getFieldDef("java.io.FileSystem", "ACCESS_READ");

		// assume
		assumeTrue(fieldDef.getClassDef().isPackagePrivate());
		assumeTrue(fieldDef.isPublic());

		// test
		boolean result = accessCheck.hasAccess(classDef, fieldDef);

		// assert
		assertTrue(result);

	}

	@Test
	void test_public_class_protected_field() {

		// prepare
		ClassDef classDef = ClassDef.forClassName("org.jarhc.Main").setSuperName("java.lang.Object").addInterfaceName("java.io.Serializable").addInterfaceName("u.Unknown");
		FieldDef fieldDef = getFieldDef("java.io.FilterInputStream", "in");

		// assume
		assumeTrue(fieldDef.getClassDef().isPublic());
		assumeTrue(fieldDef.isProtected());

		// test
		boolean result = accessCheck.hasAccess(classDef, fieldDef);

		// assert
		assertFalse(result);

	}

	@Test
	void test_public_class_protected_field_same_package() {

		// prepare
		ClassDef classDef = ClassDef.forClassName("java.io.FilterOutputStream");
		FieldDef fieldDef = getFieldDef("java.io.FilterInputStream", "in");

		// assume
		assumeTrue(fieldDef.getClassDef().isPublic());
		assumeTrue(fieldDef.isProtected());

		// test
		boolean result = accessCheck.hasAccess(classDef, fieldDef);

		// assert
		assertTrue(result);

	}

	@Test
	void test_public_class_protected_field_subclass() {

		// prepare
		ClassDef classDef = ClassDef.forClassName("my.MyOutputStream").setSuperName("java.io.FilterInputStream");
		FieldDef fieldDef = getFieldDef("java.io.FilterInputStream", "in");

		// assume
		assumeTrue(fieldDef.getClassDef().isPublic());
		assumeTrue(fieldDef.isProtected());

		// test
		boolean result = accessCheck.hasAccess(classDef, fieldDef);

		// assert
		assertTrue(result);

	}

	@Test
	void test_public_class_package_private_field() {

		// prepare
		ClassDef classDef = ClassDef.forClassName("org.jarhc.Main");
		FieldDef fieldDef = getFieldDef("java.text.DateFormatSymbols", "PATTERN_YEAR");

		// assume
		assumeTrue(fieldDef.getClassDef().isPublic());
		assumeTrue(fieldDef.isPackagePrivate());

		// test
		boolean result = accessCheck.hasAccess(classDef, fieldDef);

		// assert
		assertFalse(result);

	}

	@Test
	void test_weird_case() { // TODO: rename

		// prepare
		ClassDef classDef = ClassDef.forClassName("com.sun.xml.internal.ws.addressing.v200408.MemberSubmissionWsaClientTube")
				.setSuperName("com.sun.xml.internal.ws.addressing.WsaClientTube");
		FieldDef fieldDef = getFieldDef("com.sun.xml.internal.ws.addressing.WsaTube", "addressingVersion");

		// assume
		assumeTrue(fieldDef.getClassDef().isPackagePrivate());
		assumeTrue(fieldDef.isProtected());

		// test
		boolean result = accessCheck.hasAccess(classDef, fieldDef);

		// assert
		assertTrue(result);

	}

	private ClassDef getClassDef(String className) {
		Optional<ClassDef> classDef = javaRuntime.getClassDef(className);
		return classDef.orElseThrow(() -> new IllegalArgumentException("Class not found: " + className));
	}

	private FieldDef getFieldDef(String className, String fieldName) {
		ClassDef classDef = getClassDef(className);
		Optional<FieldDef> fieldDef = classDef.getFieldDef(fieldName);
		return fieldDef.orElseThrow(() -> new IllegalArgumentException("Field not found: " + className + "." + fieldName));
	}

}
