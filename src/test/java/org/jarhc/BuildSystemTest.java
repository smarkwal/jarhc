package org.jarhc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.Charset;
import java.util.Locale;
import org.junit.jupiter.api.Test;

/**
 * Tests for build system configuration and capabilities.
 */
class BuildSystemTest {

	@Test
	void defaultLocal() {
		Locale locale = Locale.getDefault();
		String code = locale.toString();
		// "en" and "en_US" are both OK
		assertTrue(code.equals("en") || code.equals("en_US"), code);
	}

	@Test
	void defaultCharset() {
		Charset charset = Charset.defaultCharset();
		assertEquals("UTF-8", charset.name());
	}

}
