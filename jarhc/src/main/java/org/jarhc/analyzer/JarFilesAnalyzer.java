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

import static org.jarhc.utils.FileUtils.formatFileSize;
import static org.jarhc.utils.Markdown.code;
import static org.jarhc.utils.StringUtils.wrapList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import org.jarhc.artifacts.Artifact;
import org.jarhc.model.ClassDef;
import org.jarhc.model.Classpath;
import org.jarhc.model.JarFile;
import org.jarhc.report.ReportSection;
import org.jarhc.report.ReportTable;
import org.jarhc.utils.JavaUtils;
import org.jarhc.utils.JavaVersion;
import org.jarhc.utils.Markdown;
import org.jarhc.utils.StringUtils;

public class JarFilesAnalyzer implements Analyzer {

	private static final String MAVEN_SEARCH_URL_PATTERN = "https://search.maven.org/search?q=1:%s";

	@Override
	public ReportSection analyze(Classpath classpath) {

		ReportTable table = buildTable(classpath);

		ReportSection section = new ReportSection("JAR Files", "List of JAR files found in classpath.");
		section.add(table);
		return section;
	}

	private ReportTable buildTable(Classpath classpath) {

		ReportTable table = new ReportTable("Artifact", "Version", "Source", "Size", "Multi-release", "Java version (classes)", "Resources", "Packages", "Checksum (SHA-1)", "Coordinates", "Issues");

		// total values
		long totalFileSize = 0;
		int totalResourceCount = 0;
		int totalPackageCount = 0;
		ClassVersionsCounter classpathCounter = new ClassVersionsCounter();

		// TODO: get package names from JarFile object

		// map from JAR file UUID to package names
		Map<String, List<String>> uuidToPackages = buildPackagesMap(classpath);

		// map from package name to JAR file UUID
		Map<String, List<String>> packageToUUIDs = invert(uuidToPackages);

		// list of all JAR file UUIDs
		List<String> uuids = new ArrayList<>(uuidToPackages.keySet());

		// for every JAR file ...
		List<JarFile> jarFiles = classpath.getJarFiles();
		for (JarFile jarFile : jarFiles) {
			String uuid = jarFile.getUUID();
			List<String> packageNames = uuidToPackages.get(uuid);

			ClassVersionsCounter jarFileCounter = new ClassVersionsCounter();
			countJavaVersions(jarFile, jarFileCounter, classpathCounter);

			// add a row with file name, size and class count
			String displayName = jarFile.getDisplayName();
			String version = getVersion(jarFile);
			String source = getSource(jarFile);
			long fileSize = jarFile.getFileSize();
			String multiReleaseInfo = getMultiReleaseInfo(jarFile);
			String javaVersionInfo = wrapList(jarFileCounter.toString(), 60);
			int resourceCount = jarFile.getResourceDefs().size();
			String packagesInfo = StringUtils.joinLines(getPackageGroups(packageNames, uuidToPackages, uuid, uuids));
			String checksum = getChecksumInfo(jarFile);
			String coordinates = getCoordinates(jarFile);

			// TODO: add more issues:
			// - "Artifact not found by checksum"
			// - "Artifact not found by coordinates"
			List<String> issues = findIssues(packageNames, packageToUUIDs);

			table.addRow(displayName, version, source, formatFileSize(fileSize), multiReleaseInfo, javaVersionInfo, String.valueOf(resourceCount), packagesInfo, checksum, coordinates, StringUtils.joinLines(issues));

			// update total values
			totalFileSize += fileSize;
			totalResourceCount += resourceCount;
			totalPackageCount += packageNames.size();
		}

		// add a row with total values
		String javaVersionInfo = wrapList(classpathCounter.toString(), 60);
		table.addRow("Classpath", "-", "-", formatFileSize(totalFileSize), "-", javaVersionInfo, String.valueOf(totalResourceCount), String.valueOf(totalPackageCount), "-", "-", "-");

		return table;
	}

	private static void countJavaVersions(JarFile jarFile, ClassVersionsCounter jarFileCounter, ClassVersionsCounter classpathCounter) {

		// for every class definition ...
		List<ClassDef> classDefs = jarFile.getClassDefs();
		for (ClassDef classDef : classDefs) {

			// TODO: make this configurable ?
			if (!classDef.isRegularClass()) {
				// ignore package-info and module-info classes
				continue;
			}

			int classVersion = classDef.getMajorClassVersion();

			jarFileCounter.count(classVersion);
			classpathCounter.count(classVersion);
		}
	}

