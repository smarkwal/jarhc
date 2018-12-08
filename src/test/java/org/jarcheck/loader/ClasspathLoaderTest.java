package org.jarcheck.loader;

import org.jarcheck.TestUtils;
import org.jarcheck.model.Classpath;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ClasspathLoaderTest {

	private final ClasspathLoader classpathLoader = new ClasspathLoader();

	@Test
	void test_load_file() throws IOException {

		// prepare
		String resource = "/test2/a.jar";
		File file = TestUtils.getResourceAsFile(resource, "ClasspathLoader-");
		List<File> files = Collections.singletonList(file);

		// test
		Classpath classpath = classpathLoader.load(files);

		// assert
		assertNotNull(classpath);
		assertEquals(1, classpath.getJarFiles().size());
		assertEquals(file.getName(), classpath.getJarFiles().get(0).getFileName());
	}

}
