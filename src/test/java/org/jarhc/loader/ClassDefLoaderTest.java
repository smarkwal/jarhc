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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import org.jarhc.TestUtils;
import org.jarhc.model.ClassDef;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ClassDefLoaderTest {

	private final ClassDefLoader classDefLoader = LoaderBuilder.create().buildClassDefLoader();

	@Test
	void test_load_java11() throws IOException {

		String resource = "/ClassDefLoaderTest/java11/Main.class";
		ClassDef classDef = loadClass(resource);

		assertNotNull(classDef);
		assertEquals("Main", classDef.getClassName());
		assertEquals(55, classDef.getMajorClassVersion());
		assertEquals("Java 11", classDef.getJavaVersion());
	}

	@Test
	void test_load_java10() throws IOException {

		String resource = "/ClassDefLoaderTest/java10/Main.class";
		ClassDef classDef = loadClass(resource);

		assertNotNull(classDef);
		assertEquals("Main", classDef.getClassName());
		assertEquals(54, classDef.getMajorClassVersion());
		assertEquals("Java 10", classDef.getJavaVersion());
	}

	@Test
	void test_load_java9() throws IOException {

		String resource = "/ClassDefLoaderTest/java9/Main.class";
		ClassDef classDef = loadClass(resource);

		assertNotNull(classDef);
		assertEquals("Main", classDef.getClassName());
		assertEquals(53, classDef.getMajorClassVersion());
		assertEquals("Java 9", classDef.getJavaVersion());
	}

	@Test
	void test_load_java8() throws IOException {

		String resource = "/ClassDefLoaderTest/java8/Main.class";
		ClassDef classDef = loadClass(resource);

		assertNotNull(classDef);
		assertEquals("Main", classDef.getClassName());
		assertEquals(52, classDef.getMajorClassVersion());
		assertEquals("Java 8", classDef.getJavaVersion());
	}

	@Test
	void test_load_java7() throws IOException {

		String resource = "/ClassDefLoaderTest/java7/Main.class";
		ClassDef classDef = loadClass(resource);

		assertNotNull(classDef);
		assertEquals("Main", classDef.getClassName());
		assertEquals(51, classDef.getMajorClassVersion());
		assertEquals("Java 7", classDef.getJavaVersion());
	}

	@Test
	void test_load_java6() throws IOException {

		String resource = "/ClassDefLoaderTest/java6/Main.class";
		ClassDef classDef = loadClass(resource);

		assertNotNull(classDef);
		assertEquals("Main", classDef.getClassName());
		assertEquals(50, classDef.getMajorClassVersion());
		assertEquals("Java 6", classDef.getJavaVersion());
	}

	@Test
	void test_load_file(@TempDir Path tempDir) throws IOException {

		String resource = "/ClassDefLoaderTest/java8/Main.class";
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
