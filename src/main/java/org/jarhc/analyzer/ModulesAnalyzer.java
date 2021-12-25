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

import java.util.List;
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

		ReportTable table = new ReportTable("JAR file", "Module name", "Automatic", "Requires", "Exports");
		// TODO: add column with opens and internal packages?

		// TODO: check module dependencies?

		// for every JAR file ...
		List<JarFile> jarFiles = classpath.getJarFiles();
		for (JarFile jarFile : jarFiles) {

			String fileName = jarFile.getFileName();
			String moduleName = getModuleName(jarFile);
			String automaticInfo = getAutomaticInfo(jarFile);
			String requiresInfo = getRequiresInfo(jarFile);
			String exportsInfo = getExportsInfo(jarFile);

			table.addRow(fileName, moduleName, automaticInfo, requiresInfo, exportsInfo);
		}

		return table;
	}

	private String getModuleName(JarFile jarFile) {
		if (jarFile.isModule()) {
			ModuleInfo moduleInfo = jarFile.getModuleInfo();
			return moduleInfo.getModuleName();
		} else {
			return getModuleNameFromFileName(jarFile) + " (auto-generated)";
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

		return moduleName;
	}

	private String getAutomaticInfo(JarFile jarFile) {
		if (jarFile.isModule()) {
			ModuleInfo moduleInfo = jarFile.getModuleInfo();
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
		if (jarFile.isModule()) {
			ModuleInfo moduleInfo = jarFile.getModuleInfo();
			if (moduleInfo.isAutomatic()) {
				return "-";
			} else {
				return String.join("\n", moduleInfo.getRequires());
			}
		} else {
			return "-";
		}
	}

	private String getExportsInfo(JarFile jarFile) {
		if (jarFile.isModule()) {
			ModuleInfo moduleInfo = jarFile.getModuleInfo();
			if (moduleInfo.isAutomatic()) {
				return "[all packages]";
			} else {
				return String.join("\n", moduleInfo.getExports());
			}
		} else {
			return "[all packages]";
		}
	}

}
