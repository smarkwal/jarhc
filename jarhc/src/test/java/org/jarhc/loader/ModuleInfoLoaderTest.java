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

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import org.jarhc.TestUtils;
import org.jarhc.model.ModuleInfo;
import org.jarhc.utils.IOUtils;
import org.junit.jupiter.api.Test;

class ModuleInfoLoaderTest {

	private final ModuleInfoLoader moduleInfoLoader = LoaderBuilder.create().buildModuleInfoLoader();

	@Test
	void test_load() throws IOException {

		String resource = "/org/jarhc/loader/ModuleInfoLoaderTest/module-info.class";
		ModuleInfo moduleInfo = loadModuleInfo(resource);

		assertNotNull(moduleInfo);
		assertEquals("c", moduleInfo.getModuleName());
		assertEquals(List.of("c"), moduleInfo.getExports());
		assertEquals(List.of("java.base", "java.xml"), moduleInfo.getRequires());
	}

	private ModuleInfo loadModuleInfo(String resource) throws IOException {
		try (InputStream stream = TestUtils.getResourceAsStream(resource)) {
			byte[] data = IOUtils.toByteArray(stream);
			return moduleInfoLoader.load(data);
		}
	}

}
