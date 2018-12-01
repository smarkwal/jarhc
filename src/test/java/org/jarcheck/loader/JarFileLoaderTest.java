package org.jarcheck.loader;

import org.jarcheck.TestUtils;
import org.jarcheck.model.JarFile;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class JarFileLoaderTest {

	@Test
	void test_load() throws IOException {

		// prepare
		String resource = "/test2/a.jar";
		File file = TestUtils.getResourceAsFile(resource, "JarFileLoaderTest-", ".jar");

		// test
		JarFileLoader jarFileLoader = new JarFileLoader();
		JarFile jarFile = jarFileLoader.load(file);

		// assert
		assertNotNull(jarFile);
		assertEquals(file.getName(), jarFile.getFileName());
		assertEquals(1, jarFile.getClassDefs().size());
		assertEquals("a/A", jarFile.getClassDefs().get(0).getClassName());

	}

}
