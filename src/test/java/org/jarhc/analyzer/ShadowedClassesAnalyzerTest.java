package org.jarhc.analyzer;

import org.jarhc.Main;
import org.jarhc.env.JavaRuntime;
import org.jarhc.model.Classpath;
import org.jarhc.report.ReportSection;
import org.jarhc.report.ReportTable;
import org.jarhc.test.ClasspathBuilder;
import org.jarhc.test.JavaRuntimeMock;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ShadowedClassesAnalyzerTest {

	@Test
	void test_analyze() {

		// prepare
		String mainClassName = Main.class.getName().replace(".", "/");
		Classpath classpath = ClasspathBuilder.create()
				.addJarFile("a.jar").addClassDef("a/A").addClassDef("java/lang/String")
				.addJarFile("b.jar").addClassDef("b/B").addClassDef(mainClassName)
				.build();

		// test
		JavaRuntime javaRuntime = new JavaRuntimeMock("/classes-oracle-jdk-1.8.0_144.txt");
		ShadowedClassesAnalyzer analyzer = new ShadowedClassesAnalyzer(javaRuntime);
		ReportSection section = analyzer.analyze(classpath);

		// assert
		assertNotNull(section);
		assertEquals("Shadowed Classes", section.getTitle());
		assertTrue(section.getDescription().startsWith("Classes shadowing JRE/JDK classes."));
		assertEquals(1, section.getContent().size());
		assertTrue(section.getContent().get(0) instanceof ReportTable);

		ReportTable table = (ReportTable) section.getContent().get(0);

		String[] columns = table.getColumns();
		assertEquals(3, columns.length);
		assertEquals("Class name", columns[0]);
		assertEquals("JAR file", columns[1]);
		assertEquals("ClassLoader", columns[2]);

		List<String[]> rows = table.getRows();
		assertEquals(1, rows.size());
		String[] values = rows.get(0);
		assertEquals(3, values.length);
		assertEquals("java.lang.String", values[0]);
		assertEquals("a.jar", values[1]);
		assertEquals("Bootstrap", values[2]);

	}

}
