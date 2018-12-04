package org.jarcheck.model;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JarFileTest {

	@Test
	void test_toString() {

		// prepare
		ArrayList<ClassDef> classDefs = new ArrayList<>();
		classDefs.add(new ClassDef("a/A", 52, 0));
		classDefs.add(new ClassDef("b/B", 52, 0));
		JarFile jarFile = new JarFile("abc.jar", 1024, classDefs);

		// test
		String result = jarFile.toString();

		// assert
		assertEquals("JarFile[abc.jar,2]", result);

	}

}
