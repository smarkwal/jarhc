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

class OsgiBundlesAnalyzerTest {

	@Test
	void test_analyze() {

		// prepare
		Classpath classpath = ClasspathBuilder.create(null)
				.addJarFile("a.jar")
				.addJarFile("b.jar")
				.addManifestAttribute("Bundle-Name", "bundle-name-b")
				.addManifestAttribute("Bundle-SymbolicName", "bundle-symbolic-name-b")
				.addManifestAttribute("Bundle-Version", "1.2.3")
				.addManifestAttribute("Bundle-Description", "bundle-description-b")
				.addManifestAttribute("Bundle-Vendor", "bundle-vendor-b")
				.addManifestAttribute("Bundle-License", "bundle-license-b")
				.addManifestAttribute("Bundle-DocURL", "bundle-doc-url-b")
				.addManifestAttribute("Import-Package", "a,java.base")
				.addManifestAttribute("DynamicImport-Package", "dynamic-package")
				.addManifestAttribute("Export-Package", "b.x,b.y")
				.addManifestAttribute("Require-Capability", "capability-1")
				.addManifestAttribute("Provide-Capability", "capability-2")
				.addManifestAttribute("Bundle-Activator", "bundle-activator")
				.addManifestAttribute("Bundle-ActivationPolicy", "lazy")
				.addManifestAttribute("Private-Package", "b.p,b.q")
				.addManifestAttribute("Include-Resource", "resource-b1,resource-b-2")
				.addManifestAttribute("Bundle-RequiredExecutionEnvironment", "JRE-1.8")
				.addManifestAttribute("Bundle-ManifestVersion", "2")
				.build();

		// test
		OsgiBundlesAnalyzer analyzer = new OsgiBundlesAnalyzer();
		ReportSection section = analyzer.analyze(classpath);

		// assert
		assertNotNull(section);
		assertEquals("OSGi Bundles", section.getTitle());
		assertEquals("Information about OSGi Bundles.", section.getDescription());
		assertEquals(1, section.getContent().size());
		assertInstanceOf(ReportTable.class, section.getContent().get(0));

		ReportTable table = (ReportTable) section.getContent().get(0);

		String[] columns = table.getColumns();
		assertValuesEquals(columns, "Artifact", "Name", "Version", "Description", "Import Package", "Export Package", "Capabilities", "Others");

		List<String[]> rows = table.getRows();
		assertEquals(1, rows.size());
		assertValuesEquals(rows.get(0),
				"b",
				joinLines("bundle-name-b", "[bundle-symbolic-name-b]"),
				"1.2.3",
				joinLines("bundle-description-b", "Vendor: bundle-vendor-b", "License: bundle-license-b", "Doc URL: bundle-doc-url-b"),
				joinLines("a", "java.base", "", "Dynamic:", "dynamic-package"),
				joinLines("b.x", "b.y"),
				joinLines("Required: capability-1", "Provided: capability-2"),
				joinLines("Activator: bundle-activator", "Activation Policy: lazy", "Manifest Version: 2", "Private Package: b.p,b.q", "Include Resource: resource-b1,resource-b-2", "Required Execution Environment: JRE-1.8")
		);
	}

}
