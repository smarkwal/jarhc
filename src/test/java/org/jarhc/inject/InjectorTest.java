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

package org.jarhc.inject;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.jarhc.utils.ExceptionUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

class InjectorTest {

	private Injector injector;

	@BeforeEach
	void setUp() {
		injector = new Injector();
	}

	@Test
	void test_createInstance_no_constructor() throws InjectorException {
		TestObject1 instance = injector.createInstance(TestObject1.class);
		assertNotNull(instance);
	}

	@SuppressWarnings("WeakerAccess")
	public static class TestObject1 {
		// no constructor
	}

	@Test
	void test_createInstance_noarg_constructor() throws InjectorException {
		TestObject2 instance = injector.createInstance(TestObject2.class);
		assertNotNull(instance);
	}

	public static class TestObject2 {
		// no-arg constructor
		public TestObject2() {
		}
	}

	@Test
	void test_createInstance_private_constructor() {
		try {
			injector.createInstance(TestObject3.class);
			fail("expected exception not thrown");
		} catch (InjectorException e) {
			assertEquals("No supported constructor found in class: org.jarhc.inject.InjectorTest$TestObject3", e.getMessage());
			assertNull(e.getCause());
		}
	}

	@SuppressWarnings("WeakerAccess")
	public static class TestObject3 {
		// private no-arg constructor
		private TestObject3() {
		}
	}

	@Test
	void test_createInstance_broken_constructor() {
		try {
			injector.createInstance(TestObject4.class);
			fail("expected exception not thrown");
		} catch (InjectorException e) {
			assertEquals("Error creating instance of class: org.jarhc.inject.InjectorTest$TestObject4", e.getMessage());
			Throwable cause = ExceptionUtils.getRootCause(e);
			assertNotNull(cause);
			assertTrue(cause instanceof NullPointerException);
			assertEquals("broken", cause.getMessage());
		}
	}

	public static class TestObject4 {
		// broken constructor
		public TestObject4() {
			throw new NullPointerException("broken");
		}
	}

	@Test
	void test_createInstance_multiple_constructors() {
		injector.addBinding(String.class, "Hello");
		injector.addBinding(Integer.class, 42);
		try {
			injector.createInstance(TestObject5.class);
			fail("expected exception not thrown");
		} catch (InjectorException e) {
			assertEquals("Multiple supported constructor found in class: org.jarhc.inject.InjectorTest$TestObject5", e.getMessage());
			assertNull(e.getCause());
		}
	}

	@SuppressWarnings("unused")
	public static class TestObject5 {
		// constructor 1
		public TestObject5(String text) {
		}

		// constructor 2
		public TestObject5(Integer number) {
		}
	}

	@Test
	void test_createInstance_single_constructor() throws InjectorException {
		injector.addBinding(String.class, "Hello");
		injector.addBinding(Integer.class, 42);
		TestObject6 instance = injector.createInstance(TestObject6.class);
		assertNotNull(instance);
	}

	public static class TestObject6 {
		// valid constructor
		public TestObject6(String text, Integer number, Logger logger) {
			assertEquals("Hello", text);
			assertEquals(Integer.valueOf(42), number);
			assertNotNull(logger);
			assertEquals(TestObject6.class.getName(), logger.getName());
		}
	}

}
