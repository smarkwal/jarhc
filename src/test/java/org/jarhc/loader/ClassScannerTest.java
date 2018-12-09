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

import org.jarhc.TestUtils;
import org.jarhc.model.ClassRef;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClassScannerTest {

	@Test
	void test_findClassRefs() throws IOException {

		// prepare
		ClassNode classNode = new ClassNode();
		try (InputStream stream = TestUtils.getResourceAsStream("/ClassRefFinderTest/Main.class")) {
			ClassReader classReader = new ClassReader(stream);
			classReader.accept(classNode, 0);
		}

		// test
		ClassScanner scanner = new ClassScanner();
		scanner.scan(classNode);
		List<ClassRef> classRefs = new ArrayList<>(scanner.getClassRefs());

		// assert
		String[] classNames = new String[]{
				"a/Main",
				"a/Base",
				"a/Interface",
				"a/ClassAnnotation",
				"a/FieldAnnotation",
				"a/MethodAnnotation",
				"a/CustomException",
				"java/lang/RuntimeException",
				"java/lang/Object",
				"java/lang/String",
				// TODO: "java/lang/Number",
				"java/lang/Long",
				// TODO: "java/lang/Boolean",
				"java/lang/System",
				"java/io/PrintStream",
				"java/util/List",
				"java/util/ArrayList",
				"a/Main$InnerMain",
				"a/Main$StaticInnerMain"
		};
		for (String className : classNames) {
			assertTrue(classRefs.contains(new ClassRef(className)), className);
		}
		assertEquals(classNames.length, classRefs.size());

	}

}
