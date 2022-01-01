package org.jarhc.test.release;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class BuildArtifactsTest extends ReleaseTest {

	@Test
	void version() {
		assertEquals("1.6-SNAPSHOT", getJarHcVersion());
	}

}
