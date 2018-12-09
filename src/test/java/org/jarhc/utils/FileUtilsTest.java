package org.jarhc.utils;

import org.junit.jupiter.api.Test;

import static org.jarhc.utils.FileUtils.formatFileSize;
import static org.junit.jupiter.api.Assertions.assertEquals;

class FileUtilsTest {

	@Test
	void test_formatFileSize() {

		assertEquals("0 B", formatFileSize(0));
		assertEquals("1 B", formatFileSize(1));
		assertEquals("21 B", formatFileSize(21));
		assertEquals("321 B", formatFileSize(321));
		assertEquals("1023 B", formatFileSize(1023));
		assertEquals("1.00 KB", formatFileSize(1024));
		assertEquals("1.21 KB", formatFileSize(1234));
		assertEquals("12.1 KB", formatFileSize(12345));
		assertEquals("121 KB", formatFileSize(123456));
		assertEquals("1.18 MB", formatFileSize(1234567));
		assertEquals("11.8 MB", formatFileSize(12345678));
		assertEquals("118 MB", formatFileSize(123456789));
		assertEquals("1177 MB", formatFileSize(1234567890));

	}

}
