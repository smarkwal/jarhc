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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ModuleInfoTest {

	@Test
	void test_toString() {

		// prepare
		ModuleInfo moduleInfo = ModuleInfo
				.forModuleName("m")
				.exports("a").exports("b")
				.requires("java.base").requires("x").requires("y")
				.build();

		// test
		String result = moduleInfo.toString();

		// assert
		assertEquals("ModuleInfo[m,exports=[a, b],requires=[java.base, x, y]]", result);

	}

}
