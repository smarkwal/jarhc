/*
 * Copyright 2022 Stephan Markwalder
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

package org.jarhc.env;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import org.jarhc.model.ModuleInfo;
import org.jarhc.test.log.LoggerBuilder;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

class ModuleSystemRuntimeTest {

	private final ClassLoader classLoader = ClassLoader.getSystemClassLoader().getParent();
	private final Logger logger = LoggerBuilder.collect(ModuleSystemRuntime.class);
	private final ModuleSystemRuntime runtime = new ModuleSystemRuntime(classLoader, logger);

	@Test
	void findModuleInfo_javaLangString() {

		// test
		ModuleInfo result = runtime.findModuleInfo("java.lang.String");

		// assert
		assertNotNull(result);
		assertTrue(result.isNamed());
		assertEquals("java.base", result.getModuleName());
		assertTrue(result.getPackages().size() > 100);
		assertEquals(Collections.emptyList(), result.getRequires());
		assertTrue(result.getExports().size() > 100);
		assertEquals(Collections.emptyList(), result.getOpens());

	}

	@Test
	void findModuleInfo_sunMiscUnsafe() {

		// test
		ModuleInfo result = runtime.findModuleInfo("sun.misc.Unsafe");

		// assert
		assertNotNull(result);
		assertTrue(result.isNamed());
		assertEquals("jdk.unsupported", result.getModuleName());
		assertTrue(result.getPackages().contains("sun.misc"));
		assertTrue(result.getRequires().contains("java.base"));
		assertTrue(result.getExports().contains("sun.misc"));
		assertTrue(result.getOpens().contains("sun.misc"));

		assertTrue(result.isExported("sun.misc", "any.module"));
		assertTrue(result.isExported("sun.misc", "UNNAMED"));
		assertTrue(result.isOpen("sun.misc", "any.module"));
		assertTrue(result.isOpen("sun.misc", "UNNAMED"));
	}

}
