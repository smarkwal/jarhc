package org.jarhc;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
		assertEquals("en_US", locale.toString());
	}

	@Test
	void defaultCharset() {
		Charset charset = Charset.defaultCharset();
		assertEquals("UTF-8", charset.name());
	}

}
