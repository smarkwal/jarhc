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

import org.jarhc.Main;
import org.jarhc.env.JavaRuntime;
import org.jarhc.java.ClassLoader;
import org.jarhc.java.JavaRuntimeClassLoader;
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
		String mainClassName = Main.class.getName();
		Classpath classpath = ClasspathBuilder.create()
				.addJarFile("a.jar").addClassDef("a.A").addClassDef("java.lang.String")
				.addJarFile("b.jar").addClassDef("b.B").addClassDef(mainClassName)
				.build();

		// test
		JavaRuntime javaRuntime = JavaRuntimeMock.getOracleRuntime();
		ClassLoader parentClassLoader = new JavaRuntimeClassLoader(javaRuntime);
		ShadowedClassesAnalyzer analyzer = new ShadowedClassesAnalyzer(parentClassLoader);
		ReportSection section = analyzer.analyze(classpath);

		// assert
		assertNotNull(section);
		assertEquals("Shadowed Classes", section.getTitle());
		assertTrue(section.getDescription().startsWith("Classes shadowing JRE/JDK classes."));
		assertEquals(1, section.getContent().size());
		assertTrue(section.getContent().get(0) instanceof ReportTable);

		ReportTable table = (ReportTable) section.getContent().get(0);

		String[] columns = table.getColumns();
		assertEquals(4, columns.length);
		assertEquals("Class name", columns[0]);
		assertEquals("JAR file", columns[1]);
		assertEquals("Class loader", columns[2]);
		assertEquals("Similarity", columns[3]);

		List<String[]> rows = table.getRows();
		assertEquals(1, rows.size());
		String[] values = rows.get(0);
		assertEquals(4, values.length);
		assertEquals("java.lang.String", values[0]);
		assertEquals("a.jar", values[1]);
		assertEquals("Runtime (rt.jar)", values[2]);
		assertEquals("Different API", values[3]);

	}

}
