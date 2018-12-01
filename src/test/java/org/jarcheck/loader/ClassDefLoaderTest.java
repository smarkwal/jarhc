package org.jarcheck.loader;

import org.jarcheck.TestUtils;
import org.jarcheck.model.ClassDef;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ClassDefLoaderTest {

	private final ClassDefLoader classDefLoader = new ClassDefLoader();

	@Test
	void test_load_java8() throws IOException {

		String resource = "/test/java8/Main.class";
		ClassDef classDef = loadClass(resource);

		assertNotNull(classDef);
		assertEquals("Main", classDef.getClassName());
		assertEquals(52, classDef.getClassVersion());
		assertEquals("Java 8", classDef.getJavaVersion());
	}

	@Test
	void test_load_java7() throws IOException {

		String resource = "/test/java7/Main.class";
		ClassDef classDef = loadClass(resource);

		assertNotNull(classDef);
		assertEquals("Main", classDef.getClassName());
		assertEquals(51, classDef.getClassVersion());
		assertEquals("Java 7", classDef.getJavaVersion());
	}

	@Test
	void test_load_java6() throws IOException {

		String resource = "/test/java6/Main.class";
		ClassDef classDef = loadClass(resource);

		assertNotNull(classDef);
		assertEquals("Main", classDef.getClassName());
		assertEquals(50, classDef.getClassVersion());
		assertEquals("Java 6", classDef.getJavaVersion());
	}

	@Test
	void test_load_file() throws IOException {

		String resource = "/test/java8/Main.class";
		File file = TestUtils.getResourceAsFile(resource, "ClassDefLoaderTest-", ".class");
		ClassDef classDef = classDefLoader.load(file);

		assertNotNull(classDef);
		assertEquals("Main", classDef.getClassName());
		assertEquals(52, classDef.getClassVersion());
		assertEquals("Java 8", classDef.getJavaVersion());
	}

	private ClassDef loadClass(String resource) throws IOException {
		try (InputStream stream = TestUtils.getResourceAsStream(resource)) {
			return classDefLoader.load(stream);
		}
	}

}
