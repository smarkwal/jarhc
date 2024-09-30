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
import org.jarhc.model.OSGiBundleInfo;
import org.jarhc.report.ReportSection;
import org.jarhc.report.ReportTable;
import org.jarhc.utils.StringUtils;

public class JarManifestsAnalyzer implements Analyzer {

	private static final Set<String> SPECIAL_ATTRIBUTE_NAMES = Set.of(

			// Runtime
			"Main-Class",
			"Class-Path",

			// Implementation
			"Implementation-Title",
			"Implementation-Version",
			"Implementation-Build",
			"Implementation-Build-Id",
			"Implementation-Vendor",
			"Implementation-Vendor-Id",
			"Implementation-URL",

			// Specification
			"Specification-Title",
			"Specification-Version",
			"Specification-Vendor"
	);

	@Override
	public ReportSection analyze(Classpath classpath) {

		ReportTable table = buildTable(classpath);

		ReportSection section = new ReportSection("JAR Manifests", "Information found in META-INF/MANIFEST.MF.");
		section.add(table);
		return section;
	}

	private ReportTable buildTable(Classpath classpath) {

		ReportTable table = new ReportTable("Artifact", "General", "Runtime", "Implementation", "Specification", "Signature");

		// for every JAR file ...
		List<JarFile> jarFiles = classpath.getJarFiles();
		for (JarFile jarFile : jarFiles) {

			// ignore JAR files without manifest
			Map<String, String> manifestAttributes = jarFile.getManifestAttributes();
			if (manifestAttributes == null) {
				continue;
			}

			String artifactName = jarFile.getArtifactName();
			String general = getGeneral(jarFile);
			String runtime = getRuntime(jarFile);
			String implementation = getImplementation(jarFile);
			String specification = getSpecification(jarFile);
			String signature = getSignature(jarFile);

			table.addRow(artifactName, general, runtime, implementation, specification, signature);
		}

		return table;
	}

	private String getGeneral(JarFile jarFile) {
		Map<String, String> manifestAttributes = jarFile.getManifestAttributes();
		List<String> lines = new ArrayList<>();
		for (String attributeName : manifestAttributes.keySet()) {
			if (SPECIAL_ATTRIBUTE_NAMES.contains(attributeName)) continue;
			if (OSGiBundleInfo.isBundleHeader(attributeName)) continue;
			String attributeValue = manifestAttributes.get(attributeName);
			lines.add(attributeName + ": " + attributeValue);
		}
		return StringUtils.joinLines(lines);
	}

	private String getRuntime(JarFile jarFile) {
		Map<String, String> manifestAttributes = jarFile.getManifestAttributes();
		String mainClass = manifestAttributes.get("Main-Class");
		String classPath = manifestAttributes.get("Class-Path");

		List<String> lines = new ArrayList<>();
		if (mainClass != null) {
			lines.add("Main Class: " + mainClass); // TODO: validate Main-Class entry
		}
		if (classPath != null) {
			lines.add("Class Path: " + classPath); // TODO: validate Class-Path entry
		}
		return StringUtils.joinLines(lines);
	}

	private String getImplementation(JarFile jarFile) {
		Map<String, String> manifestAttributes = jarFile.getManifestAttributes();
		String title = manifestAttributes.get("Implementation-Title");
		String version = manifestAttributes.get("Implementation-Version");
		String build = manifestAttributes.get("Implementation-Build");
		String buildId = manifestAttributes.get("Implementation-Build-Id");
		String vendor = manifestAttributes.get("Implementation-Vendor");
		String vendorId = manifestAttributes.get("Implementation-Vendor-Id");
		String url = manifestAttributes.get("Implementation-URL");

		List<String> lines = new ArrayList<>();
		if (title != null) {
			lines.add(title);
		}
		if (version != null) {
			lines.add("Version: " + version);
		}
		if (build != null) {
			lines.add("Build: " + build);
		}
		if (buildId != null) {
			lines.add("Build ID: " + buildId);
		}
		if (vendor != null) {
			lines.add("Vendor: " + vendor);
		}
		if (vendorId != null) {
			lines.add("Vendor ID: " + vendorId);
		}
		if (url != null) {
			lines.add("URL: " + url);
		}
		return StringUtils.joinLines(lines);
	}

	private String getSpecification(JarFile jarFile) {
		Map<String, String> manifestAttributes = jarFile.getManifestAttributes();
		String title = manifestAttributes.get("Specification-Title");
		String version = manifestAttributes.get("Specification-Version");
		String vendor = manifestAttributes.get("Specification-Vendor");

		List<String> lines = new ArrayList<>();
		if (title != null) {
			lines.add(title);
		}
		if (version != null) {
			lines.add("Version: " + version);
		}
		if (vendor != null) {
			lines.add("Vendor: " + vendor);
		}
		return StringUtils.joinLines(lines);
	}

	private String getSignature(JarFile jarFile) {
		// TODO: implement
		return "";
	}

}
