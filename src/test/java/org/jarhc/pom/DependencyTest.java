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

package org.jarhc.pom;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class DependencyTest {

	@Test
	void test_JUnit() {

		Dependency dependency = new Dependency("junit", "junit", "4.12", Scope.TEST, false);

		assertEquals("junit", dependency.getGroupId());
		assertEquals("junit", dependency.getArtifactId());
		assertEquals("4.12", dependency.getVersion());
		assertEquals(Scope.TEST, dependency.getScope());
		assertFalse(dependency.isOptional());

		assertEquals("junit:junit:4.12 (test)", dependency.toString());

	}

	@Test
	void test_ASM() {

		Dependency dependency = new Dependency("org.ow2.asm", "asm", "7.1", Scope.COMPILE, true);

		assertEquals("org.ow2.asm", dependency.getGroupId());
		assertEquals("asm", dependency.getArtifactId());
		assertEquals("7.1", dependency.getVersion());
		assertEquals(Scope.COMPILE, dependency.getScope());
		assertTrue(dependency.isOptional());

		assertEquals("org.ow2.asm:asm:7.1 (optional)", dependency.toString());

	}

	@Test
	void test_ServletAPI() {

		// javax.servlet:javax.servlet-api:jar:3.1.0

		Dependency dependency = new Dependency("javax.servlet", "javax.servlet-api", "3.1.0", Scope.PROVIDED, true);

		assertEquals("javax.servlet", dependency.getGroupId());
		assertEquals("javax.servlet-api", dependency.getArtifactId());
		assertEquals("3.1.0", dependency.getVersion());
		assertEquals(Scope.PROVIDED, dependency.getScope());
		assertTrue(dependency.isOptional());

		assertEquals("javax.servlet:javax.servlet-api:3.1.0 (provided, optional)", dependency.toString());

	}

}