package org.jarhc.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ClassDefTest {

	@Test
	void test_toString() {

		// prepare
		ClassDef classDef = new ClassDef("a/b/C", 52, 0, ClassRef.NONE);

		// test
		String result = classDef.toString();

		// assert
		assertEquals("ClassDef[a/b/C,52.0]", result);

	}

}
