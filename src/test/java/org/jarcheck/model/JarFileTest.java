package org.jarcheck.model;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JarFileTest {

	@Test
	void test_toString() {

		// prepare
		ArrayList<ClassDef> classDefs = new ArrayList<>();
		classDefs.add(new ClassDef("a/A", 52, 0, ClassRef.NONE));
		classDefs.add(new ClassDef("b/B", 52, 0, ClassRef.NONE));
		JarFile jarFile = new JarFile("abc.jar", 1024, classDefs);

		// test
		String result = jarFile.toString();

		// assert
		assertEquals("JarFile[abc.jar,2]", result);

	}

	@Test
	void test_getClassDefs() {

		// prepare
		ArrayList<ClassDef> classDefs = new ArrayList<>();
		classDefs.add(new ClassDef("a/A", 52, 0, ClassRef.NONE));
		classDefs.add(new ClassDef("b/B", 52, 0, ClassRef.NONE));
		JarFile jarFile = new JarFile("abc.jar", 1024, classDefs);

		// test
		List<ClassDef> result = jarFile.getClassDefs();

		// assert
		assertEquals(2, result.size());
		assertEquals("a/A", result.get(0).getClassName());
		assertEquals("b/B", result.get(1).getClassName());

	}

	@Test
	void test_getClassDef() {

		// prepare
		ArrayList<ClassDef> classDefs = new ArrayList<>();
		classDefs.add(new ClassDef("a/A", 52, 0, ClassRef.NONE));
		classDefs.add(new ClassDef("b/B", 52, 0, ClassRef.NONE));
		JarFile jarFile = new JarFile("abc.jar", 1024, classDefs);

		// test
		ClassDef result = jarFile.getClassDef("b/B");

		// assert
		assertNotNull(result);
		assertEquals("b/B", result.getClassName());

		// test
		result = jarFile.getClassDef("c/C");

		// assert
		assertNull(result);

	}

}
