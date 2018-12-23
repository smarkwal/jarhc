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

import org.jarhc.model.ClassDef;
import org.jarhc.model.Classpath;
import org.jarhc.model.JarFile;
import org.jarhc.report.ReportSection;
import org.jarhc.report.ReportTable;
import org.jarhc.utils.JavaUtils;
import org.jarhc.utils.MultiMap;
import org.jarhc.utils.StringUtils;

import java.util.List;
import java.util.Set;

public class PackagesAnalyzer extends Analyzer {

	@Override
	public ReportSection analyze(Classpath classpath) {

		ReportTable table = buildTable(classpath);

		ReportSection section = new ReportSection("Packages", "List of packages per JAR file.");
		section.add(table);
		return section;
	}

	private ReportTable buildTable(Classpath classpath) {

		// map from JAR file name to package Names
		MultiMap<String, String> map = new MultiMap<>();

		// for every JAR file ...
		List<JarFile> jarFiles = classpath.getJarFiles();
		for (JarFile jarFile : jarFiles) {

			String fileName = jarFile.getFileName();

			// for every class definition ...
			List<ClassDef> classDefs = jarFile.getClassDefs();
			for (ClassDef classDef : classDefs) {

				String className = classDef.getClassName();
				if (className.equals("module-info")) continue;

				// get package name from class name
				String packageName = JavaUtils.getPackageName(className);

				// remember JAR files for package name
				map.add(fileName, packageName);
			}
		}

		ReportTable table = new ReportTable("JAR file", "Packages");

		// for every JAR file ...
		for (String fileName : map.getKeys()) {
			Set<String> packageNames = map.getValues(fileName);
			// if package has been found in more than one JAR file ...
			if (packageNames.size() > 0) {
				table.addRow(fileName, StringUtils.joinLines(packageNames));
			}
		}

		return table;
	}

}
