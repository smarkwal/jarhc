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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ArtifactTest {

	@Test
	void validateCoordinates() {

		assertTrue(Artifact.validateCoordinates("org.jarhc:jarhc:1.3"));
		assertTrue(Artifact.validateCoordinates("org.jarhc:jarhc:1.3:jar"));
		assertTrue(Artifact.validateCoordinates("org.jarhc:jarhc:1.4-SNAPSHOT"));
		assertTrue(Artifact.validateCoordinates("org.jarhc:jarhc:1.4-SNAPSHOT:jar"));
		assertTrue(Artifact.validateCoordinates("org.slf4j:slf4j-api:1.7.28"));
		assertTrue(Artifact.validateCoordinates("org.slf4j:slf4j-api:1.7.28:jar"));

		assertFalse(Artifact.validateCoordinates("report.txt"));
		assertFalse(Artifact.validateCoordinates("target/jarhc/report.html"));
		assertFalse(Artifact.validateCoordinates("C:\\jarhc\\report.html"));
		assertFalse(Artifact.validateCoordinates("org.slf4j:slf4j-api:1.7.28:jar:all-deps"));

	}

}