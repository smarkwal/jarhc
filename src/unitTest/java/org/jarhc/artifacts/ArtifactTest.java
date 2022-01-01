/*
 * Copyright 2019 Stephan Markwalder
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jarhc.artifacts;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ArtifactTest {

	@Test
	void validateCoordinates() {

		// Maven coordinates
		assertTrue(Artifact.validateCoordinates("org.jarhc:jarhc:1.3"));
		assertTrue(Artifact.validateCoordinates("org.jarhc:jarhc:1.3:jar"));
		assertTrue(Artifact.validateCoordinates("org.jarhc:jarhc:1.4-SNAPSHOT"));
		assertTrue(Artifact.validateCoordinates("org.jarhc:jarhc:1.4-SNAPSHOT:jar"));
		assertTrue(Artifact.validateCoordinates("org.slf4j:slf4j-api:1.7.28"));
		assertTrue(Artifact.validateCoordinates("org.slf4j:slf4j-api:1.7.28:jar"));
		assertTrue(Artifact.validateCoordinates("org.eclipse.jetty:test-jetty-webapp:9.4.20.v20190813:war"));

		// Buildr coordinates
		assertTrue(Artifact.validateCoordinates("org.jarhc:jarhc:jar:1.3"));
		assertTrue(Artifact.validateCoordinates("org.jarhc:jarhc:jar:1.4-SNAPSHOT"));
		assertTrue(Artifact.validateCoordinates("org.slf4j:slf4j-api:jar:1.7.28"));
		assertTrue(Artifact.validateCoordinates("org.eclipse.jetty:test-jetty-webapp:war:9.4.20.v20190813"));

		// invalid coordinates
		assertFalse(Artifact.validateCoordinates("report.txt"));
		assertFalse(Artifact.validateCoordinates("target/jarhc/report.html"));
		assertFalse(Artifact.validateCoordinates("C:\\jarhc\\report.html"));
		assertFalse(Artifact.validateCoordinates("org.slf4j:slf4j-api:1.7.28:jar:all-deps"));

	}

	@Test
	void test_constructor_withMavenCoordinates() {

		Artifact artifact = new Artifact("org.jarhc:jarhc:1.3");
		assertEquals("org.jarhc", artifact.getGroupId());
		assertEquals("jarhc", artifact.getArtifactId());
		assertEquals("1.3", artifact.getVersion());
		assertEquals("jar", artifact.getType());

		artifact = new Artifact("org.jarhc:jarhc:1.4-SNAPSHOT:jar");
		assertEquals("org.jarhc", artifact.getGroupId());
		assertEquals("jarhc", artifact.getArtifactId());
		assertEquals("1.4-SNAPSHOT", artifact.getVersion());
		assertEquals("jar", artifact.getType());

		artifact = new Artifact("org.eclipse.jetty:test-jetty-webapp:9.4.20.v20190813:war");
		assertEquals("org.eclipse.jetty", artifact.getGroupId());
		assertEquals("test-jetty-webapp", artifact.getArtifactId());
		assertEquals("9.4.20.v20190813", artifact.getVersion());
		assertEquals("war", artifact.getType());

	}

	@Test
	void test_constructor_withBuildrCoordinates() {

		Artifact artifact = new Artifact("org.jarhc:jarhc:jar:1.4-SNAPSHOT");
		assertEquals("org.jarhc", artifact.getGroupId());
		assertEquals("jarhc", artifact.getArtifactId());
		assertEquals("1.4-SNAPSHOT", artifact.getVersion());
		assertEquals("jar", artifact.getType());

		artifact = new Artifact("org.eclipse.jetty:test-jetty-webapp:war:9.4.20.v20190813");
		assertEquals("org.eclipse.jetty", artifact.getGroupId());
		assertEquals("test-jetty-webapp", artifact.getArtifactId());
		assertEquals("9.4.20.v20190813", artifact.getVersion());
		assertEquals("war", artifact.getType());

	}

	@Test
	void test_constructor_throwsIllegalArgumentException_ifArgumentIsNull() {

		assertThrows(IllegalArgumentException.class, () -> new Artifact(null, "jarhc", "1.3", "jar"));
		assertThrows(IllegalArgumentException.class, () -> new Artifact("org.jarhc", null, "1.3", "jar"));
		assertThrows(IllegalArgumentException.class, () -> new Artifact("org.jarhc", "jarhc", null, "jar"));
		assertThrows(IllegalArgumentException.class, () -> new Artifact("org.jarhc", "jarhc", "1.3", null));

	}

	@Test
	void test_toString() {

		// prepare
		Artifact artifact = new Artifact("org.jarhc", "jarhc", "1.4-SNAPSHOT", "jar");

		// test
		String result = artifact.toString();

		// assert
		assertEquals("org.jarhc:jarhc:1.4-SNAPSHOT:jar", result);

	}

	@Test
	void toCoordinates() {

		// prepare
		Artifact artifact = new Artifact("org.jarhc", "jarhc", "1.4-SNAPSHOT", "jar");

		// test
		String result = artifact.toCoordinates();

		// assert
		assertEquals("org.jarhc:jarhc:1.4-SNAPSHOT", result);

	}

	@Test
	void getPath() {

		// prepare
		Artifact artifact = new Artifact("org.jarhc", "jarhc", "1.4-SNAPSHOT", "jar");

		// test
		String result = artifact.getPath();

		// assert
		assertEquals("org/jarhc/jarhc/1.4-SNAPSHOT/jarhc-1.4-SNAPSHOT.jar", result);

	}

	@Test
	void getFileName() {

		// prepare
		Artifact artifact = new Artifact("org.jarhc", "jarhc", "1.4-SNAPSHOT", "jar");

		// test
		String result = artifact.getFileName();

		// assert
		assertEquals("jarhc-1.4-SNAPSHOT.jar", result);

	}

	@Test
	void getFileName_forOSGIBundle() {

		// prepare
		Artifact artifact = new Artifact("org.test", "core-bundle", "2.1.3", "bundle");

		// test
		String result = artifact.getFileName();

		// assert
		assertEquals("core-bundle-2.1.3.jar", result);

	}

	@Test
	void testEquals() {

		// prepare
		Artifact artifact1 = new Artifact("org.jarhc", "jarhc", "1.3", "jar");

		// test
		Artifact artifact2 = new Artifact("org.jarhc", "jarhc", "1.3", "jar");
		assertEquals(artifact1, artifact2);
		assertEquals(artifact2, artifact1);

		artifact2 = new Artifact("org.jarcheck", "jarhc", "1.3", "jar");
		assertNotEquals(artifact1, artifact2);
		assertNotEquals(artifact2, artifact1);

		artifact2 = new Artifact("org.jarhc", "jarcheck", "1.3", "jar");
		assertNotEquals(artifact1, artifact2);
		assertNotEquals(artifact2, artifact1);

		artifact2 = new Artifact("org.jarhc", "jarhc", "1.2", "jar");
		assertNotEquals(artifact1, artifact2);
		assertNotEquals(artifact2, artifact1);

		artifact2 = new Artifact("org.jarhc", "jarhc", "1.3", "pom");
		assertNotEquals(artifact1, artifact2);
		assertNotEquals(artifact2, artifact1);

	}

	@Test
	void testHashCode() {

		// prepare
		Artifact artifact1 = new Artifact("org.jarhc", "jarhc", "1.3", "jar");

		// test
		Artifact artifact2 = new Artifact("org.jarhc", "jarhc", "1.3", "jar");
		assertEquals(artifact1.hashCode(), artifact2.hashCode());

		artifact2 = new Artifact("org.jarcheck", "jarhc", "1.3", "jar");
		assertNotEquals(artifact1.hashCode(), artifact2.hashCode());

		artifact2 = new Artifact("org.jarhc", "jarcheck", "1.3", "jar");
		assertNotEquals(artifact1.hashCode(), artifact2.hashCode());

		artifact2 = new Artifact("org.jarhc", "jarhc", "1.2", "jar");
		assertNotEquals(artifact1.hashCode(), artifact2.hashCode());

		artifact2 = new Artifact("org.jarhc", "jarhc", "1.3", "pom");
		assertNotEquals(artifact1.hashCode(), artifact2.hashCode());

	}

	@Test
	void withType() {

		// prepare
		Artifact artifact1 = new Artifact("org.jarhc", "jarhc", "1.3", "jar");

		// test
		Artifact artifact2 = artifact1.withType("pom");

		// assert
		assertSame(artifact1.getGroupId(), artifact2.getGroupId());
		assertSame(artifact1.getArtifactId(), artifact2.getArtifactId());
		assertSame(artifact1.getVersion(), artifact2.getVersion());
		assertEquals("jar", artifact1.getType());
		assertEquals("pom", artifact2.getType());

	}

}