package org.jarcheck.loader;

import org.jarcheck.TestUtils;
import org.jarcheck.model.ClassRef;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClassRefFinderTest {

	@Test
	void test_findClassRefs() throws IOException {

		// prepare
		ClassNode classNode = new ClassNode();
		try (InputStream stream = TestUtils.getResourceAsStream("/test3/Main.class")) {
			ClassReader classReader = new ClassReader(stream);
			classReader.accept(classNode, 0);
		}

		// test
		List<ClassRef> classRefs = ClassRefFinder.findClassRefs(classNode);

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
