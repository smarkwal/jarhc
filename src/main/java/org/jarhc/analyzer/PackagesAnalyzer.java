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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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

		ReportTable table = new ReportTable("JAR file", "Count", "Packages");

		// for every JAR file ...
		for (String fileName : map.getKeys()) {
			Set<String> packageNames = map.getValues(fileName);
			List<String> packageGroups = getPackageGroups(map, fileName, packageNames);
			table.addRow(fileName, String.valueOf(packageNames.size()), StringUtils.joinLines(packageGroups));
		}

		return table;
	}

	private List<String> getPackageGroups(MultiMap<String, String> map, String fileName, Set<String> packageNames) {

		Map<String, PackageGroup> packageGroups = new LinkedHashMap<>();

		for (String packageName : packageNames) {

			PackageIdentifier packageIdentifier = new PackageIdentifier(packageName);

			int maxCommonLength = 0;
			for (String fileName2 : map.getKeys()) {
				if (fileName.equals(fileName2)) continue;
				for (String packageName2 : map.getValues(fileName2)) {
					PackageIdentifier packageIdentifier2 = new PackageIdentifier(packageName2);
					int commonLength = PackageIdentifier.getCommonLength(packageIdentifier, packageIdentifier2);
					if (commonLength > maxCommonLength) maxCommonLength = commonLength;
				}
			}

			boolean isSubPackage = false;
			if (maxCommonLength < packageIdentifier.getLength() - 1) {
				isSubPackage = true;
				packageName = packageIdentifier.getParentPackage(maxCommonLength + 1).toString();
			}

			PackageGroup packageGroup = packageGroups.get(packageName);
			if (packageGroup == null) {
				packageGroup = new PackageGroup(packageName, isSubPackage);
				packageGroups.put(packageName, packageGroup);
			} else {
				if (isSubPackage) {
					packageGroup.addSubPackage();
				}
			}

		}

		return packageGroups.values().stream().map(PackageGroup::toString).collect(Collectors.toList());
	}

	private static class PackageGroup {

		private final String name;
		private boolean exists = false;
		private int subPackages = 0;

		public PackageGroup(String name, boolean subPackage) {
			this.name = name;
			if (subPackage) {
				subPackages = 1;
			} else {
				exists = true;
			}
		}

		public void addSubPackage() {
			subPackages++;
		}

		@Override
		public String toString() {
			if (exists) {
				if (subPackages == 0) {
					return name;
				} else if (subPackages == 1) {
					return name + " (+ 1 subpackage)";
				} else {
					return name + " (+ " + subPackages + " subpackages)";
				}
			} else {
				if (subPackages == 0) {
					return name + ".*";
				} else if (subPackages == 1) {
					return name + ".* (1 subpackage)";
				} else {
					return name + ".* (" + subPackages + " subpackages)";
				}
			}
		}

	}

	private static class PackageIdentifier {

		private final String[] parts;

		public PackageIdentifier(String name) {
			parts = name.split("\\.");
		}

		private PackageIdentifier(String[] parts, int len) {
			this.parts = new String[len];
			System.arraycopy(parts, 0, this.parts, 0, len);
		}

		public int getLength() {
			return parts.length;
		}

		public PackageIdentifier getParentPackage(int len) {
			return new PackageIdentifier(parts, len);
		}

		public static int getCommonLength(PackageIdentifier name1, PackageIdentifier name2) {
			String[] parts1 = name1.parts;
			String[] parts2 = name2.parts;
			int len = Math.min(parts1.length, parts2.length);
			for (int i = 0; i < len; i++) {
				String part1 = parts1[i];
				String part2 = parts2[i];
				if (!part1.equals(part2)) {
					return i;
				}
			}
			return len;
		}

		@Override
		public String toString() {
			return String.join(".", parts);
		}
	}

}
