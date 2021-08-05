package org.jarhc.it;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import org.jarhc.artifacts.Artifact;
import org.jarhc.artifacts.MavenArtifactFinder;
import org.jarhc.artifacts.RepositoryException;
import org.junit.jupiter.api.Test;

class MavenArtifactFinderIT {

	private final MavenArtifactFinder artifactFinder = new MavenArtifactFinder();

	@Test
	void test_findArtifact_byChecksum_CommonsIO() throws RepositoryException {

		// test
		Optional<Artifact> artifact = artifactFinder.findArtifact("815893df5f31da2ece4040fe0a12fd44b577afaf");

		// assert
		assertTrue(artifact.isPresent());
		assertEquals("commons-io", artifact.get().getGroupId());
		assertEquals("commons-io", artifact.get().getArtifactId());
		assertEquals("2.6", artifact.get().getVersion());
		assertEquals("jar", artifact.get().getType());

	}

	@Test
	void test_findArtifact_byChecksum_CommonsCodec() throws RepositoryException {

		// test
		Optional<Artifact> artifact = artifactFinder.findArtifact("093ee1760aba62d6896d578bd7d247d0fa52f0e7");

		// assert
		assertTrue(artifact.isPresent());
		assertEquals("commons-codec", artifact.get().getGroupId());
		assertEquals("commons-codec", artifact.get().getArtifactId());
		assertEquals("1.11", artifact.get().getVersion());
		assertEquals("jar", artifact.get().getType());

	}

	@Test
	void test_findArtifact_byChecksum_ASM() throws RepositoryException {

		// test
		Optional<Artifact> artifact = artifactFinder.findArtifact("d74d4ba0dee443f68fb2dcb7fcdb945a2cd89912");

		// assert
		assertTrue(artifact.isPresent());
		assertEquals("org.ow2.asm", artifact.get().getGroupId());
		assertEquals("asm", artifact.get().getArtifactId());
		assertEquals("7.0", artifact.get().getVersion());
		assertEquals("jar", artifact.get().getType());

	}

	@Test
	void test_findArtifact_byChecksum_TestJettyWebApp() throws RepositoryException {

		// test
		Optional<Artifact> artifact = artifactFinder.findArtifact("9d920ed18833e7275ba688d88242af4c3711fbea");

		// assert
		assertTrue(artifact.isPresent());
		assertEquals("org.eclipse.jetty", artifact.get().getGroupId());
		assertEquals("test-jetty-webapp", artifact.get().getArtifactId());
		assertEquals("9.4.20.v20190813", artifact.get().getVersion());
		assertEquals("war", artifact.get().getType());

	}

	@Test
	void test_findArtifact_byChecksum_notFound() throws RepositoryException {

		// test
		Optional<Artifact> artifact = artifactFinder.findArtifact("1234567890123456789012345678901234567890");

		// assert
		assertFalse(artifact.isPresent());

	}

}