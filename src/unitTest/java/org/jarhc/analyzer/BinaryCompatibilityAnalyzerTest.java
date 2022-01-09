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

package org.jarhc.analyzer;

import static org.jarhc.TestUtils.assertValuesEquals;
import static org.jarhc.utils.StringUtils.joinLines;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Modifier;
import java.util.List;
import org.jarhc.app.Options;
import org.jarhc.env.JavaRuntime;
import org.jarhc.model.AnnotationRef;
import org.jarhc.model.ClassDef;
import org.jarhc.model.Classpath;
import org.jarhc.model.ModuleInfo;
import org.jarhc.report.ReportSection;
import org.jarhc.report.ReportTable;
import org.jarhc.test.ClasspathBuilder;
import org.jarhc.test.JavaRuntimeMock;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.Opcodes;

class BinaryCompatibilityAnalyzerTest {

	private final JavaRuntime javaRuntime = JavaRuntimeMock.getOracleRuntime();

	private final Options options = new Options();

	@Test
	void test_analyze() {

		// prepare
		Classpath classpath = ClasspathBuilder.create(javaRuntime)
				.addJarFile("a.jar")
				.addClassDef("a.A").addClassRef("b.B").addClassRef("c.C").addClassRef("java.lang.String")
				.addJarFile("b.jar")
				.addClassDef("b.B")
				.build();

		// test
		BinaryCompatibilityAnalyzer analyzer = new BinaryCompatibilityAnalyzer(options);
		ReportSection section = analyzer.analyze(classpath);

		// assert
		ReportTable table = assertSectionHeader(section);

		List<String[]> rows = table.getRows();
		assertEquals(1, rows.size());
		assertValuesEquals(rows.get(0), "a.jar", joinLines("a.A", "\u2022 Class is not accessible: class b.B", "\u2022 Class not found: c.C (package not found)"));
	}

	@Test
	void test_analyze_withClassFileIssues() {

		// prepare
		Classpath classpath = ClasspathBuilder.create(javaRuntime)
				.addJarFile("a.jar")
				.addClassDef("a.A", 11, 61, 0)
				.build();

		// test
		BinaryCompatibilityAnalyzer analyzer = new BinaryCompatibilityAnalyzer(options);
		ReportSection section = analyzer.analyze(classpath);

		// assert
		ReportTable table = assertSectionHeader(section);

		List<String[]> rows = table.getRows();
		assertEquals(1, rows.size());
		assertValuesEquals(rows.get(0), "a.jar", joinLines("a.A", "\u2022 Compiled for Java 17, but bundled for Java 11."));
	}

	@Test
	void test_analyze_permittedSubclassIsInSameModule() {

		// prepare
		Classpath classpath = ClasspathBuilder.create(javaRuntime)
				.addJarFile("a.jar").addModuleInfo(ModuleInfo.forModuleName("a"))
				.addClassDef(ClassDef.forClassName("a.A").withAccess(Modifier.PUBLIC).addPermittedSubclassName("b.B"))
				.addClassDef(ClassDef.forClassName("b.B").withAccess(Modifier.PUBLIC).setSuperName("a.A"))
				.build();

		// test
		BinaryCompatibilityAnalyzer analyzer = new BinaryCompatibilityAnalyzer(options);
		ReportSection section = analyzer.analyze(classpath);

		// assert
		ReportTable table = assertSectionHeader(section);

		List<String[]> rows = table.getRows();
		assertEquals(0, rows.size());
	}

