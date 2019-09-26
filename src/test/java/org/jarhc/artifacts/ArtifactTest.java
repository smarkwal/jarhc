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
	void test_toString() {

		// prepare
		Artifact artifact = new Artifact("org.jarhc", "jarhc", "1.4-SNAPSHOT", "jar");

		// test
		String result = artifact.toString();

		// asert
		assertEquals("org.jarhc:jarhc:1.4-SNAPSHOT:jar", result);

	}

	@Test
	void getPath() {

		// prepare
		Artifact artifact = new Artifact("org.jarhc", "jarhc", "1.4-SNAPSHOT", "jar");

		// test
		String result = artifact.getPath();

		// asert
		assertEquals("org/jarhc/jarhc/1.4-SNAPSHOT/jarhc-1.4-SNAPSHOT.jar", result);

	}

	@Test
	void getFileName() {

		// prepare
		Artifact artifact = new Artifact("org.jarhc", "jarhc", "1.4-SNAPSHOT", "jar");

		// test
		String result = artifact.getFileName();

		// asert
		assertEquals("jarhc-1.4-SNAPSHOT.jar", result);

	}

}