	private String getVersion(JarFile jarFile) {
		String version = jarFile.getArtifactVersion();
		if (version == null || version.isEmpty()) {
			return Markdown.UNKNOWN;
		}
		return version;
	}

	private static String getSource(JarFile jarFile) {
		String coordinates = jarFile.getCoordinates();
		if (coordinates != null) {
			Artifact artifact = new Artifact(coordinates);
			return artifact.toLink();
		}
		return jarFile.getFileName();
	}

	private String getMultiReleaseInfo(JarFile jarFile) {
		if (jarFile.isMultiRelease()) {
			// TODO: sort releases in descending order (17, 11, 8, ...)
			String releases = jarFile.getReleases().stream().map(r -> "Java " + r).collect(Collectors.joining(", "));
			return "Yes (" + releases + ")";
		} else {
			return "No";
		}
	}

	private String getChecksumInfo(JarFile jarFile) {
		String checksum = jarFile.getChecksum();
		if (checksum == null || checksum.isEmpty()) return Markdown.UNKNOWN;
		String url = String.format(MAVEN_SEARCH_URL_PATTERN, checksum);
		return Markdown.link(checksum, url);
	}

	private String getCoordinates(JarFile jarFile) {
		List<Artifact> artifacts = jarFile.getArtifacts();
		if (artifacts == null) {
			return Markdown.ERROR; // most likely a response timeout
		} else if (artifacts.isEmpty()) {
			return Markdown.UNKNOWN;
		}
		// return coordinates of all artifacts
		return artifacts.stream().map(Artifact::toLink).collect(StringUtils.joinLines());
	}

	private static class ClassVersionsCounter {

		// map from major class version to number of classes
		private final Map<Integer, Integer> map = new TreeMap<>();

		void count(int majorClassVersion) {
			map.merge(majorClassVersion, 1, Integer::sum);
		}

		@Override
		public String toString() {
			int size = map.size();
			if (size == 0) {
				return "[no class files]";
			}
			return map.entrySet().stream()
					.sorted(this::sortByClassVersion)
					.map(this::formatJavaVersion)
					.collect(Collectors.joining(", "));
		}

		private int sortByClassVersion(Map.Entry<Integer, Integer> entry1, Map.Entry<Integer, Integer> entry2) {
			return entry2.getKey() - entry1.getKey();
		}

		private String formatJavaVersion(Map.Entry<Integer, Integer> entry) {
			return JavaVersion.fromClassVersion(entry.getKey()) + " (" + entry.getValue() + ")";
		}

	}

	// packages ------------------------------------------------------------------------------------

	/**
	 * Build a map from JAR file UUID to package names.
	 *
	 * @param classpath Classpath
	 * @return Map from JAR file UUID to package names
	 */
	private static Map<String, List<String>> buildPackagesMap(Classpath classpath) {

		Map<String, List<String>> uuidToPackages = new HashMap<>();

		// for every JAR file ...
		List<JarFile> jarFiles = classpath.getJarFiles();
		for (JarFile jarFile : jarFiles) {

			// remember package names by UUID
			String uuid = jarFile.getUUID();
			List<String> packageNames = jarFile.getPackageNames();
			uuidToPackages.put(uuid, packageNames);
		}

		return uuidToPackages;
	}

	/**
	 * Invert the given multi-value map.
	 *
	 * @param map Map to invert
	 * @return Inverted map
	 */
	private static Map<String, List<String>> invert(Map<String, List<String>> map) {
		Map<String, List<String>> result = new HashMap<>(map.size());
		for (Map.Entry<String, List<String>> entry : map.entrySet()) {
			String key = entry.getKey();
			List<String> values = entry.getValue();
			for (String value : values) {
				List<String> list = result.computeIfAbsent(value, k -> new ArrayList<>(2));
				if (!list.contains(key)) {
					list.add(key);
				}
			}
		}
		return result;
	}

