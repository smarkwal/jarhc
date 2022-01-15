/*
 * Copyright 2019 Stephan Markwalder
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

import static java.lang.Math.min;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.jarhc.app.Options;
import org.jarhc.java.ClassLoader;
import org.jarhc.model.ClassDef;
import org.jarhc.model.Classpath;
import org.jarhc.model.JarFile;
import org.jarhc.model.ResourceDef;
import org.jarhc.report.ReportSection;
import org.jarhc.report.ReportTable;
import org.jarhc.utils.StringUtils;

public class DuplicateClassesAnalyzer implements Analyzer {

	private final boolean ignoreExactCopy;

	public DuplicateClassesAnalyzer(Options options) {
		this.ignoreExactCopy = options.isIgnoreExactCopy();
	}

	@Override
	public ReportSection analyze(Classpath classpath) {

		ReportTable table = buildTable(classpath);

		ReportSection section = new ReportSection("Duplicate Classes", "Duplicate classes, shadowed classes, and duplicate resources.");
		section.add(table);
		return section;
	}

	private ReportTable buildTable(Classpath classpath) {

		ClassLoader parentClassLoader = classpath.getParent();

		// map from class name to class definitions
		Map<String, List<ClassDef>> duplicateClasses = new TreeMap<>();

		// map from resource path to resource definitions
		Map<String, List<ResourceDef>> duplicateResources = new TreeMap<>();

		// TODO: parallel execution

		// for every JAR file ...
		List<JarFile> jarFiles = classpath.getJarFiles();
		for (JarFile jarFile : jarFiles) {
			collectDuplicateClasses(jarFile, duplicateClasses);
			collectDuplicateResources(jarFile, duplicateResources);
		}

		if (parentClassLoader != null) {
			// find shadowed classes
			collectShadowedClasses(duplicateClasses, parentClassLoader);

			// TODO: search for shadowed resources
		}

		// remove classes and resources found only once
		duplicateClasses.values().removeIf(c -> c.size() < 2);
		duplicateResources.values().removeIf(r -> r.size() < 2);

		// create table
		ReportTable table = new ReportTable("Class/Resource", "Sources", "Similarity");
		buildDuplicateClassesRows(duplicateClasses, table);
		buildDuplicateResourcesRows(duplicateResources, table);
		return table;
	}

	private void collectDuplicateClasses(JarFile jarFile, Map<String, List<ClassDef>> duplicateClasses) {

		// for every class definition ...
		List<ClassDef> classDefs = jarFile.getClassDefs();
		for (ClassDef classDef : classDefs) {

			String className = classDef.getClassName();
			if (className.equals("module-info")) continue;

			// remember class definitions for class name
			List<ClassDef> list = duplicateClasses.computeIfAbsent(className, k -> new ArrayList<>());
			list.add(classDef);

		}
	}

	private void collectDuplicateResources(JarFile jarFile, Map<String, List<ResourceDef>> duplicateResources) {

		// for every resource definition ...
		List<ResourceDef> resourceDefs = jarFile.getResourceDefs();
		for (ResourceDef resourceDef : resourceDefs) {

			String path = resourceDef.getPath();

			// remember resources for resource path
			List<ResourceDef> list = duplicateResources.computeIfAbsent(path, k -> new ArrayList<>());
			list.add(resourceDef);

		}
	}

	private void collectShadowedClasses(Map<String, List<ClassDef>> duplicateClasses, ClassLoader parentClassLoader) {

		// for every class ...
		for (Map.Entry<String, List<ClassDef>> entry : duplicateClasses.entrySet()) {
			String className = entry.getKey();
			List<ClassDef> classDefs = entry.getValue();

			// check if class shadows provided class or JRE class
			parentClassLoader.getClassDef(className).ifPresent(classDefs::add);
		}

	}

	private void buildDuplicateClassesRows(Map<String, List<ClassDef>> duplicateClasses, ReportTable table) {

		// for every duplicate class ...
		for (Map.Entry<String, List<ClassDef>> entry : duplicateClasses.entrySet()) {
			String className = entry.getKey();
			List<ClassDef> classDefs = entry.getValue();

			// get JAR file or class loader (sorted by name)
			String sources = getClassSources(classDefs);

			// calculates the level of similarity between the class definitions
			String similarity = getClassSimilarity(classDefs);

			if (ignoreExactCopy && similarity.equals("Exact copy")) {
				continue;
			}

			table.addRow(className, sources, similarity);
		}
	}

	private void buildDuplicateResourcesRows(Map<String, List<ResourceDef>> duplicateResources, ReportTable table) {

		// for every duplicate resource ...
		for (Map.Entry<String, List<ResourceDef>> entry : duplicateResources.entrySet()) {
			String resourcePath = entry.getKey();
			List<ResourceDef> resourceDefs = entry.getValue();

			// get JAR file or class loader (sorted by name)
			String sources = getResourceSources(resourceDefs);

			// calculates the level of similarity between the resources
			String similarity = getResourceSimilarity(resourceDefs);

			if (ignoreExactCopy && similarity.equals("Exact copy")) {
				continue;
			}

			table.addRow(resourcePath, sources, similarity);
		}

	}

	private static String getClassSources(Collection<ClassDef> classDefs) {
		return classDefs.stream()
				.map(DuplicateClassesAnalyzer::getSource) // get JAR file name and/or class loader
				.sorted(String.CASE_INSENSITIVE_ORDER) // sort case-insensitive
				.collect(StringUtils.joinLines());
	}

	/**
	 * Get JAR file name and/or class loader name of the given class definition.
	 *
	 * @param classDef Class definition.
	 * @return JAR file name and/or class loader.
	 */
	private static String getSource(ClassDef classDef) {
		JarFile jarFile = classDef.getJarFile();
		String classLoader = classDef.getClassLoader();
		if (jarFile != null) {
			return jarFile.getFileName() + " (" + classLoader + ")";
		} else {
			return classLoader;
		}
	}

	private String getResourceSources(List<ResourceDef> resourceDefs) {
		return resourceDefs.stream()
				.map(ResourceDef::getJarFile) // get JAR file
				.map(JarFile::getFileName) // get JAR file name
				.sorted(String.CASE_INSENSITIVE_ORDER) // sort case-insensitive
				.collect(StringUtils.joinLines());
	}

	/**
	 * Compares a set of class definitions by class file checksum and API checksum.
	 *
	 * @param classDefs Class definitions
	 * @return Similarity
	 */
	private static String getClassSimilarity(Collection<ClassDef> classDefs) {

		// compare class file checksums
		if (sameChecksum(classDefs, ClassDef::getClassFileChecksum)) {
			// all classes are absolute identical (byte-by-byte)
			return "Exact copy";
		}

		// compare API checksums
		if (sameChecksum(classDefs, ClassDef::getApiChecksum)) {
			// all classes have the same API (non-private elements)
			return "Same API";
		}

		StringBuilder result = new StringBuilder("Different API");
		List<String[]> apis = classDefs.stream().map(def -> def.getApiDescription().split("\n")).collect(Collectors.toList());
		for (int i = 1; i < apis.size(); i++) {
			String[] apis1 = apis.get(0);
			String[] apis2 = apis.get(i);
			long diff = calculateApiDiff(apis1, apis2);
			int total = Math.max(apis1.length, apis2.length);

			BigDecimal similarity = BigDecimal.valueOf(100).subtract(
					BigDecimal.valueOf(100 * diff).divide(BigDecimal.valueOf(total), RoundingMode.CEILING)
			);

			result.append("\n(").append(total - diff).append("/").append(total).append(" = ").append(similarity).append("% similar)");
		}

		return result.toString();
	}

	/**
	 * Checks if the given class definitions have the same checksum.
	 *
	 * @param classDefs        Class definitions
	 * @param checksumFunction Checksum function
	 * @return <code>true</code> if all class definitions have the same checksum, <code>false</code> otherwise
	 */
	private static boolean sameChecksum(Collection<ClassDef> classDefs, Function<ClassDef, String> checksumFunction) {
		return classDefs.stream()
				.map(checksumFunction) // calculate checksum
				.distinct() // get unique checksums
				.limit(2) // stop when 2 different checksums have been found
				.count() < 2;
	}

	/**
	 * Compares a set of resources by checksum.
	 *
	 * @param resourceDefs Resource definitions
	 * @return Similarity
	 */
	private static String getResourceSimilarity(Collection<ResourceDef> resourceDefs) {
		boolean sameContent = resourceDefs.stream().map(ResourceDef::getChecksum).distinct().limit(2).count() < 2;
		if (sameContent) {
			return "Exact copy";
		} else {
			return "Different content";
		}
	}

	public static int calculateApiDiff(String[] api1, String[] api2) {

		int len1 = api1.length;
		int len2 = api2.length;
		int[] line1 = new int[len2 + 1];
		int[] line2 = new int[len2 + 1];

		for (int i = 0; i <= len2; i++) {
			line1[i] = i;
		}

		for (int i = 0; i < len1; i++) {
			line2[0] = i + 1;

			for (int j = 0; j < len2; j++) {
				int del = line1[j + 1] + 1;
				int ins = line2[j] + 1;
				boolean eq = api1[i].equals(api2[j]);
				int sub = eq ? line1[j] : line1[j] + 1;
				int min = min(min(del, ins), sub);
				line2[j + 1] = min;
			}

			// swap line 1 and line 2
			int[] tmp = line2;
			line2 = line1;
			line1 = tmp;
		}

		return line1[len2];
	}

}