	@Test
	void test_analyze_permittedSubclassIsNotInSameModule() {

		// prepare
		Classpath classpath = ClasspathBuilder.create(javaRuntime)
				.addJarFile("a.jar").addModuleInfo(ModuleInfo.forModuleName("a"))
				.addClassDef(ClassDef.forClassName("a.A").withAccess(Modifier.PUBLIC).addPermittedSubclassName("b.B"))
				.addJarFile("b.jar").addModuleInfo(ModuleInfo.forModuleName("b"))
				.addClassDef(ClassDef.forClassName("b.B").withAccess(Modifier.PUBLIC).setSuperName("a.A"))
				.build();

		// test
		BinaryCompatibilityAnalyzer analyzer = new BinaryCompatibilityAnalyzer(options);
		ReportSection section = analyzer.analyze(classpath);

		// assert
		ReportTable table = assertSectionHeader(section);

		List<String[]> rows = table.getRows();
		assertEquals(2, rows.size());
		assertValuesEquals(rows.get(0), "a.jar", joinLines("a.A", "\u2022 Permitted subclass is not in same module: public class b.B"));
		assertValuesEquals(rows.get(1), "b.jar", joinLines("b.B", "\u2022 Sealed superclass is not in same module: public sealed class a.A"));
	}

	@Test
	void test_analyze_permittedSubclassIsInSamePackage() {

		// prepare
		Classpath classpath = ClasspathBuilder.create(javaRuntime)
				.addJarFile("a.jar")
				.addClassDef(ClassDef.forClassName("a.A").addPermittedSubclassName("a.B"))
				.addClassDef(ClassDef.forClassName("a.B").setSuperName("a.A"))
				.build();

		// test
		BinaryCompatibilityAnalyzer analyzer = new BinaryCompatibilityAnalyzer(options);
		ReportSection section = analyzer.analyze(classpath);

		// assert
		ReportTable table = assertSectionHeader(section);

		List<String[]> rows = table.getRows();
		assertEquals(0, rows.size());
	}

	@Test
	void test_analyze_permittedSubclassIsNotInSamePackage() {

		// prepare
		Classpath classpath = ClasspathBuilder.create(javaRuntime)
				.addJarFile("a.jar")
				.addClassDef(ClassDef.forClassName("a.A").withAccess(Modifier.PUBLIC).addPermittedSubclassName("b.B"))
				.addClassDef(ClassDef.forClassName("b.B").withAccess(Modifier.PUBLIC).setSuperName("a.A"))
				.build();

		// test
		BinaryCompatibilityAnalyzer analyzer = new BinaryCompatibilityAnalyzer(options);
		ReportSection section = analyzer.analyze(classpath);

		// assert
		ReportTable table = assertSectionHeader(section);

		List<String[]> rows = table.getRows();
		assertEquals(1, rows.size());
		assertValuesEquals(rows.get(0), "a.jar", joinLines("a.A", "\u2022 Permitted subclass is not in same package: public class b.B", "", "b.B", "\u2022 Sealed superclass is not in same package: public sealed class a.A"));
	}

	@Test
	void test_analyze_permittedSubclassNotFound() {

		// prepare
		Classpath classpath = ClasspathBuilder.create(javaRuntime)
				.addJarFile("a.jar")
				.addClassDef(ClassDef.forClassName("a.A").addPermittedSubclassName("a.B"))
				.build();

		// test
		BinaryCompatibilityAnalyzer analyzer = new BinaryCompatibilityAnalyzer(options);
		ReportSection section = analyzer.analyze(classpath);

		// assert
		ReportTable table = assertSectionHeader(section);

		List<String[]> rows = table.getRows();
		assertEquals(1, rows.size());
		assertValuesEquals(rows.get(0), "a.jar", joinLines("a.A", "\u2022 Permitted subclass not found: a.B"));
	}

	@Test
	void test_analyze_permittedSubclassDoesNotExtendSealedClass() {

		// prepare
		Classpath classpath = ClasspathBuilder.create(javaRuntime)
				.addJarFile("a.jar")
				.addClassDef(ClassDef.forClassName("a.A").addPermittedSubclassName("a.B"))
				.addClassDef(ClassDef.forClassName("a.B"))
				.build();

		// test
		BinaryCompatibilityAnalyzer analyzer = new BinaryCompatibilityAnalyzer(options);
		ReportSection section = analyzer.analyze(classpath);

		// assert
		ReportTable table = assertSectionHeader(section);

		List<String[]> rows = table.getRows();
		assertEquals(1, rows.size());
		assertValuesEquals(rows.get(0), "a.jar", joinLines("a.A", "\u2022 Permitted subclass does not extend sealed class: class a.B"));
	}

