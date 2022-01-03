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
import java.util.Locale;
import org.jarhc.loader.JarFileNameNormalizer;
import org.jarhc.model.Classpath;
import org.jarhc.model.JarFile;
import org.jarhc.model.ModuleInfo;
import org.jarhc.report.ReportSection;
import org.jarhc.report.ReportTable;

public class ModulesAnalyzer implements Analyzer {

	@Override
	public ReportSection analyze(Classpath classpath) {

		ReportTable table = buildTable(classpath);

		ReportSection section = new ReportSection("Modules", "List of Java Modules found in classpath.");
		section.add(table);
		return section;
	}

	private ReportTable buildTable(Classpath classpath) {

		ReportTable table = new ReportTable("JAR file", "Module name", "Definition", "Automatic", "Requires", "Exports");
		// TODO: add column with opens and internal packages?

		// TODO: check module dependencies?

		// for every JAR file ...
		List<JarFile> jarFiles = classpath.getJarFiles();
		for (JarFile jarFile : jarFiles) {

			String fileName = jarFile.getFileName();
			String moduleName = getModuleName(jarFile);
			String definitionInfo = getDefinitionInfo(jarFile);
			String automaticInfo = getAutomaticInfo(jarFile);
			String requiresInfo = getRequiresInfo(jarFile);
			String exportsInfo = getExportsInfo(jarFile);

			table.addRow(fileName, moduleName, definitionInfo, automaticInfo, requiresInfo, exportsInfo);
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

	private String getModuleNameFromFileName(JarFile jarFile) {

		// start with file name
		String moduleName = jarFile.getFileName();

		// remove ".jar" file extension
		if (moduleName.endsWith(".jar")) {
			moduleName = moduleName.substring(0, moduleName.length() - 4);
		}

		// remove version number
		moduleName = JarFileNameNormalizer.getFileNameWithoutVersionNumber(moduleName);

		// replace all dashes with dots
		moduleName = moduleName.replace("-", ".");

		// convert to lower-case
		moduleName = moduleName.toLowerCase(Locale.ROOT);

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
				return "-";
			} else {
				List<String> requires = new ArrayList<>(moduleInfo.getRequires());
				requires.sort(ModulesAnalyzer::compareModuleNames);
				return String.join("\n", requires);
			}
		} else {
			return "-";
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
