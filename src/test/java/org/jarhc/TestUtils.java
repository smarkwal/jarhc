package org.jarhc;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

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

	public static String getResourceAsString(String resource, String encoding) throws IOException {
		if (resource == null) throw new IllegalArgumentException("resource");
		InputStream stream = getResourceAsStream(resource);
		ByteArrayOutputStream result = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		while (true) {
			int len = stream.read(buffer);
			if (len < 0) break;
			result.write(buffer, 0, len);
		}
		return result.toString(encoding);
	}

	public static List<String> getResourceAsLines(String resource, String encoding) throws IOException {
		List<String> lines = new ArrayList<>();
		try (InputStream stream = getResourceAsStream(resource)) {
			BufferedReader reader = new BufferedReader(new InputStreamReader(stream, encoding));
			while (true) {
				String line = reader.readLine();
				if (line == null) break;
				lines.add(line);
			}
		}
		return lines;
	}

	public static File getResourceAsFile(String resource, String prefix) throws IOException {
		if (resource == null) throw new IllegalArgumentException("resource");
		InputStream stream = getResourceAsStream(resource);
		File directory = createTempDirectory(prefix);
		String fileName = getFileName(resource);
		File file = new File(directory, fileName);
		Files.copy(stream, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
		return file;
	}

	private static String getFileName(String resource) {
		if (resource.contains("/")) {
			int post = resource.lastIndexOf('/');
			return resource.substring(post + 1);
		} else {
			return resource;
		}
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
