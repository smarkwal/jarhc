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

package org.jarhc.it;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.File;
import java.util.Collections;
import org.jarhc.loader.ClasspathLoader;
import org.jarhc.loader.LoaderBuilder;
import org.jarhc.model.Classpath;
import org.jarhc.model.JarFile;
import org.jarhc.model.ModuleInfo;
import org.jarhc.utils.JavaUtils;
import org.junit.jupiter.api.Test;

class ModuleInfoBuilderTest {

	@Test
	void test_java_base_jmod() {

		ModuleInfo moduleInfo = loadModuleInfo("java.base");

		assertTrue(moduleInfo.getRequires().isEmpty());
		assertTrue(moduleInfo.getPackages().size() > 100);
		assertTrue(moduleInfo.getPackages().contains("java.lang"));
		assertTrue(moduleInfo.getExports().size() > 100);
		assertTrue(moduleInfo.getExports().contains("java.lang"));
		assertTrue(moduleInfo.getOpens().isEmpty());

		assertTrue(moduleInfo.isExported("java.util.zip", "UNNAMED"));
		assertTrue(moduleInfo.isExported("jdk.internal.jimage", "jdk.jlink"));
		assertFalse(moduleInfo.isExported("jdk.internal.jimage", "any.module"));
		assertFalse(moduleInfo.isExported("jdk.internal.jimage", "UNNAMED"));
	}

	@Test
	void test_java_se_jmod() {

		ModuleInfo moduleInfo = loadModuleInfo("java.se");

		assertTrue(moduleInfo.getRequires().size() > 10);
		assertTrue(moduleInfo.getRequires().contains("java.base"));
		assertTrue(moduleInfo.getPackages().isEmpty());
		assertTrue(moduleInfo.getExports().isEmpty());
		assertTrue(moduleInfo.getOpens().isEmpty());
	}

	@Test
	void test_jdk_unsupported_jmod() {

		ModuleInfo moduleInfo = loadModuleInfo("jdk.unsupported");

		assertTrue(moduleInfo.getRequires().contains("java.base"));
		assertTrue(moduleInfo.getPackages().contains("sun.misc"));
		assertTrue(moduleInfo.getExports().contains("sun.misc"));
		assertTrue(moduleInfo.getOpens().contains("sun.misc"));

		assertTrue(moduleInfo.isExported("sun.misc", "any.module"));
		assertTrue(moduleInfo.isExported("sun.misc", "UNNAMED"));
		assertTrue(moduleInfo.isOpen("sun.misc", "any.module"));
		assertTrue(moduleInfo.isOpen("sun.misc", "UNNAMED"));
	}

	private ModuleInfo loadModuleInfo(String moduleName) {

		// find module file
		File jmodFile = JavaUtils.getJavaModuleFile(moduleName);
		assumeTrue(jmodFile.isFile(), "Java runtime is a JDK.");

		// load module file
		ClasspathLoader classpathLoader = LoaderBuilder.create().buildClasspathLoader();
		Classpath classpath = classpathLoader.load(Collections.singleton(jmodFile));

		// get JAR file created for module
		JarFile jarFile = classpath.getJarFile(moduleName + ".jmod");
		assertNotNull(jarFile);

		// assert
		ModuleInfo moduleInfo = jarFile.getModuleInfo();
		assertNotNull(moduleInfo);
		assertEquals(moduleName, moduleInfo.getModuleName());
		assertTrue(moduleInfo.isNamed());
		assertFalse(moduleInfo.isUnnamed());
		assertFalse(moduleInfo.isAutomatic());
		// assertEquals(javaVersion, moduleInfo.getRelease());

		return moduleInfo;
	}

}
