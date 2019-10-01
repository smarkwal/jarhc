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

class ModelReaderTest {

	private final ModelReader modelReader = new ModelReader();

	@Test
	void read_ASM() throws ModelException {

		// prepare
		InputStream inputStream = this.getClass().getResourceAsStream("asm-7.1.pom");

		// test
		Model model = modelReader.read(inputStream);

		// assert
		assertNotNull(model);
		assertEquals("org.ow2.asm", model.getGroupId());
		assertEquals("asm", model.getArtifactId());
		assertEquals("7.1", model.getVersion());
		assertEquals("org.ow2", model.getParentGroupId());
		assertEquals("ow2", model.getParentArtifactId());
		assertEquals("1.5", model.getParentVersion());
		assertEquals("asm", model.getName());
		assertEquals("ASM, a very small and fast Java bytecode manipulation framework", model.getDescription());

		assertEquals(new ArrayList<>(), model.getPropertyNames());

		List<Dependency> dependencies = model.getDependencies();
		assertEquals(3, dependencies.size());
		assertEquals(new Dependency("org.ow2.asm", "asm-test", "7.1", Scope.TEST, false), dependencies.get(0));
		assertEquals(new Dependency("org.junit.jupiter", "junit-jupiter-api", "5.3.2", Scope.TEST, false), dependencies.get(1));
		assertEquals(new Dependency("org.junit.jupiter", "junit-jupiter-params", "5.3.2", Scope.TEST, false), dependencies.get(2));

	}

	@Test
	void read_ASMTree() throws ModelException {

		// prepare
		InputStream inputStream = this.getClass().getResourceAsStream("asm-tree-7.1.pom");

		// test
		Model model = modelReader.read(inputStream);

		// assert
		assertNotNull(model);
		assertEquals("org.ow2.asm", model.getGroupId());
		assertEquals("asm-tree", model.getArtifactId());
		assertEquals("7.1", model.getVersion());
		assertEquals("org.ow2", model.getParentGroupId());
		assertEquals("ow2", model.getParentArtifactId());
		assertEquals("1.5", model.getParentVersion());
		assertEquals("asm-tree", model.getName());
		assertEquals("Tree API of ASM, a very small and fast Java bytecode manipulation framework", model.getDescription());

		assertEquals(new ArrayList<>(), model.getPropertyNames());

		List<Dependency> dependencies = model.getDependencies();
		assertEquals(4, dependencies.size());
		assertEquals(new Dependency("org.ow2.asm", "asm", "7.1", Scope.COMPILE, false), dependencies.get(0));
		assertEquals(new Dependency("org.ow2.asm", "asm-test", "7.1", Scope.TEST, false), dependencies.get(1));
		assertEquals(new Dependency("org.junit.jupiter", "junit-jupiter-api", "5.3.2", Scope.TEST, false), dependencies.get(2));
		assertEquals(new Dependency("org.junit.jupiter", "junit-jupiter-params", "5.3.2", Scope.TEST, false), dependencies.get(3));

	}

	@Test
	void read_CommonsIO() throws ModelException {

		// prepare
		InputStream inputStream = this.getClass().getResourceAsStream("commons-io-2.6.pom");

		// test
		Model model = modelReader.read(inputStream);

		// assert
		assertNotNull(model);
		assertEquals("commons-io", model.getGroupId());
		assertEquals("commons-io", model.getArtifactId());
		assertEquals("2.6", model.getVersion());
		assertEquals("org.apache.commons", model.getParentGroupId());
		assertEquals("commons-parent", model.getParentArtifactId());
		assertEquals("42", model.getParentVersion());
		assertEquals("Apache Commons IO", model.getName());
		assertEquals("The Apache Commons IO library contains utility classes, stream implementations, file filters,\n\t\tfile comparators, endian transformation classes, and much more.", model.getDescription());

		assertEquals(13, model.getPropertyNames().size());
		assertTrue(model.hasProperty("maven.compiler.source"));
		assertEquals("1.7", model.getProperty("maven.compiler.source"));

		List<Dependency> dependencies = model.getDependencies();
		assertEquals(1, dependencies.size());
		assertEquals(new Dependency("junit", "junit", "4.12", Scope.TEST, false), dependencies.get(0));

	}

	@Test
	void read_Mime4j() throws ModelException {

		// prepare
		InputStream inputStream = this.getClass().getResourceAsStream("apache-mime4j-core-0.7.2.pom");

		// test
		Model model = modelReader.read(inputStream);

		// assert
		assertNotNull(model);
		assertEquals("org.apache.james", model.getGroupId());
		assertEquals("apache-mime4j-core", model.getArtifactId());
		assertEquals("0.7.2", model.getVersion());
		assertEquals("org.apache.james", model.getParentGroupId());
		assertEquals("apache-mime4j-project", model.getParentArtifactId());
		assertEquals("0.7.2", model.getParentVersion());
		assertEquals("Apache JAMES Mime4j (Core)", model.getName());
		assertEquals("", model.getDescription());

		assertEquals(new ArrayList<>(), model.getPropertyNames());

		List<Dependency> dependencies = model.getDependencies();
		assertEquals(2, dependencies.size());
		assertEquals(new Dependency("junit", "junit", "", Scope.TEST, true), dependencies.get(0));
		assertEquals(new Dependency("commons-io", "commons-io", "", Scope.TEST, true), dependencies.get(1));

	}

	@Test()
	void read_throwsException_forIllegalXmlFile() {

		// prepare
		InputStream inputStream = new ByteArrayInputStream("<?xml version=\"1.0\" encoding=\"UTF-8\"?><project>".getBytes(StandardCharsets.UTF_8));

		// test and assert
		assertThrows(ModelException.class, () -> modelReader.read(inputStream));

	}

}