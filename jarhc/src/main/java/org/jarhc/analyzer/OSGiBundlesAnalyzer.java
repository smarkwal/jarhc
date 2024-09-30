/*
 * Copyright 2021 Stephan Markwalder
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jarhc.model.Classpath;
import org.jarhc.model.JarFile;
import org.jarhc.report.ReportSection;
import org.jarhc.report.ReportTable;
import org.jarhc.utils.StringUtils;

public class OsgiBundlesAnalyzer implements Analyzer {

	// list of OSGI Bundle manifest attributes:
	// https://docs.osgi.org/reference/bundle-headers.html
	// https://docs.osgi.org/specification/osgi.core/8.0.0/framework.module.html#i2654895

	public static final Set<String> MANIFEST_ATTRIBUTES = Set.of(
			// Name
			"Bundle-Name",
			"Bundle-SymbolicName",

			// Version
			"Bundle-Version",

			// Description
			"Bundle-Description",
			"Bundle-Vendor",
			"Bundle-License",
			"Bundle-DocURL",

			// Import Package
			"Import-Package",
			"DynamicImport-Package",

			// Export Package
			"Export-Package",

			// Capabilities
			"Require-Capability",
			"Provide-Capability",

			// Others
			// TODO: re-distribute these attributes to more specific columns
			"Bundle-ActivationPolicy",
			"Bundle-Activator",
			"Bundle-Category",
			"Bundle-Classpath",
			"Bundle-ContactAddress",
			"Bundle-Copyright",
			"Bundle-Icon",
			"Bundle-Localization",
			"Bundle-ManifestVersion",
			"Bundle-NativeCode",
			"Bundle-RequiredExecutionEnvironment",
			"Bundle-UpdateLocation",
			"Export-Service",
			"Fragment-Host",
			"Ignore-Package",
			"Import-Bundle",
			"Import-Library",
			"Import-Service",
			"Include-Resource",
			"Module-Scope",
			"Module-Type",
			"Private-Package",
			"Require-Bundle",
			"Web-ContextPath",
			"Web-DispatcherServletUrlPatterns",
			"Web-FilterMappings"
	);

	@Override
	public ReportSection analyze(Classpath classpath) {

		ReportTable table = buildTable(classpath);

		ReportSection section = new ReportSection("OSGi Bundles", "Information about OSGi Bundles.");
		section.add(table);
		return section;
	}

	private ReportTable buildTable(Classpath classpath) {

		ReportTable table = new ReportTable("Artifact", "Name", "Version", "Description", "Import Package", "Export Package", "Capabilities", "Others");

		// for every JAR file ...
		List<JarFile> jarFiles = classpath.getJarFiles();
		for (JarFile jarFile : jarFiles) {

			// ignore JAR files without manifest
			Map<String, String> manifestAttributes = jarFile.getManifestAttributes();
			if (manifestAttributes == null) {
				continue;
			}

			// ignore JAR files without OSGI Bundle information
			String bundleName = manifestAttributes.get("Bundle-Name");
			if (bundleName == null) {
				continue;
			}

			String artifactName = jarFile.getArtifactName();
			String name = getName(jarFile);
			String version = getVersion(jarFile);
			String description = getDescription(jarFile);
			String importPackage = getImportPackage(jarFile);
			String exportPackage = getExportPackage(jarFile);
			String capabilities = getCapabilities(jarFile);
			String others = getOthers(jarFile);

			table.addRow(artifactName, name, version, description, importPackage, exportPackage, capabilities, others);
		}

		return table;
	}

	private String getName(JarFile jarFile) {
		Map<String, String> manifestAttributes = jarFile.getManifestAttributes();
		String name = manifestAttributes.get("Bundle-Name");
		String symbolicName = manifestAttributes.get("Bundle-SymbolicName");

		if (name != null && symbolicName != null) {
			if (name.equals(symbolicName)) {
				return name;
			} else {
				return name + System.lineSeparator() + "[" + symbolicName + "]";
			}
		} else if (name != null) {
			return name;
		} else if (symbolicName != null) {
			return symbolicName;
		} else {
			return null;
		}
	}

	private String getVersion(JarFile jarFile) {
		Map<String, String> manifestAttributes = jarFile.getManifestAttributes();
		return manifestAttributes.get("Bundle-Version");
	}

	private String getDescription(JarFile jarFile) {
		Map<String, String> manifestAttributes = jarFile.getManifestAttributes();
		String description = manifestAttributes.get("Bundle-Description");
		String vendor = manifestAttributes.get("Bundle-Vendor");
		String license = manifestAttributes.get("Bundle-License");
		String docUrl = manifestAttributes.get("Bundle-DocURL");

		List<String> lines = new ArrayList<>();
		if (description != null) {
			lines.add(description);
		}
		if (vendor != null) {
			lines.add("Vendor: " + vendor);
		}
		if (license != null) {
			lines.add("License: " + license);
		}
		if (docUrl != null) {
			lines.add("Doc URL: " + docUrl);
		}
		wrapLines(lines, 100); // TODO: wrap description by spaces instead of commas
		return StringUtils.joinLines(lines);
	}

	private String getImportPackage(JarFile jarFile) {
		Map<String, String> manifestAttributes = jarFile.getManifestAttributes();
		String importPackage = manifestAttributes.get("Import-Package");
		String dynamicImportPackage = manifestAttributes.get("DynamicImport-Package");

		List<String> lines = new ArrayList<>();
		if (importPackage != null) {
			// TODO: improve splitting to support version ranges:
			//  example: org.apache.log4j;version="e;[1.2.15,2.0.0)"e;;resolution:=optional
			// TODO: what does "e; mean?
			String[] packages = importPackage.split(",");
			lines.addAll(List.of(packages));
		}
		if (dynamicImportPackage != null) {
			if (importPackage != null) {
				lines.add("");
			}
			lines.add("Dynamic:");
			String[] packages = dynamicImportPackage.split(",");
			lines.addAll(List.of(packages));
		}
		return StringUtils.joinLines(lines);
	}

	private String getExportPackage(JarFile jarFile) {
		Map<String, String> manifestAttributes = jarFile.getManifestAttributes();
		String exportPackage = manifestAttributes.get("Export-Package");

		List<String> lines = new ArrayList<>();
		if (exportPackage != null) {
			String[] packages = exportPackage.split(",");
			lines.addAll(List.of(packages));
		}
		return StringUtils.joinLines(lines);
	}

	private String getCapabilities(JarFile jarFile) {
		Map<String, String> manifestAttributes = jarFile.getManifestAttributes();
		String requireCapability = manifestAttributes.get("Require-Capability");
		String provideCapability = manifestAttributes.get("Provide-Capability");

		List<String> lines = new ArrayList<>();
		if (requireCapability != null) {
			lines.add("Required: " + requireCapability);
		}
		if (provideCapability != null) {
			lines.add("Provided: " + provideCapability);
		}
		return StringUtils.joinLines(lines);
	}

	private String getOthers(JarFile jarFile) {
		Map<String, String> manifestAttributes = jarFile.getManifestAttributes();
		String bundleActivator = manifestAttributes.get("Bundle-Activator");
		String bundleActivationPolicy = manifestAttributes.get("Bundle-ActivationPolicy");
		String bundleManifestVersion = manifestAttributes.get("Bundle-ManifestVersion");
		String privatePackage = manifestAttributes.get("Private-Package");
		String includeResource = manifestAttributes.get("Include-Resource");
		String requiredExecutionEnvironment = manifestAttributes.get("Bundle-RequiredExecutionEnvironment");

		List<String> lines = new ArrayList<>();
		if (bundleActivator != null) {
			lines.add("Activator: " + bundleActivator);
		}
		if (bundleActivationPolicy != null) {
			lines.add("Activation Policy: " + bundleActivationPolicy);
		}
		if (bundleManifestVersion != null) {
			lines.add("Manifest Version: " + bundleManifestVersion);
		}
		if (privatePackage != null) {
			lines.add("Private Package: " + privatePackage);
		}
		if (includeResource != null) {
			lines.add("Include Resource: " + includeResource);
		}
		if (requiredExecutionEnvironment != null) {
			lines.add("Required Execution Environment: " + requiredExecutionEnvironment);
		}
		wrapLines(lines, 100);
		return StringUtils.joinLines(lines);
	}

	private static void wrapLines(List<String> lines, int maxLength) {
		lines.replaceAll(s -> StringUtils.wrapText(s, maxLength));
	}

}
