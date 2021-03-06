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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.jarhc.model.ClassDef;
import org.jarhc.model.Classpath;
import org.jarhc.model.JarFile;
import org.jarhc.report.ReportSection;
import org.jarhc.report.ReportTable;
import org.jarhc.utils.JavaUtils;
import org.jarhc.utils.MultiMap;
import org.jarhc.utils.StringUtils;


public class PackagesAnalyzer implements Analyzer {

	@Override
	public ReportSection analyze(Classpath classpath) {

		ReportTable table = buildTable(classpath);

		ReportSection section = new ReportSection("Packages", "List of packages per JAR file.");
		section.add(table);
		return section;
	}

	private ReportTable buildTable(Classpath classpath) {

		// map from JAR file name to package names
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

		// map from package name to JAR file names
		MultiMap<String, String> packageToJarFile = map.invert();

		ReportTable table = new ReportTable("JAR file", "Count", "Packages", "Issues");

		// for every JAR file ...
		for (String fileName : map.getKeys()) {
			Set<String> packageNames = map.getValues(fileName);
			List<String> packageGroups = getPackageGroups(packageNames, map, fileName);
			List<String> issues = findIssues(packageNames, packageToJarFile);
			table.addRow(fileName, String.valueOf(packageNames.size()), StringUtils.joinLines(packageGroups), StringUtils.joinLines(issues));
		}

		return table;
	}

	private List<String> findIssues(Set<String> packageNames, MultiMap<String, String> packageToJarFile) {
		List<String> issues = new ArrayList<>();

		// check if any package is found in more than one JAR file
		for (String packageName : packageNames) {
			Set<String> jarFileNames = packageToJarFile.getValues(packageName);
			if (jarFileNames.size() > 1) {
				issues.add("Split Package: " + packageName);
			}
		}

		// check if packages have different roots
		List<String> rootPackageNames = getRootPackageNames(packageNames);
		if (rootPackageNames.size() > 1) {
			String roots = rootPackageNames.stream()
					.map(packageName -> {
						if (packageNames.contains(packageName)) {
							return packageName;
						} else {
							return packageName + ".*";
						}
					})
					.collect(Collectors.joining(", "));
			issues.add("Fat JAR: " + roots);
		}

		return issues;
	}

	private List<String> getRootPackageNames(Set<String> packageNames) {
		return packageNames.stream().map(packageName -> {
			if (packageName.startsWith("org.") || packageName.startsWith("com.") || packageName.startsWith("net.")) {
				return getParentPackage(packageName, 2);
			} else {
				return getParentPackage(packageName, 1);
			}
		}).distinct().sorted().collect(Collectors.toList());
	}

	private List<String> getPackageGroups(Set<String> packageNames, MultiMap<String, String> map, String fileName) {

		// map from unique package prefix to group of packages
		Map<String, List<String>> packageGroups = new LinkedHashMap<>();

		for (String packageName : packageNames) {
			String uniqueParentPackage = getUniqueParentPackage(packageName, map, fileName);
			List<String> packageGroup = packageGroups.computeIfAbsent(uniqueParentPackage, k -> new ArrayList<>());
			packageGroup.add(packageName);
		}

		return packageGroups.values().stream().map(PackagesAnalyzer::getPackageGroupDescription).collect(Collectors.toList());
	}

	private static String getUniqueParentPackage(String packageName, MultiMap<String, String> map, String fileName) {

		int maxLength = 0;
		for (String fileName2 : map.getKeys()) {
			if (fileName.equals(fileName2)) continue;
			for (String packageName2 : map.getValues(fileName2)) {
				int length = getParentPackageLength(packageName, packageName2);
				if (length > maxLength) maxLength = length;
			}
		}

		String parentPackage = packageName;
		if (maxLength < getPackageLength(packageName) - 1) {
			parentPackage = getParentPackage(packageName, maxLength + 1);
		}
		return parentPackage;
	}

	private static int getPackageLength(String packageName) {
		int count = 1;
		int len = packageName.length();
		for (int i = 0; i < len; i++) {
			if (packageName.charAt(i) == '.') count++;
		}
		return count;
	}

	private static String getPackageGroupDescription(List<String> packageNames) {

		if (packageNames.size() == 1) {
			return packageNames.get(0);
		}

		String parentPackage = getParentPackage(packageNames);

		if (packageNames.contains(parentPackage)) {
			int subPackages = packageNames.size() - 1;
			if (subPackages == 1) {
				return parentPackage + " (+1 subpackage)";
			} else {
				return parentPackage + " (+" + subPackages + " subpackages)";
			}
		} else {
			int subPackages = packageNames.size();
			return parentPackage + ".* (" + subPackages + " subpackages)";
		}
	}

	private static String getParentPackage(List<String> packageNames) {
		String firstPackageName = packageNames.get(0);
		int minLength = Integer.MAX_VALUE;
		for (String packageName : packageNames) {
			int length = getParentPackageLength(firstPackageName, packageName);
			if (length < minLength) minLength = length;
		}
		return getParentPackage(firstPackageName, minLength);
	}

	private static int getParentPackageLength(String packageName1, String packageName2) {
		String[] parts1 = packageName1.split("\\.");
		String[] parts2 = packageName2.split("\\.");
		int length = Math.min(parts1.length, parts2.length);
		for (int i = 0; i < length; i++) {
			String part1 = parts1[i];
			String part2 = parts2[i];
			if (!part1.equals(part2)) {
				return i;
			}
		}
		return length;
	}

	private static String getParentPackage(String packageName, int length) {
		String[] parts = packageName.split("\\.", length + 1);
		return Arrays.stream(parts).limit(length).collect(Collectors.joining("."));
	}

}
