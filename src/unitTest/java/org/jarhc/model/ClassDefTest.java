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

package org.jarhc.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class ClassDefTest {

	private final JarFile jarFile = Mockito.mock(JarFile.class);

	private ClassDef classDef;

	@BeforeEach
	void setUp() {

		classDef = ClassDef.forClassName("a.b.C")
				.setClassLoader("Provided")
				.setRelease(11)
				.setMajorClassVersion(52)
				.setMinorClassVersion(1)
				.setClassFileChecksum("1234567890123456789012345678901234567890")
				.setSuperName("s.t.U")
				.addInterfaceName("i.j.K1")
				.addInterfaceNames(Arrays.asList("i.j.K2", "i.j.K3"))
				.addPermittedSubclassName("p.q.R1")
				.addPermittedSubclassNames(Arrays.asList("p.q.R2", "p.q.R3"));
		classDef.setJarFile(jarFile);
		classDef.setModuleInfo(ModuleInfo.UNNAMED);
		classDef.setAccess(Modifier.PUBLIC + Modifier.ABSTRACT);

		classDef.addRecordComponentDef(new RecordComponentDef("enabled", "boolean"));
		classDef.addFieldDef(new FieldDef(Modifier.PROTECTED + Modifier.FINAL, "id", "java.lang.String"));
		classDef.addMethodDef(new MethodDef(Modifier.PUBLIC, "getId", "()Ljava/lang/String;"));

		classDef.addClassRef(new ClassRef("x.y.Z"));
		classDef.addFieldRef(new FieldRef("x.y.Z", "int", "VERSION", true, false));
		classDef.addMethodRef(new MethodRef("x.y.Z", "()I", "getIndex", false, true));

	}

	@Test
	void test_toString() {

		// test
		String result = classDef.toString();

		// assert
		assertEquals("ClassDef[public abstract sealed class a.b.C,52.1]", result);

	}

	@Test
	void builder() {

		// assert
		assertEquals("a.b.C", classDef.getClassName());
		assertEquals("Provided", classDef.getClassLoader());
		assertEquals(11, classDef.getRelease());
		assertEquals(52, classDef.getMajorClassVersion());
		assertEquals(1, classDef.getMinorClassVersion());
		assertEquals("1234567890123456789012345678901234567890", classDef.getClassFileChecksum());
		assertEquals("s.t.U", classDef.getSuperName());
		assertEquals(Arrays.asList("i.j.K1", "i.j.K2", "i.j.K3"), classDef.getInterfaceNames());
		assertEquals(Arrays.asList("p.q.R1", "p.q.R2", "p.q.R3"), classDef.getPermittedSubclassNames());
		assertSame(jarFile, classDef.getJarFile());
		assertEquals(1025, classDef.getAccess());

		assertEquals(1, classDef.getFieldDefs().size());
		assertTrue(classDef.getFieldDef("id").isPresent());
		assertEquals("id", classDef.getFieldDef("id").get().getFieldName());
		assertEquals(1, classDef.getMethodDefs().size());
		assertTrue(classDef.getMethodDef("getId", "()Ljava/lang/String;").isPresent());
		assertEquals("getId", classDef.getMethodDef("getId", "()Ljava/lang/String;").get().getMethodName());

		assertEquals(1, classDef.getClassRefs().size());
		assertEquals("x.y.Z", classDef.getClassRefs().get(0).getClassName());
		assertEquals(1, classDef.getFieldRefs().size());
		assertEquals("VERSION", classDef.getFieldRefs().get(0).getFieldName());
		assertEquals(1, classDef.getMethodRefs().size());
		assertEquals("getIndex", classDef.getMethodRefs().get(0).getMethodName());

		assertTrue(classDef.isRegularClass());
		assertEquals("Java 8", classDef.getJavaVersion());

		assertSame(ModuleInfo.UNNAMED, classDef.getModuleInfo());
	}

	@Test
	void getApiDescription() {

		// test
		String description = classDef.getApiDescription();

		String expected = "public abstract sealed class a.b.C\n" +
				"extends: s.t.U\n" +
				"implements: [i.j.K1, i.j.K2, i.j.K3]\n" +
				"permits: [p.q.R1, p.q.R2, p.q.R3]\n" +
				"record component: boolean a.b.C.enabled\n" +
				"field: protected final java.lang.String a.b.C.id\n" +
				"method: public java.lang.String a.b.C.getId()";

		// assert
		assertEquals(expected, description);

	}

	@Test
	void getApiChecksum() {

		// test
		String checksum = classDef.getApiChecksum();

		// assert
		assertEquals("15d828ab3d18e175481c5f2b9e242cae343b1da8", checksum);

	}

	@Test
	void permittedSubclasses() {

		// prepare
		ClassDef classDef = new ClassDef("a.b.C");

		// assert
		assertFalse(classDef.isSealed());
		assertNotNull(classDef.getPermittedSubclassNames());
		assertTrue(classDef.getPermittedSubclassNames().isEmpty());
		assertEquals("class", classDef.getModifiers());

		// add permitted subclasses
		classDef.addPermittedSubclassNames(Arrays.asList("x.y.Z1", "x.y.Z2"));
		classDef.addPermittedSubclassName("x.y.Z3");

		// assert
		assertTrue(classDef.isSealed());
		assertNotNull(classDef.getPermittedSubclassNames());
		assertEquals(3, classDef.getPermittedSubclassNames().size());
		assertEquals("sealed class", classDef.getModifiers());
		assertEquals(Arrays.asList("x.y.Z1", "x.y.Z2", "x.y.Z3"), classDef.getPermittedSubclassNames());

	}

}
