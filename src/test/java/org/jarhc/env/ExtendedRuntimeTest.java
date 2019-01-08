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

package org.jarhc.env;

import org.jarhc.model.ClassDef;
import org.jarhc.model.Classpath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ExtendedRuntimeTest {

	private ExtendedRuntime runtime;

	@BeforeEach
	void setUp() {
		Classpath classpath = Mockito.mock(Classpath.class);
		Mockito.when(classpath.getClassDef("u.Unknown")).thenReturn(Optional.empty());
		Mockito.when(classpath.getClassDef("c.Common")).thenReturn(Optional.of(ClassDef.forClassName("c.Common").setClassLoader("Provided")));

		JavaRuntime javaRuntime = Mockito.mock(JavaRuntime.class);
		Mockito.when(javaRuntime.getName()).thenReturn("[name]");
		Mockito.when(javaRuntime.getJavaVersion()).thenReturn("[version]");
		Mockito.when(javaRuntime.getJavaVendor()).thenReturn("[vendor]");
		Mockito.when(javaRuntime.getJavaHome()).thenReturn("[home]");
		Mockito.when(javaRuntime.getClassDef("u.Unknown")).thenReturn(Optional.empty());
		Mockito.when(javaRuntime.getClassDef("c.Common")).thenReturn(Optional.empty());
		Mockito.when(javaRuntime.getClassDef("java.lang.String")).thenReturn(Optional.of(ClassDef.forClassName("java.lang.String").setClassLoader("Runtime")));
		Mockito.when(javaRuntime.getClassLoaderName("u.Unknown")).thenReturn(Optional.empty());
		Mockito.when(javaRuntime.getClassLoaderName("c.Common")).thenReturn(Optional.empty());
		Mockito.when(javaRuntime.getClassLoaderName("java.lang.String")).thenReturn(Optional.of("Runtime"));

		runtime = new ExtendedRuntime(classpath, javaRuntime);
	}

	@Test
	void test_getName() {
		// test
		String result = runtime.getName();
		// assert
		assertEquals("[name]", result);
	}

	@Test
	void test_getJavaVersion() {
		// test
		String result = runtime.getJavaVersion();
		// assert
		assertEquals("[version]", result);
	}

	@Test
	void test_getJavaVendor() {
		// test
		String result = runtime.getJavaVendor();
		// assert
		assertEquals("[vendor]", result);
	}

	@Test
	void test_getJavaHome() {
		// test
		String result = runtime.getJavaHome();
		// assert
		assertEquals("[home]", result);
	}

	@Test
	void test_getClassLoaderName_Unknown() {
		// test
		Optional<String> result = runtime.getClassLoaderName("u.Unknown");
		// assert
		assertNotNull(result);
		assertFalse(result.isPresent());
	}

	@Test
	void test_getClassLoaderName_Common() {
		// test
		Optional<String> result = runtime.getClassLoaderName("c.Common");
		// assert
		assertNotNull(result);
		assertTrue(result.isPresent());
		assertEquals("Provided", result.get());
	}

	@Test
	void test_getClassLoaderName_String() {
		// test
		Optional<String> result = runtime.getClassLoaderName("java.lang.String");
		// assert
		assertNotNull(result);
		assertTrue(result.isPresent());
		assertEquals("Runtime", result.get());
	}

	@Test
	void test_getClassDef_Unknown() {
		// test
		Optional<ClassDef> result = runtime.getClassDef("u.Unknown");
		// assert
		assertNotNull(result);
		assertFalse(result.isPresent());
	}

	@Test
	void test_getClassDef_Common() {
		// test
		Optional<ClassDef> result = runtime.getClassDef("c.Common");
		// assert
		assertNotNull(result);
		assertTrue(result.isPresent());
		ClassDef classDef = result.get();
		assertEquals("c.Common", classDef.getClassName());
		assertEquals("Provided", classDef.getClassLoader());
	}

	@Test
	void test_getClassDef_String() {
		// test
		Optional<ClassDef> result = runtime.getClassDef("java.lang.String");
		// assert
		assertNotNull(result);
		assertTrue(result.isPresent());
		ClassDef classDef = result.get();
		assertEquals("java.lang.String", classDef.getClassName());
		assertEquals("Runtime", classDef.getClassLoader());
	}

}