	private static List<String> findIssues(List<String> packageNames, Map<String, List<String>> packageToUUIDs) {
		List<String> issues = new ArrayList<>();

		// check if any package is found in more than one JAR file
		for (String packageName : packageNames) {
			List<String> uuids = packageToUUIDs.get(packageName);
			if (uuids.size() > 1) {
				issues.add("Split Package: " + code(packageName));
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
					.map(Markdown::code)
					.collect(Collectors.joining(", "));
			issues.add("Fat JAR: " + roots);
		}

		return issues;
	}

	private static List<String> getRootPackageNames(List<String> packageNames) {
		List<String> rootPackageNames = new ArrayList<>(4);
		for (String packageName : packageNames) {
			String rootPackageName = getRootPackageName(packageName);
			if (!rootPackageNames.contains(rootPackageName)) {
				rootPackageNames.add(rootPackageName);
			}
		}
		if (rootPackageNames.size() > 1) {
			rootPackageNames.sort(null);
		}
		return rootPackageNames;
	}

	private static String getRootPackageName(String packageName) {
		int parts = 1;
		if (packageName.startsWith("org.") || packageName.startsWith("com.") || packageName.startsWith("net.")) {
			parts = 2;
		}
		return JavaUtils.getParentPackageName(packageName, parts);
	}

	private static List<String> getPackageGroups(List<String> packageNames, Map<String, List<String>> uuidToPackages, String uuid, List<String> uuids) {

		// map from unique package prefix to group of packages
		Map<String, List<String>> packageGroups = new LinkedHashMap<>();

		for (String packageName : packageNames) {
			String uniqueParentPackage = getUniqueParentPackage(packageName, uuidToPackages, uuid, uuids);
			List<String> packageGroup = packageGroups.computeIfAbsent(uniqueParentPackage, k -> new ArrayList<>(4));
			packageGroup.add(packageName);
		}

		return packageGroups.values().stream().map(JarFilesAnalyzer::getPackageGroupDescription).collect(Collectors.toList());
	}

	private static String getUniqueParentPackage(String packageName, Map<String, List<String>> uuidToPackages, String uuid, List<String> uuids) {

		// calculate maximum common package prefix with packages in other JAR files
		int maxLength = 0;

		// for all other JAR files ...
		//noinspection ForLoopReplaceableByForEach (performance)
		for (int i = 0; i < uuids.size(); i++) {
			String otherUUID = uuids.get(i);
			if (otherUUID.equals(uuid)) {
				// ignore current JAR file
				continue;
			}

			// for all packages in other JAR file ...
			List<String> otherPackageNames = uuidToPackages.get(otherUUID);
			//noinspection ForLoopReplaceableByForEach (performance)
			for (int j = 0; j < otherPackageNames.size(); j++) {
				String otherPackageName = otherPackageNames.get(j);

				int length = getParentPackageLength(packageName, otherPackageName);
				if (length > maxLength) {
					maxLength = length;
				}
			}
		}

		String parentPackage = packageName;
		if (maxLength < getPackageLength(packageName) - 1) {
			parentPackage = JavaUtils.getParentPackageName(packageName, maxLength + 1);
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
			return code(packageNames.get(0));
		}

		String parentPackage = getParentPackage(packageNames);

		if (packageNames.contains(parentPackage)) {
			int subPackages = packageNames.size() - 1;
			if (subPackages == 1) {
				return code(parentPackage) + " (+1 subpackage)";
			} else {
				return code(parentPackage) + " (+" + subPackages + " subpackages)";
			}
		} else {
			int subPackages = packageNames.size();
			return code(parentPackage + ".*") + " (" + subPackages + " subpackages)";
		}
	}

	private static String getParentPackage(List<String> packageNames) {
		String firstPackageName = packageNames.get(0);
		int minLength = Integer.MAX_VALUE;
		for (String packageName : packageNames) {
			int length = getParentPackageLength(firstPackageName, packageName);
			if (length < minLength) minLength = length;
		}
		return JavaUtils.getParentPackageName(firstPackageName, minLength);
	}

	/**
	 * Get number of parent packages in common between two package names.
	 *
	 * @param packageName1 First package name.
	 * @param packageName2 Second package name.
	 * @return Number of common parent packages.
	 */
	static int getParentPackageLength(String packageName1, String packageName2) {

		int length1 = packageName1.length();
		int length2 = packageName2.length();

		int minLength = Math.min(length1, length2);
		if (minLength == 0) return 0; // at least one of the package names is empty

		int parts = 0; // number of package parts already matched
		for (int i = 0; i < minLength; i++) {
			char char1 = packageName1.charAt(i);
			char char2 = packageName2.charAt(i);
			if (char1 != char2) {
				return parts;
			} else if (char1 == '.') {
				parts++; // matched package part found
			}
		}

		if (length1 == length2) {
			// example: a.b.c vs a.b.c
			return parts + 1;
		} else if (length1 > length2) {
			if (packageName1.charAt(minLength) == '.') {
				// example: a.b.c vs a.b
				return parts + 1;
			} else {
				// example: a.bx vs a.b
				return parts;
			}
		} else { // length2 > length1
			if (packageName2.charAt(minLength) == '.') {
				// example: a.b vs a.b.c
				return parts + 1;
			} else {
				// example: a.b vs a.bx
				return parts;
			}
		}

	}

}