	@Test
	void test_analyze_classIsNotAPermittedSubclassOfSealedSuperclass() {

		// prepare
		Classpath classpath = ClasspathBuilder.create(javaRuntime)
				.addJarFile("a.jar")
				.addClassDef(ClassDef.forClassName("a.A").addPermittedSubclassName("a.B"))
				.addClassDef(ClassDef.forClassName("a.B").setSuperName("a.A"))
				.addClassDef(ClassDef.forClassName("a.C").setSuperName("a.A"))
				.build();

		// test
		BinaryCompatibilityAnalyzer analyzer = new BinaryCompatibilityAnalyzer(options);
		ReportSection section = analyzer.analyze(classpath);

		// assert
		ReportTable table = assertSectionHeader(section);

		List<String[]> rows = table.getRows();
		assertEquals(1, rows.size());
		assertValuesEquals(rows.get(0), "a.jar", joinLines("a.C", "\u2022 Class is not a permitted subclass of sealed superclass: sealed class a.A"));
	}

	@Test
	void test_analyze_permittedSubclassIsNotAccessible() {

		// prepare
		Classpath classpath = ClasspathBuilder.create(javaRuntime)
				.addJarFile("a.jar").addModuleInfo(ModuleInfo.forModuleName("a"))
				.addClassDef(ClassDef.forClassName("a.A").withAccess(Modifier.PUBLIC).addPermittedSubclassName("b.B"))
				.addClassDef(ClassDef.forClassName("b.B").setSuperName("a.A"))
				.build();

		// test
		BinaryCompatibilityAnalyzer analyzer = new BinaryCompatibilityAnalyzer(options);
		ReportSection section = analyzer.analyze(classpath);

		// assert
		ReportTable table = assertSectionHeader(section);

		List<String[]> rows = table.getRows();
		assertEquals(1, rows.size());
		assertValuesEquals(rows.get(0), "a.jar", joinLines("a.A", "\u2022 Permitted subclass is not accessible: class b.B"));
	}

	@Test
	void test_analyze_permittedSubclassIsInUnnamedModule() {

		// prepare
		Classpath classpath = ClasspathBuilder.create(javaRuntime)
				.addJarFile("a.jar").addModuleInfo(ModuleInfo.forModuleName("a"))
				.addClassDef(ClassDef.forClassName("a.A").withAccess(Modifier.PUBLIC).addPermittedSubclassName("b.B"))
				.addJarFile("b.jar")
				.addClassDef(ClassDef.forClassName("b.B").withAccess(Modifier.PUBLIC).setSuperName("a.A"))
				.build();

		// test
		BinaryCompatibilityAnalyzer analyzer = new BinaryCompatibilityAnalyzer(options);
		ReportSection section = analyzer.analyze(classpath);

		// assert
		ReportTable table = assertSectionHeader(section);

		List<String[]> rows = table.getRows();
		assertEquals(2, rows.size());
		assertValuesEquals(rows.get(0), "a.jar", joinLines("a.A", "\u2022 Permitted subclass is in unnamed module: public class b.B"));
		assertValuesEquals(rows.get(1), "b.jar", joinLines("b.B", "\u2022 Sealed superclass is in a named module: public sealed class a.A"));
	}

