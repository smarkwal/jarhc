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
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ModuleInfoTest {

	@Test
	void test_toString() {

		// prepare
		ModuleInfo moduleInfo = ModuleInfo
				.forModuleName("m")
				.addPackage("m")
				.addRequires("java.base").addRequires("x").addRequires("y")
				.addExports("a").addExports("b", "s", "t")
				.addOpens("c").addOpens("d", "u", "v");

		// test
		String result = moduleInfo.toString();

		// assert
		assertEquals("ModuleInfo[m,requires=[java.base, x, y],exports=[a, b],opens=[c, d]]", result);

	}

	@Test
	void test_toString_forAutomaticModule() {

		// prepare
		ModuleInfo moduleInfo = ModuleInfo
				.forModuleName("m")
				.setAutomatic(true);

		// test
		String result = moduleInfo.toString();

		// assert
		assertEquals("ModuleInfo[m,automatic]", result);

	}

	@Test
	void test_toString_forUnnamedModule() {

		// prepare
		ModuleInfo moduleInfo = ModuleInfo.UNNAMED;

		// test
		String result = moduleInfo.toString();

		// assert
		assertEquals("ModuleInfo[UNNAMED]", result);

	}

	@Test
	void getRelease() {

		// prepare
		ModuleInfo moduleInfo = ModuleInfo
				.forModuleName("m")
				.setRelease(11);

		// test
		int result = moduleInfo.getRelease();

		// assert
		assertEquals(11, result);

	}

	@Test
	void isAutomatic_returnsFalse_forModule() {

		// prepare
		ModuleInfo moduleInfo = ModuleInfo
				.forModuleName("m");

		// test
		boolean result = moduleInfo.isAutomatic();

		// assert
		assertFalse(result);

	}

	@Test
	void isAutomatic_returnsTrue_forAutomaticModule() {

		// prepare
		ModuleInfo moduleInfo = ModuleInfo
				.forModuleName("m")
				.setAutomatic(true);

		// test
		boolean result = moduleInfo.isAutomatic();

		// assert
		assertTrue(result);

	}

}
