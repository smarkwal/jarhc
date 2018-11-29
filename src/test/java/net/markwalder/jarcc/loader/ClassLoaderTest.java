package net.markwalder.jarcc.loader;

import net.markwalder.jarcc.model.ClassDef;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ClassLoaderTest {

	@Test
	void test_load_java8() throws IOException {

		String resource = "/test/java8/Main.class";
		ClassDef classDef = loadClass(resource);

		assertNotNull(classDef);
		assertEquals("Main", classDef.getName());
		assertEquals(52, classDef.getVersion());
		assertEquals("Java 8", classDef.getJavaVersion());
	}

	@Test
	void test_load_java7() throws IOException {

		String resource = "/test/java7/Main.class";
		ClassDef classDef = loadClass(resource);

		assertNotNull(classDef);
		assertEquals("Main", classDef.getName());
		assertEquals(51, classDef.getVersion());
		assertEquals("Java 7", classDef.getJavaVersion());
	}

	@Test
	void test_load_java6() throws IOException {

		String resource = "/test/java6/Main.class";
		ClassDef classDef = loadClass(resource);

		assertNotNull(classDef);
		assertEquals("Main", classDef.getName());
		assertEquals(50, classDef.getVersion());
		assertEquals("Java 6", classDef.getJavaVersion());
	}

	private static ClassDef loadClass(String resource) throws IOException {
		try (InputStream stream = ClassLoaderTest.class.getResourceAsStream(resource)) {
			ClassLoader classLoader = new ClassLoader();
			return classLoader.load(stream);
		}
	}

}
