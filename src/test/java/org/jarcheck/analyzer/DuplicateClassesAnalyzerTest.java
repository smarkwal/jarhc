package org.jarcheck.analyzer;

import org.jarcheck.model.Classpath;
import org.jarcheck.report.ReportSection;
import org.jarcheck.report.ReportTable;
import org.jarcheck.test.ClasspathBuilder;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DuplicateClassesAnalyzerTest {

	@Test
	void test_analyze() {

		// prepare
		Classpath classpath = ClasspathBuilder.create()
				.addJarFile("a-1.jar").addClassDef("a/b/C").addClassDef("a/b/X")
				.addJarFile("a-2.jar").addClassDef("a/b/C").addClassDef("a/b/Y")
				.addJarFile("b.jar").addClassDef("x/y/C")
				.build();

		// test
		DuplicateClassesAnalyzer analyzer = new DuplicateClassesAnalyzer();
		ReportSection section = analyzer.analyze(classpath);

		// assert
		assertNotNull(section);
		assertEquals("Duplicate Classes", section.getTitle());
		assertEquals("Classes found in multiple JAR files.", section.getDescription());
		assertEquals(1, section.getContent().size());
		assertTrue(section.getContent().get(0) instanceof ReportTable);

		ReportTable table = (ReportTable) section.getContent().get(0);

		String[] columns = table.getColumns();
		assertEquals(2, columns.length);
		assertEquals("Class name", columns[0]);
		assertEquals("JAR files", columns[1]);

		List<String[]> rows = table.getRows();
		assertEquals(1, rows.size());
		String[] values = rows.get(0);
		assertEquals(2, values.length);
		assertEquals("a.b.C", values[0]);
		assertEquals("a-1.jar" + System.lineSeparator() + "a-2.jar", values[1]);

	}

}
