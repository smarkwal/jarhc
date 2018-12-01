package org.jarcheck;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

/**
 * Test utility methods.
 */
public class TestUtils {

	public static InputStream getResourceAsStream(String resource) throws IOException {
		if (resource == null) throw new IllegalArgumentException("resource");
		InputStream stream = TestUtils.class.getResourceAsStream(resource);
		if (stream == null) throw new IOException("Resource not found: " + resource);
		return stream;
	}

	public static File getResourceAsFile(String resource, String prefix, String suffix) throws IOException {
		if (resource == null) throw new IllegalArgumentException("resource");
		InputStream stream = getResourceAsStream(resource);
		File directory = createTempDirectory(prefix);
		File file = File.createTempFile(prefix, suffix, directory);
		Files.copy(stream, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
		return file;
	}

	private static File createTempDirectory(String prefix) throws IOException {

		// create a file with a unique name
		File file = File.createTempFile(prefix, ".dir");

		// delete the file and create a directory with the same name
		boolean deleted = file.delete();
		if (!deleted) throw new IOException("Unable to delete file: " + file.getAbsolutePath());
		boolean created = file.mkdirs();
		if (!created) throw new IOException("Unable to create directory: " + file.getAbsolutePath());

		return file;
	}

}
