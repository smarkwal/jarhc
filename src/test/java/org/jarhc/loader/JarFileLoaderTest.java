package org.jarhc.loader;

import org.jarhc.TestUtils;
import org.jarhc.model.JarFile;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junitpioneer.jupiter.TempDirectory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junitpioneer.jupiter.TempDirectory.TempDir;

@ExtendWith(TempDirectory.class)
class JarFileLoaderTest {

	@Test
	void test_load(@TempDir Path tempDir) throws IOException {

		// prepare
		String resource = "/test2/a.jar";
		File file = TestUtils.getResourceAsFile(resource, tempDir);

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
