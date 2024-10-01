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
import org.jarhc.model.Classpath;
import org.jarhc.model.JarFile;
import org.jarhc.model.OSGiBundleInfo;
import org.jarhc.report.ReportSection;
import org.jarhc.report.ReportTable;
import org.jarhc.utils.StringUtils;

public class OSGiBundlesAnalyzer implements Analyzer {

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

			// ignore JAR files without OSGi Bundle information
			OSGiBundleInfo bundleInfo = jarFile.getOSGiBundleInfo();
			if (bundleInfo == null) {
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
		OSGiBundleInfo bundleInfo = jarFile.getOSGiBundleInfo();
		String name = bundleInfo.getBundleName();
		String symbolicName = bundleInfo.getBundleSymbolicName();

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
		OSGiBundleInfo bundleInfo = jarFile.getOSGiBundleInfo();
		return bundleInfo.getBundleVersion();
	}

	private String getDescription(JarFile jarFile) {
		OSGiBundleInfo bundleInfo = jarFile.getOSGiBundleInfo();
		String description = bundleInfo.getBundleDescription();
		String vendor = bundleInfo.getBundleVendor();
		String license = bundleInfo.getBundleLicense();
		String docUrl = bundleInfo.getBundleDocURL();

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
		wrapText(lines);
		return StringUtils.joinLines(lines);
	}

	private String getImportPackage(JarFile jarFile) {
		OSGiBundleInfo bundleInfo = jarFile.getOSGiBundleInfo();
		List<String> importPackage = bundleInfo.getImportPackage();
		List<String> dynamicImportPackage = bundleInfo.getDynamicImportPackage();

		List<String> lines = new ArrayList<>();
		if (importPackage != null) {
			lines.addAll(importPackage);
		}
		if (dynamicImportPackage != null) {
			if (importPackage != null) {
				lines.add("");
			}
			lines.add("Dynamic:");
			lines.addAll(dynamicImportPackage);
		}
		return StringUtils.joinLines(lines);
	}

	private String getExportPackage(JarFile jarFile) {
		OSGiBundleInfo bundleInfo = jarFile.getOSGiBundleInfo();
		List<String> exportPackage = bundleInfo.getExportPackage();

		List<String> lines = new ArrayList<>();
		if (exportPackage != null) {
			lines.addAll(exportPackage);
		}
		return StringUtils.joinLines(lines);
	}

	private String getCapabilities(JarFile jarFile) {
		OSGiBundleInfo bundleInfo = jarFile.getOSGiBundleInfo();
		List<String> requireCapability = bundleInfo.getRequireCapability();
		List<String> provideCapability = bundleInfo.getProvideCapability();

		List<String> lines = new ArrayList<>();
		if (requireCapability != null) {
			lines.add("Required:");
			lines.addAll(requireCapability);
		}
		if (provideCapability != null) {
			lines.add("Provided:");
			lines.addAll(provideCapability);
		}
		wrapText(lines);
		return StringUtils.joinLines(lines);
	}

	private String getOthers(JarFile jarFile) {
		OSGiBundleInfo bundleInfo = jarFile.getOSGiBundleInfo();
		String bundleActivator = bundleInfo.getBundleActivator();
		String bundleActivationPolicy = bundleInfo.getBundleActivationPolicy();
		String bundleManifestVersion = bundleInfo.getBundleManifestVersion();
		List<String> privatePackage = bundleInfo.getPrivatePackage();
		List<String> includeResource = bundleInfo.getIncludeResource();
		String requiredExecutionEnvironment = bundleInfo.getBundleRequiredExecutionEnvironment();

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
			lines.add("Private Package:");
			lines.addAll(privatePackage);
		}
		if (includeResource != null) {
			lines.add("Include Resource:");
			lines.addAll(includeResource);
		}
		if (requiredExecutionEnvironment != null) {
			lines.add("Required Execution Environment: " + requiredExecutionEnvironment);
		}
		wrapText(lines);
		return StringUtils.joinLines(lines);
	}

	private static String wrapText(String text) {
		return StringUtils.wrapText(text, 60);
	}

	private static void wrapText(List<String> lines) {
		lines.replaceAll(s -> wrapText(s));
	}

}
