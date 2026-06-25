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

import static org.jarhc.utils.Markdown.code;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jarhc.model.Classpath;
import org.jarhc.model.JarFile;
import org.jarhc.model.ModuleInfo;
import org.jarhc.report.ReportSection;
import org.jarhc.report.ReportTable;
import org.jarhc.utils.Markdown;

public class JpmsModulesAnalyzer implements Analyzer {

	/**
	 * Pattern matching a hyphen followed by digits that are then followed by a
	 * dot or the end of the string. This marks the start of the version part
	 * when deriving an automatic module name from a JAR file name
	 * (see {@link java.lang.module.ModuleFinder#of(java.nio.file.Path...)}).
	 */
	private static final Pattern DASH_VERSION = Pattern.compile("-(\\d+(\\.|$))");

	@Override
	public ReportSection analyze(Classpath classpath) {

		ReportTable table = buildTable(classpath);

		ReportSection section = new ReportSection("JPMS Modules", "List of Java Modules found in classpath.");
		section.addTable(table);
		return section;
	}

	private ReportTable buildTable(Classpath classpath) {

		ReportTable table = new ReportTable("Artifact", "Module name", "Definition", "Automatic", "Requires", "Exports");
		// TODO: add column with opens and internal packages?

		// TODO: check module dependencies?

		// for every JAR file ...
		List<JarFile> jarFiles = classpath.getJarFiles();
		for (JarFile jarFile : jarFiles) {

			String displayName = jarFile.getDisplayName();
			String moduleName = getModuleName(jarFile);
			String definitionInfo = getDefinitionInfo(jarFile);
			String automaticInfo = getAutomaticInfo(jarFile);
			String requiresInfo = getRequiresInfo(jarFile);
			String exportsInfo = getExportsInfo(jarFile);

			table.addRow(displayName, code(moduleName), definitionInfo, automaticInfo, requiresInfo, exportsInfo);
		}

		return table;
	}

	private String getModuleName(JarFile jarFile) {
		ModuleInfo moduleInfo = jarFile.getModuleInfo();
		if (moduleInfo.isNamed()) {
			return moduleInfo.getModuleName();
		} else {
			return getModuleNameFromFileName(jarFile);
		}
	}

	/**
	 * Derives the automatic module name for the given JAR file from its file name.
	 * <p>
	 * This implements the same algorithm the Java Platform Module System uses when
	 * deriving an automatic module name for a JAR file on the module path
	 * (see {@link java.lang.module.ModuleFinder#of(java.nio.file.Path...)}):
	 * <ol>
	 *     <li>drop the {@code .jar} file extension,</li>
	 *     <li>find the first hyphen that is followed by a digit (and then a dot
	 *     or the end of the string), and discard everything from that hyphen
	 *     onwards (this is the version part),</li>
	 *     <li>replace every non-alphanumeric character with a dot,</li>
	 *     <li>collapse repeated dots, and strip leading and trailing dots.</li>
	 * </ol>
	 * Unlike the artifact name derivation (see
	 * {@link org.jarhc.artifacts.Artifact#fromFileName(String)}), the original
	 * case is preserved.
	 *
	 * @param jarFile JAR file
	 * @return Derived automatic module name
	 */
	private String getModuleNameFromFileName(JarFile jarFile) {

		// start with file name
		String moduleName = jarFile.getFileName();

		// remove ".jar" file extension
		if (moduleName.endsWith(".jar")) {
			moduleName = moduleName.substring(0, moduleName.length() - 4);
		}

		// remove version number: discard everything from the first hyphen
		// that is followed by a digit (and then a dot or the end of the string)
		Matcher matcher = DASH_VERSION.matcher(moduleName);
		if (matcher.find()) {
			moduleName = moduleName.substring(0, matcher.start());
		}

		// replace all non-alphanumeric characters with dots
		moduleName = moduleName.replaceAll("[^A-Za-z0-9]", ".");

		// collapse repeated dots
		moduleName = moduleName.replaceAll("\\.{2,}", ".");

		// strip leading and trailing dots
		moduleName = moduleName.replaceAll("^\\.|\\.$", "");

		return moduleName;
	}

	private String getDefinitionInfo(JarFile jarFile) {
		ModuleInfo moduleInfo = jarFile.getModuleInfo();
		if (moduleInfo.isNamed()) {
			if (moduleInfo.isAutomatic()) {
				return "Manifest";
			} else {
				return "Module-Info";
			}
		} else {
			return "Auto-generated";
		}
	}

	private String getAutomaticInfo(JarFile jarFile) {
		ModuleInfo moduleInfo = jarFile.getModuleInfo();
		if (moduleInfo.isNamed()) {
			if (moduleInfo.isAutomatic()) {
				return "Yes";
			} else {
				return "No";
			}
		} else {
			return "Yes";
		}
	}

	private String getRequiresInfo(JarFile jarFile) {
		ModuleInfo moduleInfo = jarFile.getModuleInfo();
		if (moduleInfo.isNamed()) {
			if (moduleInfo.isAutomatic()) {
				return "";
			} else {
				List<String> requires = new ArrayList<>(moduleInfo.getRequires());
				requires.sort(JpmsModulesAnalyzer::compareModuleNames);
				requires.replaceAll(Markdown::code);
				return String.join("\n", requires);
			}
		} else {
			return "";
		}
	}

	private String getExportsInfo(JarFile jarFile) {
		ModuleInfo moduleInfo = jarFile.getModuleInfo();
		if (moduleInfo.isNamed()) {
			if (moduleInfo.isAutomatic()) {
				return "[all packages]";
			} else {
				List<String> exports = new ArrayList<>(moduleInfo.getExports());
				exports.sort(String.CASE_INSENSITIVE_ORDER);
				exports.replaceAll(Markdown::code);
				return String.join("\n", exports);
			}
		} else {
			return "[all packages]";
		}
	}

	/**
	 * Order modules by group and name:
	 * 1. Custom modules
	 * 2. Java modules except java.base
	 * 3. java.base
	 * 4. JDK-internal modules
	 */
	private static int compareModuleNames(String name1, String name2) {
		int group1 = getModuleGroup(name1);
		int group2 = getModuleGroup(name2);
		if (group1 != group2) return group1 - group2;
		return name1.compareTo(name2);
	}

	private static int getModuleGroup(String name) {
		if (name.startsWith("jdk.")) return 3;
		if (name.equals("java.base")) return 2;
		if (name.startsWith("java.")) return 1;
		return 0;
	}

}
