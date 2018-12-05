package org.jarcheck.analyzer;

import org.jarcheck.model.Classpath;
import org.jarcheck.report.ReportSection;
import org.jarcheck.report.ReportTable;
import org.jarcheck.test.ClasspathBuilder;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JarDependenciesAnalyzerTest {

	@Test
	void test_analyze() {

		// prepare
		Classpath classpath = ClasspathBuilder.create()
				.addJarFile("a.jar")
				.addClassDef("a/A").addClassRef("b/B1").addClassRef("c/C").addClassRef("x/X")
				.addJarFile("b.jar")
				.addClassDef("b/B1")
				.addClassDef("b/B2").addClassRef("c/C").addClassRef("b/B1")
				.addJarFile("c.jar")
				.addClassDef("c/C")
				.addJarFile("d.jar")
				.build();

		// test
		JarDependenciesAnalyzer analyzer = new JarDependenciesAnalyzer();
		ReportSection section = analyzer.analyze(classpath);

		// assert
		assertNotNull(section);
		assertEquals("JAR File Dependencies", section.getTitle());
		assertEquals("Dependencies between JAR files.", section.getDescription());
		assertEquals(1, section.getContent().size());
		assertTrue(section.getContent().get(0) instanceof ReportTable);

		ReportTable table = (ReportTable) section.getContent().get(0);

		String[] columns = table.getColumns();
		assertEquals(3, columns.length);
		assertEquals("JAR file", columns[0]);
		assertEquals("Uses", columns[1]);
		assertEquals("Used by", columns[2]);

		List<String[]> rows = table.getRows();
		assertEquals(4, rows.size());

		String[] values1 = rows.get(0);
		assertEquals(3, values1.length);
		assertEquals("a.jar", values1[0]);
		assertEquals("b.jar" + System.lineSeparator() + "c.jar", values1[1]);
		assertEquals("[none]", values1[2]);

		String[] values2 = rows.get(1);
		assertEquals(3, values2.length);
		assertEquals("b.jar", values2[0]);
		assertEquals("c.jar", values2[1]);
		assertEquals("a.jar", values2[2]);

		String[] values3 = rows.get(2);
		assertEquals(3, values3.length);
		assertEquals("c.jar", values3[0]);
		assertEquals("[none]", values3[1]);
		assertEquals("a.jar" + System.lineSeparator() + "b.jar", values3[2]);

		String[] values4 = rows.get(3);
		assertEquals(3, values4.length);
		assertEquals("d.jar", values4[0]);
		assertEquals("[none]", values4[1]);
		assertEquals("[none]", values4[2]);

	}

}
