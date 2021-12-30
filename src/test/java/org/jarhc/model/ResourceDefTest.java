package org.jarhc.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class ResourceDefTest {

	@Test
	void test() {

		// prepare
		JarFile jarFile = Mockito.mock(JarFile.class);

		// test
		ResourceDef def = new ResourceDef("path/to/file.txt", "checksum");
		def.setRelease(11);
		def.setJarFile(jarFile);

		// assert
		assertEquals("path/to/file.txt", def.getPath());
		assertEquals("checksum", def.getChecksum());
		assertEquals(11, def.getRelease());
		assertSame(jarFile, def.getJarFile());

	}

}