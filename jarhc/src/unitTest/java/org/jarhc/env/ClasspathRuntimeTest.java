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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.jarhc.model.ClassDef;
import org.jarhc.model.Classpath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class ClasspathRuntimeTest {

	private ClasspathJavaRuntime runtime;

	@BeforeEach
	void setUp() {
		Classpath classpath = Mockito.mock(Classpath.class);
		Mockito.when(classpath.getClassDef("u.Unknown")).thenReturn(null);
		Mockito.when(classpath.getClassDef("java.lang.String")).thenReturn(ClassDef.forClassName("java.lang.String").setClassLoader("Runtime"));
		runtime = new ClasspathJavaRuntime(classpath);
	}

	@Test
	void test_getName() {
		// test
		String result = runtime.getName();
		// assert
		assertEquals("[unknown]", result);
	}

	@Test
	void test_getJavaVersion() {
		// test
		String result = runtime.getJavaVersion();
		// assert
		assertEquals("[unknown]", result);
	}

	@Test
	void test_getJavaVendor() {
		// test
		String result = runtime.getJavaVendor();
		// assert
		assertEquals("[unknown]", result);
	}

	@Test
	void test_getJavaHome() {
		// test
		String result = runtime.getJavaHome();
		// assert
		assertEquals("[none]", result);
	}

	@Test
	void test_getClassDef_Unknown() {
		// test
		ClassDef result = runtime.getClassDef("u.Unknown");
		// assert
		assertNull(result);
	}

	@Test
	void test_getClassDef_String() {
		// test
		ClassDef result = runtime.getClassDef("java.lang.String");
		// assert
		assertNotNull(result);
		assertEquals("java.lang.String", result.getClassName());
		assertEquals("Runtime", result.getClassLoader());
	}

}
