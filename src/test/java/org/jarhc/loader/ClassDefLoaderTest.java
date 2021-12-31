/*
 * Copyright 2018 Stephan Markwalder
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

package org.jarhc.loader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import org.jarhc.TestUtils;
import org.jarhc.model.ClassDef;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.io.TempDir;

class ClassDefLoaderTest {

	private final ClassDefLoader classDefLoader = LoaderBuilder.create().buildClassDefLoader();

	@TestFactory
	Collection<DynamicTest> test_load_class() {
		return Arrays.asList(
				DynamicTest.dynamicTest("Java 17", () -> test_load_class(17, 61)),
				DynamicTest.dynamicTest("Java 16", () -> test_load_class(16, 60)),
				DynamicTest.dynamicTest("Java 15", () -> test_load_class(15, 59)),
				DynamicTest.dynamicTest("Java 14", () -> test_load_class(14, 58)),
				DynamicTest.dynamicTest("Java 13", () -> test_load_class(13, 57)),
				DynamicTest.dynamicTest("Java 12", () -> test_load_class(12, 56)),
				DynamicTest.dynamicTest("Java 11", () -> test_load_class(11, 55)),
				DynamicTest.dynamicTest("Java 10", () -> test_load_class(10, 54)),
				DynamicTest.dynamicTest("Java 9", () -> test_load_class(9, 53)),
				DynamicTest.dynamicTest("Java 8", () -> test_load_class(8, 52)),
				DynamicTest.dynamicTest("Java 7", () -> test_load_class(7, 51)),
				DynamicTest.dynamicTest("Java 6", () -> test_load_class(6, 50))
		);
	}

	private void test_load_class(int javaMajorVersion, int expectedClassVersion) throws IOException {
		String resource = "/org/jarhc/loader/ClassDefLoaderTest/java" + javaMajorVersion + "/Main.class";
		ClassDef classDef = loadClass(resource);

		assertNotNull(classDef);
		assertEquals("Main", classDef.getClassName());
		assertEquals(expectedClassVersion, classDef.getMajorClassVersion());
		assertEquals("Java " + javaMajorVersion, classDef.getJavaVersion());
	}

	@Test
	void test_load_record() throws IOException {
		String resource = "/org/jarhc/loader/ClassDefLoaderTest/java16/Record.class";
		ClassDef classDef = loadClass(resource);

		assertNotNull(classDef);
		assertEquals("Record", classDef.getClassName());
		assertEquals(60, classDef.getMajorClassVersion());
		assertEquals("Java 16", classDef.getJavaVersion());
	}

	@Test
	void test_load_sealed_class() throws IOException {
		String resource = "/org/jarhc/loader/ClassDefLoaderTest/java17/SealedParent.class";
		ClassDef classDef = loadClass(resource);

		assertNotNull(classDef);
		assertEquals("SealedParent", classDef.getClassName());
		assertEquals(61, classDef.getMajorClassVersion());
		assertEquals("Java 17", classDef.getJavaVersion());
	}

	@Test
	void test_load_final_class() throws IOException {
		String resource = "/org/jarhc/loader/ClassDefLoaderTest/java17/FinalChild.class";
		ClassDef classDef = loadClass(resource);

		assertNotNull(classDef);
		assertEquals("FinalChild", classDef.getClassName());
		assertTrue(classDef.isFinal());
		assertEquals(61, classDef.getMajorClassVersion());
		assertEquals("Java 17", classDef.getJavaVersion());
	}

	@Test
	void test_load_file(@TempDir Path tempDir) throws IOException {

		String resource = "/org/jarhc/loader/ClassDefLoaderTest/java8/Main.class";
		File file = TestUtils.getResourceAsFile(resource, tempDir);
		ClassDef classDef = classDefLoader.load(file);

		assertNotNull(classDef);
		assertEquals("Main", classDef.getClassName());
		assertEquals(52, classDef.getMajorClassVersion());
		assertEquals("Java 8", classDef.getJavaVersion());
	}

	// TODO: test loading of field and method definitions
	// TODO: test loading of class, field, and method references

	/*
	@Test
	void test_getFieldDefs() throws IOException {

		// test
		ClassScanner scanner = scanTestClass();

		// assert
		List<FieldDef> fieldDefs = scanner.getFieldDefs();

		FieldDef nameField = fieldDefs.stream().filter(f -> f.getFieldName().equals("name")).findFirst().orElse(null);
		assertNotNull(nameField);
		assertEquals("public java.lang.String name", nameField.getDisplayName());

		FieldDef numberField = fieldDefs.stream().filter(f -> f.getFieldName().equals("number")).findFirst().orElse(null);
		assertNotNull(numberField);
		assertEquals("public int number", numberField.getDisplayName());

		FieldDef dataField = fieldDefs.stream().filter(f -> f.getFieldName().equals("data")).findFirst().orElse(null);
		assertNotNull(dataField);
		assertEquals("public byte[] data", dataField.getDisplayName());

		assertEquals(3, fieldDefs.size());
	}

	@Test
	void test_getMethodDefs() throws IOException {

		// test
		ClassScanner scanner = scanTestClass();

		// assert
		List<MethodDef> methodDefs = scanner.getMethodDefs();

		MethodDef mainMethod = methodDefs.stream().filter(m -> m.getMethodName().equals("main")).findFirst().orElse(null);
		assertNotNull(mainMethod);
		assertEquals("public static void main(java.lang.String[])", mainMethod.getDisplayName());

		MethodDef testMethod = methodDefs.stream().filter(m -> m.getMethodName().equals("test")).findFirst().orElse(null);
		assertNotNull(testMethod);
		assertEquals("public void test()", testMethod.getDisplayName());

		MethodDef createListMethod = methodDefs.stream().filter(m -> m.getMethodName().equals("createList")).findFirst().orElse(null);
		assertNotNull(createListMethod);
		assertEquals("public static java.util.List createList()", createListMethod.getDisplayName());

		MethodDef initMethod = methodDefs.stream().filter(m -> m.getMethodName().equals("<init>")).findFirst().orElse(null);
		assertNotNull(initMethod);
		assertEquals("public void <init>()", initMethod.getDisplayName());

		assertEquals(4, methodDefs.size());
	}
	*/

	private ClassDef loadClass(String resource) throws IOException {
		try (InputStream stream = TestUtils.getResourceAsStream(resource)) {
			return classDefLoader.load(stream);
		}
	}

}
