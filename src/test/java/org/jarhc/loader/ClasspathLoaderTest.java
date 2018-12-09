package org.jarhc.loader;

import org.jarhc.TestUtils;
import org.jarhc.model.Classpath;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junitpioneer.jupiter.TempDirectory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junitpioneer.jupiter.TempDirectory.TempDir;

@ExtendWith(TempDirectory.class)
class ClasspathLoaderTest {

	private final ClasspathLoader classpathLoader = new ClasspathLoader();

	@Test
	void test_load_file(@TempDir Path tempDir) throws IOException {

		// prepare
		String resource = "/test2/a.jar";
		File file = TestUtils.getResourceAsFile(resource, tempDir);
		List<File> files = Collections.singletonList(file);

		// test
		Classpath classpath = classpathLoader.load(files);

		// assert
		assertNotNull(classpath);
		assertEquals(1, classpath.getJarFiles().size());
		assertEquals(file.getName(), classpath.getJarFiles().get(0).getFileName());
	}

}
