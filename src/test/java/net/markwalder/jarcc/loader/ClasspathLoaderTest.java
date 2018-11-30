package net.markwalder.jarcc.loader;

import net.markwalder.jarcc.TestUtils;
import net.markwalder.jarcc.model.Classpath;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ClasspathLoaderTest {

	private final ClasspathLoader classpathLoader = new ClasspathLoader();

	@Test
	void test_load_file() throws IOException {

		// prepare
		String resource = "/test2/a.jar";
		File file = TestUtils.getResourceAsFile(resource, "ClasspathLoader-", ".jar");

		// test
		Classpath classpath = classpathLoader.load(file.getParentFile(), false);

		// assert
		assertNotNull(classpath);
		assertEquals(1, classpath.getJarFiles().size());
		assertEquals(file.getName(), classpath.getJarFiles().get(0).getFileName());
	}

}
