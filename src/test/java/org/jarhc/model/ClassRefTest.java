package org.jarhc.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ClassRefTest {

	@Test
	void test_toString() {

		// prepare
		ClassRef classRef = new ClassRef("a/A");

		// test
		String result = classRef.toString();

		// assert
		assertEquals("ClassRef[a/A]", result);

	}

}
