package org.jarcheck.model;

import org.jarcheck.test.ClasspathBuilder;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ClasspathTest {

	@Test
	void test_toString() {

		// prepare
		// prepare
		Classpath classpath = ClasspathBuilder.create()
				.addJarFile("a.jar").addClassDef("a/A")
				.addJarFile("b.jar").addClassDef("b/B")
				.build();
		// test
		String result = classpath.toString();

		// assert
		assertEquals("Classpath[2]", result);

	}

}
