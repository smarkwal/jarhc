package net.markwalder.jarcc;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class TestUtils {

	public static InputStream getResourceAsStream(String resource) throws IOException {
		InputStream stream = TestUtils.class.getResourceAsStream(resource);
		if (stream == null) throw new IOException("Resource not found: " + resource);
		return stream;
	}

	public static File getResourceAsFile(String resource, String prefix, String suffix) throws IOException {
		InputStream stream = getResourceAsStream(resource);
		File file = File.createTempFile(prefix, suffix);
		Files.copy(stream, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
		return file;
	}

}
