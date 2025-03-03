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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.jarhc.model.Classpath;
import org.jarhc.model.OSGiBundleInfo;
import org.jarhc.report.ReportSection;
import org.jarhc.report.ReportTable;
import org.jarhc.test.ClasspathBuilder;
import org.junit.jupiter.api.Test;

class OSGiBundlesAnalyzerTest {

	@Test
	void test_analyze() {

		// prepare
		Map<String, String> bundleHeaders = new LinkedHashMap<>();
		bundleHeaders.put("Bundle-Name", "bundle-name-b");
		bundleHeaders.put("Bundle-SymbolicName", "bundle-symbolic-name-b");
		bundleHeaders.put("Bundle-Version", "1.2.3");
		bundleHeaders.put("Bundle-Description", "bundle-description-b");
		bundleHeaders.put("Bundle-Vendor", "bundle-vendor-b");
		bundleHeaders.put("Bundle-License", "bundle-license-b");
		bundleHeaders.put("Bundle-DocURL", "bundle-doc-url-b");
		bundleHeaders.put("Import-Package", "a,java.base");
		bundleHeaders.put("DynamicImport-Package", "dynamic-package");
		bundleHeaders.put("Export-Package", "b.x,b.y");
		bundleHeaders.put("Require-Capability", "capability-1");
		bundleHeaders.put("Provide-Capability", "capability-2");
		bundleHeaders.put("Bundle-Activator", "bundle-activator");
		bundleHeaders.put("Bundle-ActivationPolicy", "lazy");
		bundleHeaders.put("Private-Package", "b.p,b.q");
		bundleHeaders.put("Include-Resource", "resource-b1,resource-b-2");
		bundleHeaders.put("Bundle-RequiredExecutionEnvironment", "JRE-1.8");
		bundleHeaders.put("Bundle-ManifestVersion", "2");
		Classpath classpath = ClasspathBuilder.create(null)
				.addJarFile("a.jar")
				.addJarFile("b.jar")
				.addOSGiBundleInfo(new OSGiBundleInfo(bundleHeaders))
				.build();

		// test
		OSGiBundlesAnalyzer analyzer = new OSGiBundlesAnalyzer();
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
				joinLines("bundle-name-b", "Symbolic Name: `bundle-symbolic-name-b`"),
				"1.2.3",
				joinLines("bundle-description-b", "Vendor: bundle-vendor-b", "License: bundle-license-b", "Doc URL: bundle-doc-url-b"),
				joinLines("`a`", "`java.base`", "", "Dynamic:", "`dynamic-package`"),
				joinLines("`b.x`", "`b.y`"),
				joinLines("Required:", "`capability-1`", "Provided:", "`capability-2`"),
				joinLines("Activator: bundle-activator", "Activation Policy: lazy", "Manifest Version: 2", "Private Package:", "b.p", "b.q", "Include Resource:", "resource-b1", "resource-b-2", "Required Execution Environment: JRE-1.8")
		);
	}

}