	@Test
	void test_analyze_permittedSubclassIsInANamedModule() {

		// prepare
		Classpath classpath = ClasspathBuilder.create(javaRuntime)
				.addJarFile("a.jar")
				.addClassDef(ClassDef.forClassName("a.A").withAccess(Modifier.PUBLIC).addPermittedSubclassName("b.B"))
				.addJarFile("b.jar").addModuleInfo(ModuleInfo.forModuleName("b"))
				.addClassDef(ClassDef.forClassName("b.B").withAccess(Modifier.PUBLIC).setSuperName("a.A"))
				.build();

		// test
		BinaryCompatibilityAnalyzer analyzer = new BinaryCompatibilityAnalyzer(options);
		ReportSection section = analyzer.analyze(classpath);

		// assert
		ReportTable table = assertSectionHeader(section);

		List<String[]> rows = table.getRows();
		assertEquals(2, rows.size());
		assertValuesEquals(rows.get(0), "a.jar", joinLines("a.A", "\u2022 Permitted subclass is in a named module: public class b.B"));
		assertValuesEquals(rows.get(1), "b.jar", joinLines("b.B", "\u2022 Sealed superclass is in unnamed module: public sealed class a.A"));
	}

	@Test
	void test_analyze_superclassIsARecordClass() {

		// prepare
		Classpath classpath = ClasspathBuilder.create(javaRuntime)
				.addJarFile("a.jar")
				.addClassDef(ClassDef.forClassName("a.A").withAccess(Opcodes.ACC_RECORD))
				.addClassDef(ClassDef.forClassName("a.B").setSuperName("a.A"))
				.build();

		// test
		BinaryCompatibilityAnalyzer analyzer = new BinaryCompatibilityAnalyzer(options);
		ReportSection section = analyzer.analyze(classpath);

		// assert
		ReportTable table = assertSectionHeader(section);

		List<String[]> rows = table.getRows();
		assertEquals(1, rows.size());
		assertValuesEquals(rows.get(0), "a.jar", joinLines("a.B", "\u2022 Superclass is a record class: record a.A"));
	}

	@Test
	void test_analyze_ignoreMissingAnnotations_false() {

		// prepare
		Classpath classpath = ClasspathBuilder.create(javaRuntime)
				.addJarFile("a.jar")
				.addClassDef(ClassDef.forClassName("a.A").addAnnotationRef(new AnnotationRef("b.B", AnnotationRef.Target.TYPE)))
				.build();

		// test
		options.setIgnoreMissingAnnotations(false);
		BinaryCompatibilityAnalyzer analyzer = new BinaryCompatibilityAnalyzer(options);
		ReportSection section = analyzer.analyze(classpath);

		// assert
		ReportTable table = assertSectionHeader(section);

		List<String[]> rows = table.getRows();
		assertEquals(1, rows.size());
		assertValuesEquals(rows.get(0), "a.jar", joinLines("a.A", "\u2022 Annotation not found: b.B (package not found)"));
	}

	@Test
	void test_analyze_ignoreMissingAnnotations_true() {

		// prepare
		Classpath classpath = ClasspathBuilder.create(javaRuntime)
				.addJarFile("a.jar")
				.addClassDef(ClassDef.forClassName("a.A").addAnnotationRef(new AnnotationRef("b.B", AnnotationRef.Target.TYPE)))
				.build();

		// test
		options.setIgnoreMissingAnnotations(true);
		BinaryCompatibilityAnalyzer analyzer = new BinaryCompatibilityAnalyzer(options);
		ReportSection section = analyzer.analyze(classpath);

		// assert
		ReportTable table = assertSectionHeader(section);

		List<String[]> rows = table.getRows();
		assertEquals(0, rows.size());
	}

	private ReportTable assertSectionHeader(ReportSection section) {

		assertNotNull(section);
		assertEquals("Binary Compatibility", section.getTitle());
		assertEquals("Compatibility issues between JAR files.", section.getDescription());
		assertEquals(1, section.getContent().size());
		assertTrue(section.getContent().get(0) instanceof ReportTable);

		ReportTable table = (ReportTable) section.getContent().get(0);

		String[] columns = table.getColumns();
		assertValuesEquals(columns, "JAR file", "Issues");

		return table;
	}

}
