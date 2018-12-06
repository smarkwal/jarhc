package org.jarcheck.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JavaUtilsTest {

	@Test
	void test_loadBootstrapClass() {

		// test
		Class javaClass = JavaUtils.loadBootstrapClass("java.lang.String");

		// assert
		assertEquals(String.class, javaClass);

		// test
		javaClass = JavaUtils.loadBootstrapClass("java/lang/String");

		// assert
		assertEquals(String.class, javaClass);

		// test
		javaClass = JavaUtils.loadBootstrapClass("a/b/C");

		// assert
		assertNull(javaClass);

	}

	@Test
	void test_isBootstrapClass() {

		// test
		boolean result = JavaUtils.isBootstrapClass("java.lang.String");

		// assert
		assertTrue(result);

		// test
		result = JavaUtils.isBootstrapClass("java/lang/String");

		// assert
		assertTrue(result);

		// test
		result = JavaUtils.isBootstrapClass("a/b/C");

		// assert
		assertFalse(result);

	}

}
