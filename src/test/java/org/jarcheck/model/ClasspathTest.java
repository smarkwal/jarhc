package org.jarcheck.model;

import org.jarcheck.test.ClasspathBuilder;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ClasspathTest {

	@Test
	void test_toString() {

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

	@Test
	void test_getJarFiles() {

		// prepare
		Classpath classpath = ClasspathBuilder.create()
				.addJarFile("a.jar").addClassDef("a/A")
				.addJarFile("b.jar").addClassDef("b/B")
				.build();

		// test
		List<JarFile> result = classpath.getJarFiles();

		// assert
		assertNotNull(result);
		assertEquals(2, result.size());
		assertEquals("a.jar", result.get(0).getFileName());
		assertEquals("b.jar", result.get(1).getFileName());

	}

	@Test
	void test_getJarFile() {

		// prepare
		Classpath classpath = ClasspathBuilder.create()
				.addJarFile("a.jar").addClassDef("a/A")
				.addJarFile("b.jar").addClassDef("b/B")
				.build();

		// test
		JarFile result = classpath.getJarFile("b.jar");

		// assert
		assertNotNull(result);
		assertEquals("b.jar", result.getFileName());

		// test
		result = classpath.getJarFile("c.jar");

		// assert
		assertNull(result);

	}

	@Test
	void test_getClassDefs() {

		// prepare
		Classpath classpath = ClasspathBuilder.create()
				.addJarFile("a.jar").addClassDef("a/A")
				.addJarFile("b.jar").addClassDef("b/B")
				.build();

		// test
		Set<ClassDef> result = classpath.getClassDefs("a/A");

		// assert
		assertNotNull(result);
		assertEquals(1, result.size());

	}

}
