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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class POMReaderTest {

	private final POMReader reader = new POMReader();

	@Test
	void read_ASM() throws POMException {

		// prepare
		InputStream inputStream = this.getClass().getResourceAsStream("asm-7.1.pom");

		// test
		POM pom = reader.read(inputStream);

		// assert
		assertNotNull(pom);
		assertEquals("org.ow2.asm", pom.getGroupId());
		assertEquals("asm", pom.getArtifactId());
		assertEquals("7.1", pom.getVersion());
		assertNotNull(pom.getParent());
		assertEquals("org.ow2", pom.getParent().getGroupId());
		assertEquals("ow2", pom.getParent().getArtifactId());
		assertEquals("1.5", pom.getParent().getVersion());
		assertEquals("asm", pom.getName());
		assertEquals("ASM, a very small and fast Java bytecode manipulation framework", pom.getDescription());

		assertEquals(new ArrayList<>(), pom.getPropertyNames());

		List<Dependency> dependencies = pom.getDependencies();
		assertEquals(3, dependencies.size());
		assertEquals(new Dependency("org.ow2.asm", "asm-test", "7.1", Scope.TEST, false), dependencies.get(0));
		assertEquals(new Dependency("org.junit.jupiter", "junit-jupiter-api", "5.3.2", Scope.TEST, false), dependencies.get(1));
		assertEquals(new Dependency("org.junit.jupiter", "junit-jupiter-params", "5.3.2", Scope.TEST, false), dependencies.get(2));

	}

	@Test
	void read_ASMTree() throws POMException {

		// prepare
		InputStream inputStream = this.getClass().getResourceAsStream("asm-tree-7.1.pom");

		// test
		POM pom = reader.read(inputStream);

		// assert
		assertNotNull(pom);
		assertEquals("org.ow2.asm", pom.getGroupId());
		assertEquals("asm-tree", pom.getArtifactId());
		assertEquals("7.1", pom.getVersion());
		assertNotNull(pom.getParent());
		assertEquals("org.ow2", pom.getParent().getGroupId());
		assertEquals("ow2", pom.getParent().getArtifactId());
		assertEquals("1.5", pom.getParent().getVersion());
		assertEquals("asm-tree", pom.getName());
		assertEquals("Tree API of ASM, a very small and fast Java bytecode manipulation framework", pom.getDescription());

		assertEquals(new ArrayList<>(), pom.getPropertyNames());

		List<Dependency> dependencies = pom.getDependencies();
		assertEquals(4, dependencies.size());
		assertEquals(new Dependency("org.ow2.asm", "asm", "7.1", Scope.COMPILE, false), dependencies.get(0));
		assertEquals(new Dependency("org.ow2.asm", "asm-test", "7.1", Scope.TEST, false), dependencies.get(1));
		assertEquals(new Dependency("org.junit.jupiter", "junit-jupiter-api", "5.3.2", Scope.TEST, false), dependencies.get(2));
		assertEquals(new Dependency("org.junit.jupiter", "junit-jupiter-params", "5.3.2", Scope.TEST, false), dependencies.get(3));

	}

	@Test
	void read_CommonsIO() throws POMException {

		// prepare
		InputStream inputStream = this.getClass().getResourceAsStream("commons-io-2.6.pom");

		// test
		POM pom = reader.read(inputStream);

		// assert
		assertNotNull(pom);
		assertEquals("commons-io", pom.getGroupId());
		assertEquals("commons-io", pom.getArtifactId());
		assertEquals("2.6", pom.getVersion());
		assertNotNull(pom.getParent());
		assertEquals("org.apache.commons", pom.getParent().getGroupId());
		assertEquals("commons-parent", pom.getParent().getArtifactId());
		assertEquals("42", pom.getParent().getVersion());
		assertEquals("Apache Commons IO", pom.getName());
		assertEquals("The Apache Commons IO library contains utility classes, stream implementations, file filters,\n\t\tfile comparators, endian transformation classes, and much more.", pom.getDescription());

		assertEquals(13, pom.getPropertyNames().size());
		assertTrue(pom.hasProperty("maven.compiler.source"));
		assertEquals("1.7", pom.getProperty("maven.compiler.source"));

		List<Dependency> dependencies = pom.getDependencies();
		assertEquals(1, dependencies.size());
		assertEquals(new Dependency("junit", "junit", "4.12", Scope.TEST, false), dependencies.get(0));

	}

	@Test
	void read_Mime4j() throws POMException {

		// prepare
		InputStream inputStream = this.getClass().getResourceAsStream("apache-mime4j-core-0.7.2.pom");

		// test
		POM pom = reader.read(inputStream);

		// assert
		assertNotNull(pom);
		assertEquals("org.apache.james", pom.getGroupId());
		assertEquals("apache-mime4j-core", pom.getArtifactId());
		assertEquals("0.7.2", pom.getVersion());
		assertNotNull(pom.getParent());
		assertEquals("org.apache.james", pom.getParent().getGroupId());
		assertEquals("apache-mime4j-project", pom.getParent().getArtifactId());
		assertEquals("0.7.2", pom.getParent().getVersion());
		assertEquals("Apache JAMES Mime4j (Core)", pom.getName());
		assertEquals("", pom.getDescription());

		assertEquals(new ArrayList<>(), pom.getPropertyNames());

		List<Dependency> dependencies = pom.getDependencies();
		assertEquals(2, dependencies.size());
		assertEquals(new Dependency("junit", "junit", "", Scope.TEST, true), dependencies.get(0));
		assertEquals(new Dependency("commons-io", "commons-io", "", Scope.TEST, true), dependencies.get(1));

	}

	@Test()
	void read_throwsException_forIllegalXmlFile() {

		// prepare
		InputStream inputStream = new ByteArrayInputStream("<?xml version=\"1.0\" encoding=\"UTF-8\"?><project>".getBytes(StandardCharsets.UTF_8));

		// test and assert
		assertThrows(POMException.class, () -> reader.read(inputStream));

	}

}