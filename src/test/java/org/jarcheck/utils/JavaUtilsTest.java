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

	@Test
	void test_getPackageName() {

		assertEquals("", JavaUtils.getPackageName("A"));
		assertEquals("a", JavaUtils.getPackageName("a/B"));
		assertEquals("a.b", JavaUtils.getPackageName("a/b/C"));
		assertEquals("a.b.c", JavaUtils.getPackageName("a/b/c/D"));
		assertEquals("java.lang", JavaUtils.getPackageName("java.lang.String"));
		assertEquals("java.io", JavaUtils.getPackageName("java.io.InputStream"));
		assertEquals("a.b.c.d", JavaUtils.getPackageName("a.b.c.d.E$F"));

	}

}
