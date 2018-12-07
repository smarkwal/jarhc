package org.jarcheck.analyzer;

import org.jarcheck.model.Classpath;
import org.jarcheck.report.ReportSection;
import org.jarcheck.report.ReportTable;
import org.jarcheck.test.ClasspathBuilder;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PackagesAnalyzerTest {

	@Test
	void test_analyze() {

		// prepare
		Classpath classpath = ClasspathBuilder.create()
				.addJarFile("a.jar")
				.addClassDef("a/A")
				.addClassDef("a/a1/A")
				.addClassDef("a/a2/A1")
				.addClassDef("a/a2/A2")
				.addJarFile("b.jar")
				.addClassDef("b/B")
				.addJarFile("c.jar")
				.build();

		// test
		PackagesAnalyzer analyzer = new PackagesAnalyzer();
		ReportSection section = analyzer.analyze(classpath);

		// assert
		assertNotNull(section);
		assertEquals("Packages", section.getTitle());
		assertEquals("List of packages per JAR file.", section.getDescription());
		assertEquals(1, section.getContent().size());
		assertTrue(section.getContent().get(0) instanceof ReportTable);

		ReportTable table = (ReportTable) section.getContent().get(0);

		String[] columns = table.getColumns();
		assertEquals(2, columns.length);
		assertEquals("JAR file", columns[0]);
		assertEquals("Packages", columns[1]);

		List<String[]> rows = table.getRows();
		assertEquals(2, rows.size());

		String[] values1 = rows.get(0);
		assertEquals(2, values1.length);
		assertEquals("a.jar", values1[0]);
		assertEquals("a" + System.lineSeparator() + "a.a1" + System.lineSeparator() + "a.a2", values1[1]);

		String[] values2 = rows.get(1);
		assertEquals(2, values2.length);
		assertEquals("b.jar", values2[0]);
		assertEquals("b", values2[1]);

	}

}
