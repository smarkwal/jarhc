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

package org.jarhc.env;

import org.jarhc.Main;
import org.jarhc.model.ClassDef;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class DefaultJavaRuntimeTest {

	@Test
	void test_getName() {

		// prepare
		JavaRuntime javaRuntime = new DefaultJavaRuntime();

		// test
		String result = javaRuntime.getName();

		// assert
		assertEquals(System.getProperty("java.runtime.name"), result);

	}

	@Test
	void test_getJavaVersion() {

		// prepare
		JavaRuntime javaRuntime = new DefaultJavaRuntime();

		// test
		String result = javaRuntime.getJavaVersion();

		// assert
		assertEquals(System.getProperty("java.version"), result);

	}

	@Test
	void test_getJavaVendor() {

		// prepare
		JavaRuntime javaRuntime = new DefaultJavaRuntime();

		// test
		String result = javaRuntime.getJavaVendor();

		// assert
		assertEquals(System.getProperty("java.vendor"), result);

	}

	@Test
	void test_getJavaHome() {

		// prepare
		JavaRuntime javaRuntime = new DefaultJavaRuntime();

		// test
		String result = javaRuntime.getJavaHome();

		// assert
		assertEquals(System.getProperty("java.home"), result);

	}

	@Test
	void test_getClassLoaderName_String() {

		// prepare
		JavaRuntime javaRuntime = new DefaultJavaRuntime();

		// test
		Optional<String> result = javaRuntime.getClassLoaderName("java.lang.String");

		// assert
		assertTrue(result.isPresent());
		assertEquals("Runtime", result.get());

	}

	@Test
	void test_getClassLoaderName_Unknown() {

		// prepare
		JavaRuntime javaRuntime = new DefaultJavaRuntime();

		// test
		Optional<String> result = javaRuntime.getClassLoaderName("u.Unknown");

		// assert
		assertFalse(result.isPresent());

	}

	@Test
	void test_getClassLoaderName_Main() {

		// prepare
		JavaRuntime javaRuntime = new DefaultJavaRuntime();

		// test
		Optional<String> result = javaRuntime.getClassLoaderName(Main.class.getName());

		// assert
		assertFalse(result.isPresent());

	}

	@Test
	void test_getClassDef_String() {

		// prepare
		JavaRuntime javaRuntime = new DefaultJavaRuntime();

		// test
		Optional<ClassDef> result = javaRuntime.getClassDef("java.lang.String");

		// assert
		assertTrue(result.isPresent());
		assertEquals("java.lang.String", result.get().getClassName());

	}

	@Test
	void test_getClassDef_Unknown() {

		// prepare
		JavaRuntime javaRuntime = new DefaultJavaRuntime();

		// test
		Optional<ClassDef> result = javaRuntime.getClassDef("u.Unknown");

		// assert
		assertFalse(result.isPresent());

	}

	@Test
	void test_getClassDef_Main() {

		// prepare
		JavaRuntime javaRuntime = new DefaultJavaRuntime();

		// test
		Optional<ClassDef> result = javaRuntime.getClassDef(Main.class.getName());

		// assert
		assertFalse(result.isPresent());

	}

	@Test
	void test_getClassDef_Integer_cached() {

		// prepare
		JavaRuntime javaRuntime = new DefaultJavaRuntime();

		// test
		Optional<ClassDef> result = javaRuntime.getClassDef("java.lang.Integer");

		// assert
		assertTrue(result.isPresent());
		assertEquals("java.lang.Integer", result.get().getClassName());

		// test 2: fetch again
		Optional<ClassDef> result2 = javaRuntime.getClassDef("java.lang.Integer");

		// assert
		assertTrue(result2.isPresent());
		assertEquals("java.lang.Integer", result2.get().getClassName());
		assertSame(result2.get(), result.get());

	}

}
