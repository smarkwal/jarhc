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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.jarhc.TestUtils;
import org.jarhc.model.ClassDef;
import org.jarhc.model.ClassRef;
import org.jarhc.model.FieldRef;
import org.jarhc.model.MethodRef;
import org.junit.jupiter.api.Test;

class ClassScannerTest {

	@Test
	void test_getClassRefs() throws IOException {

		// test
		ClassDef classDef = scanTestClass();

		// assert
		List<ClassRef> classRefs = new ArrayList<>(classDef.getClassRefs());
		String[] classNames = new String[]{
				"a.Main",
				"a.Base",
				"a.Interface",
				"a.CustomException",
				"java.lang.RuntimeException",
				"java.lang.Object",
				"java.lang.String",
				// TODO: "java.lang.Number",
				"java.lang.Long",
				// TODO: "java.lang.Boolean",
				"java.lang.System",
				"java.io.PrintStream",
				"java.util.List",
				"java.util.ArrayList",
				"a.Main$InnerMain",
				"a.Main$StaticInnerMain"
		};
		for (String className : classNames) {
			assertTrue(classRefs.contains(new ClassRef(className)), className);
		}
		assertEquals(classNames.length, classRefs.size());

	}

	@Test
	void test_getFieldRefs() throws IOException {

		// test
		ClassDef classDef = scanTestClass();

		// assert
		List<FieldRef> fieldRefs = classDef.getFieldRefs();

		FieldRef outField = fieldRefs.stream().filter(f -> f.getFieldName().equals("out")).findFirst().orElse(null);
		assertNotNull(outField);
		assertEquals("static java.io.PrintStream java.lang.System.out", outField.getDisplayName());

		assertEquals(1, fieldRefs.size());
	}

	@Test
	void test_getMethodRefs() throws IOException {

		// test
		ClassDef classDef = scanTestClass();

		// assert
		List<MethodRef> methodRefs = classDef.getMethodRefs();

		MethodRef initBaseMethod = methodRefs.stream().filter(f -> f.getMethodName().equals("<init>") && f.getMethodOwner().contains("Base")).findFirst().orElse(null);
		assertNotNull(initBaseMethod);
		assertEquals("void a.Base.<init>()", initBaseMethod.getDisplayName());

		MethodRef initMainMethod = methodRefs.stream().filter(f -> f.getMethodName().equals("<init>") && f.getMethodOwner().contains("Main")).findFirst().orElse(null);
		assertNotNull(initMainMethod);
		assertEquals("void a.Main.<init>()", initMainMethod.getDisplayName());

		MethodRef printlnObjectMethod = methodRefs.stream().filter(f -> f.getMethodName().equals("println") && f.getMethodDescriptor().contains("Object")).findFirst().orElse(null);
		assertNotNull(printlnObjectMethod);
		assertEquals("void java.io.PrintStream.println(java.lang.Object)", printlnObjectMethod.getDisplayName());

		MethodRef printlnStringMethod = methodRefs.stream().filter(f -> f.getMethodName().equals("println") && f.getMethodDescriptor().contains("String")).findFirst().orElse(null);
		assertNotNull(printlnStringMethod);
		assertEquals("void java.io.PrintStream.println(java.lang.String)", printlnStringMethod.getDisplayName());

		MethodRef initLongMethod = methodRefs.stream().filter(f -> f.getMethodName().equals("<init>") && f.getMethodOwner().contains("Long")).findFirst().orElse(null);
		assertNotNull(initLongMethod);
		assertEquals("void java.lang.Long.<init>(long)", initLongMethod.getDisplayName());

		MethodRef initArrayListMethod = methodRefs.stream().filter(f -> f.getMethodName().equals("<init>") && f.getMethodOwner().contains("ArrayList")).findFirst().orElse(null);
		assertNotNull(initArrayListMethod);
		assertEquals("void java.util.ArrayList.<init>()", initArrayListMethod.getDisplayName());

		assertEquals(6, methodRefs.size());
	}

	private ClassDef scanTestClass() throws IOException {

		// prepare
		ClassDefLoader loader = new ClassDefLoader("Classpath", true);

		// test
		try (InputStream stream = TestUtils.getResourceAsStream("/org/jarhc/loader/ClassScannerTest/Main.class")) {
			return loader.load(stream);
		}

	}

}
