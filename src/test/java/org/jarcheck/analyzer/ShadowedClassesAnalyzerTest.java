package org.jarcheck.analyzer;

import org.jarcheck.model.Classpath;
import org.jarcheck.report.ReportSection;
import org.jarcheck.report.ReportTable;
import org.jarcheck.test.ClasspathBuilder;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ShadowedClassesAnalyzerTest {

	@Test
	void test_analyze() {

		// prepare
		Classpath classpath = ClasspathBuilder.create()
				.addJarFile("a.jar").addClassDef("a/A").addClassDef("java/lang/String")
				.addJarFile("b.jar").addClassDef("b/B")
				.build();

		// test
		ShadowedClassesAnalyzer analyzer = new ShadowedClassesAnalyzer();
		ReportSection section = analyzer.analyze(classpath);

		// assert
		assertNotNull(section);
		assertEquals("Shadowed Classes", section.getTitle());
		assertTrue(section.getDescription().startsWith("Classes shadowing JRE/JDK classes."));
		assertEquals(1, section.getContent().size());
		assertTrue(section.getContent().get(0) instanceof ReportTable);

		ReportTable table = (ReportTable) section.getContent().get(0);

		String[] columns = table.getColumns();
		assertEquals(2, columns.length);
		assertEquals("Class name", columns[0]);
		assertEquals("ClassLoader", columns[1]);

		List<String[]> rows = table.getRows();
		assertEquals(1, rows.size());
		String[] values = rows.get(0);
		assertEquals(2, values.length);
		assertEquals("java.lang.String", values[0]);
		assertEquals("Bootstrap", values[1]);

	}

}
