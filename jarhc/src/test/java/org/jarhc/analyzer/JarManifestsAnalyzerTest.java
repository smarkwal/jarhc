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
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;
import org.jarhc.model.Classpath;
import org.jarhc.report.ReportSection;
import org.jarhc.report.ReportTable;
import org.jarhc.test.ClasspathBuilder;
import org.junit.jupiter.api.Test;

class JarManifestsAnalyzerTest {

	@Test
	void test_analyze() {

		// prepare
		Classpath classpath = ClasspathBuilder.create(null)
				.addJarFile("a.jar")
				.addManifestAttribute("Manifest-Version", "1.0")
				.addJarFile("b.jar")
				.addManifestAttribute("Implementation-Title", "B Impl")
				.addManifestAttribute("Implementation-Version", "1.2.3")
				.addManifestAttribute("Implementation-Build", "1.2.3-b271")
				.addManifestAttribute("Implementation-Build-Id", "b271")
				.addManifestAttribute("Implementation-Vendor", "B Corp")
				.addManifestAttribute("Implementation-Vendor-Id", "b-corp")
				.addManifestAttribute("Implementation-URL", "https://b-corp.example.com")
				.addManifestAttribute("Specification-Title", "B Spec")
				.addManifestAttribute("Specification-Version", "1.0.0")
				.addManifestAttribute("Specification-Vendor", "C Corp R&D")
				.addManifestAttribute("Main-Class", "b.Main")
				.addManifestAttribute("Class-Path", "a.jar")
				.addManifestAttribute("Manifest-Version", "1.0")
				.addManifestAttribute("Created-By", "Apache Maven Bundle Plugin 5.1.8")
				.addManifestAttribute("Build-Jdk-Spec", "1.8")
				.addManifestAttribute("Build-Jdk", "1.8.0_202")
				.addManifestAttribute("X-Custom", "Hello World")
				.build();

		// test
		JarManifestsAnalyzer analyzer = new JarManifestsAnalyzer();
		ReportSection section = analyzer.analyze(classpath);

		// assert
		assertNotNull(section);
		assertEquals("JAR Manifests", section.getTitle());
		assertEquals("Information found in META-INF/MANIFEST.MF.", section.getDescription());
		assertEquals(1, section.getContent().size());
		assertInstanceOf(ReportTable.class, section.getContent().get(0));

		ReportTable table = (ReportTable) section.getContent().get(0);

		String[] columns = table.getColumns();
		assertValuesEquals(columns, "Artifact", "General", "Runtime", "Implementation", "Specification", "Signature");

		List<String[]> rows = table.getRows();
		assertEquals(2, rows.size());
		assertValuesEquals(rows.get(0), "a", "Manifest-Version: `1.0`", "", "", "", "");
		assertValuesEquals(rows.get(1),
				"b",
				joinLines("Manifest-Version: `1.0`", "Created-By: `Apache Maven Bundle Plugin 5.1.8`", "Build-Jdk-Spec: `1.8`", "Build-Jdk: `1.8.0_202`", "X-Custom: `Hello World`"),
				joinLines("Main Class: `b.Main`", "Class Path: `a.jar`"),
				joinLines("B Impl", "Version: `1.2.3`", "Build: `1.2.3-b271`", "Build ID: `b271`", "Vendor: `B Corp`", "Vendor ID: `b-corp`", "URL: `https://b-corp.example.com`"),
				joinLines("B Spec", "Version: `1.0.0`", "Vendor: `C Corp R&D`"),
				""
		);
	}

}
