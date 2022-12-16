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

import static org.jarhc.model.AnnotationRef.Target;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.jarhc.TestUtils;
import org.jarhc.model.ClassDef;
import org.jarhc.model.ClassRef;
import org.jarhc.model.FieldDef;
import org.jarhc.model.MethodDef;
import org.jarhc.model.RecordComponentDef;
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
		assertEquals("public record", classDef.getModifiers());
		assertEquals("Record", classDef.getClassName());
		assertEquals("java.lang.Record", classDef.getSuperName());
		assertEquals(60, classDef.getMajorClassVersion());
		assertEquals("Java 16", classDef.getJavaVersion());

		List<ClassRef> classRefs = classDef.getClassRefs();
		assertEquals(5, classRefs.size());

		List<RecordComponentDef> recordComponentDefs = classDef.getRecordComponentDefs();
		assertEquals(3, recordComponentDefs.size());

		RecordComponentDef recordComponentDef = recordComponentDefs.get(0);
		assertEquals("id", recordComponentDef.getName());
		assertEquals("int", recordComponentDef.getType());
		assertEquals(0, recordComponentDef.getAnnotationRefs().size());

		recordComponentDef = recordComponentDefs.get(1);
		assertEquals("name", recordComponentDef.getName());
		assertEquals("java.lang.String", recordComponentDef.getType());
		assertTrue(recordComponentDef.hasAnnotationRef("MyAnnotation", Target.RECORD_COMPONENT));
		// Note: @Deprecated annotation is present only on this record component
		// because it has only @Target({RECORD_COMPONENT}).

		recordComponentDef = recordComponentDefs.get(2);
		assertEquals("enabled", recordComponentDef.getName());
		assertEquals("boolean", recordComponentDef.getType());
		assertEquals(0, recordComponentDef.getAnnotationRefs().size());
		// Note: @Deprecated annotation is not present on record component
		// because it does not have @Target({RECORD_COMPONENT}).

		List<FieldDef> fieldDefs = classDef.getFieldDefs();
		assertEquals(3, fieldDefs.size());

		FieldDef fieldDef = fieldDefs.get(0);
		assertEquals("id", fieldDef.getFieldName());
		assertEquals("int", fieldDef.getFieldType());
		assertEquals("private final", fieldDef.getModifiers());
		assertEquals(0, fieldDef.getAnnotationRefs().size());

		fieldDef = fieldDefs.get(1);
		assertEquals("name", fieldDef.getFieldName());
		assertEquals("java.lang.String", fieldDef.getFieldType());
		assertEquals("private final", fieldDef.getModifiers());
		assertEquals(0, fieldDef.getAnnotationRefs().size());

		fieldDef = fieldDefs.get(2);
		assertEquals("enabled", fieldDef.getFieldName());
		assertEquals("boolean", fieldDef.getFieldType());
		assertEquals("private final", fieldDef.getModifiers());
		assertTrue(fieldDef.hasAnnotationRef("java.lang.Deprecated", Target.FIELD));

		List<MethodDef> methodDefs = classDef.getMethodDefs();
		assertEquals(7, methodDefs.size());

		MethodDef methodDef = classDef.getMethodDef("<init>", "(ILjava/lang/String;Z)V");
		if (methodDef == null) {
			throw new AssertionError();
		}
		assertEquals("public", methodDef.getModifiers());
		assertTrue(methodDef.hasAnnotationRef("java.lang.Deprecated", Target.PARAMETER));

		methodDef = classDef.getMethodDef("id", "()I");
		if (methodDef == null) {
			throw new AssertionError();
		}
		assertEquals("public", methodDef.getModifiers());
		assertEquals(0, methodDef.getAnnotationRefs().size());

		methodDef = classDef.getMethodDef("name", "()Ljava/lang/String;");
		if (methodDef == null) {
			throw new AssertionError();
		}
		assertEquals("public", methodDef.getModifiers());
		assertEquals(0, methodDef.getAnnotationRefs().size());

		methodDef = classDef.getMethodDef("enabled", "()Z");
		if (methodDef == null) {
			throw new AssertionError();
		}
		assertEquals("public", methodDef.getModifiers());
		assertTrue(methodDef.hasAnnotationRef("java.lang.Deprecated", Target.METHOD));

		String expectedApiDescription = "public record Record\n" +
				"extends: java.lang.Record\n" +
				"implements: []\n" +
				"permits: []\n" +
				"record component: boolean Record.enabled\n" +
				"record component: int Record.id\n" +
				"record component: java.lang.String Record.name\n" +
				"method: public boolean Record.enabled()\n" +
				"method: public final boolean Record.equals(java.lang.Object)\n" +
				"method: public final int Record.hashCode()\n" +
				"method: public final java.lang.String Record.toString()\n" +
				"method: public int Record.id()\n" +
				"method: public java.lang.String Record.name()\n" +
				"method: public void Record.<init>(int,java.lang.String,boolean)";

		String apiDescription = classDef.getApiDescription();
		assertEquals(expectedApiDescription, apiDescription);
	}

	@Test
	void test_load_class_refs(@TempDir Path tempDir) throws IOException {

		String resource = "/org/jarhc/loader/ClassDefLoaderTest/java6/ClassRefs.class";
		File file = TestUtils.getResourceAsFile(resource, tempDir);
		ClassDef classDef = classDefLoader.load(file);

		assertNotNull(classDef);
		assertEquals("ClassRefs", classDef.getClassName());
		assertEquals(50, classDef.getMajorClassVersion());
		assertEquals("Java 6", classDef.getJavaVersion());

		List<ClassRef> classRefs = classDef.getClassRefs();
		assertTrue(classRefs.contains(new ClassRef("java.lang.Object")));
		assertTrue(classRefs.contains(new ClassRef("java.util.ArrayList")));
	}

	@Test
	void test_load_annotation_refs(@TempDir Path tempDir) throws IOException {

		String resource = "/org/jarhc/loader/ClassDefLoaderTest/java9/AnnotationRefs.class";
		File file = TestUtils.getResourceAsFile(resource, tempDir);
		ClassDef classDef = classDefLoader.load(file);

		assertNotNull(classDef);
		assertEquals("AnnotationRefs", classDef.getClassName());
		assertEquals(53, classDef.getMajorClassVersion());
		assertEquals("Java 9", classDef.getJavaVersion());

		assertEquals(2, classDef.getAnnotationRefs().size());
		assertTrue(classDef.hasAnnotationRef("Annotations$TypeAnnotation", Target.TYPE));
		assertTrue(classDef.hasAnnotationRef("Annotations$TypeParameterAnnotation", Target.TYPE_PARAMETER));

		MethodDef constructorDef = classDef.getMethodDef("<init>", "(Ljava/lang/Object;)V");
		if (constructorDef == null) {
			throw new AssertionError("Constructor not found");
		}
		assertEquals(1, constructorDef.getAnnotationRefs().size());
		assertTrue(constructorDef.hasAnnotationRef("Annotations$ConstructorAnnotation", Target.CONSTRUCTOR));

		MethodDef methodDef = classDef.getMethodDef("method", "(I)V");
		if (methodDef == null) {
			throw new AssertionError("Method not found");
		}
		assertEquals(2, methodDef.getAnnotationRefs().size());
		assertTrue(methodDef.hasAnnotationRef("Annotations$MethodAnnotation", Target.METHOD));
		assertTrue(methodDef.hasAnnotationRef("Annotations$ParameterAnnotation", Target.PARAMETER));

		FieldDef fieldDef = classDef.getFieldDef("field");
		if (fieldDef == null) {
			throw new AssertionError("Field not found");
		}
		assertEquals(1, fieldDef.getAnnotationRefs().size());
		assertTrue(fieldDef.hasAnnotationRef("Annotations$FieldAnnotation", Target.FIELD));

		FieldDef fieldDef2 = classDef.getFieldDef("field2");
		if (fieldDef2 == null) {
			throw new AssertionError("Field 2 not found");
		}
		assertEquals(1, fieldDef2.getAnnotationRefs().size());
		assertTrue(fieldDef2.hasAnnotationRef("Annotations$TypeUseAnnotation", Target.TYPE_PARAMETER));

	}

	@Test
	void test_load_annotation_refs_2(@TempDir Path tempDir) throws IOException {

		String resource = "/org/jarhc/loader/ClassDefLoaderTest/java9/AnnotationRefs$MyAnnotation.class";
		File file = TestUtils.getResourceAsFile(resource, tempDir);
		ClassDef classDef = classDefLoader.load(file);

		assertNotNull(classDef);
		assertEquals("AnnotationRefs$MyAnnotation", classDef.getClassName());
		assertEquals(53, classDef.getMajorClassVersion());
		assertEquals("Java 9", classDef.getJavaVersion());

		assertEquals(1, classDef.getAnnotationRefs().size());
		assertTrue(classDef.hasAnnotationRef("Annotations$AnnotationTypeAnnotation", Target.ANNOTATION_TYPE));

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

	private ClassDef loadClass(String resource) throws IOException {
		try (InputStream stream = TestUtils.getResourceAsStream(resource)) {
			return classDefLoader.load(stream);
		}
	}

}
