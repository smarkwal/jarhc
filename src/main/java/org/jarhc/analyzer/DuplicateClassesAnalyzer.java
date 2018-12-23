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
import org.jarhc.utils.StringUtils;

import java.util.*;
import java.util.function.Function;

public class DuplicateClassesAnalyzer extends Analyzer {

	@Override
	public ReportSection analyze(Classpath classpath) {

		ReportTable table = buildTable(classpath);

		ReportSection section = new ReportSection("Duplicate Classes", "Classes found in multiple JAR files.");
		section.add(table);
		return section;
	}

	private ReportTable buildTable(Classpath classpath) {

		// map from class name to class definitions
		Map<String, List<ClassDef>> map = new TreeMap<>();

		// for every JAR file ...
		List<JarFile> jarFiles = classpath.getJarFiles();
		for (JarFile jarFile : jarFiles) {

			// for every class definition ...
			List<ClassDef> classDefs = jarFile.getClassDefs();
			for (ClassDef classDef : classDefs) {

				String className = classDef.getClassName();
				if (className.equals("module-info")) continue;

				// remember class definitions for class name
				List<ClassDef> list = map.computeIfAbsent(className, k -> new ArrayList<>());
				list.add(classDef);
			}
		}

		ReportTable table = new ReportTable("Class name", "JAR files", "Similarity");

		// for every package ...
		for (String className : map.keySet()) {
			List<ClassDef> classDefs = map.get(className);
			// if class has been found in more than one JAR file ...
			if (classDefs.size() > 1) {

				// get all JAR files (sorted by file name)
				String jarFileNames = getJarFileNames(classDefs);

				// calculates the level of similarity between the class definitions
				String similarity = getSimilarity(classDefs);

				table.addRow(formatClassName(className), jarFileNames, similarity);
			}
		}

		return table;
	}

	private static String formatClassName(String name) {
		return name.replaceAll("/", ".");
	}

	private static String getJarFileNames(Collection<ClassDef> classDefs) {
		return classDefs.stream()
				.map(ClassDef::getJarFile) // get JAR file
				.map(JarFile::getFileName) // get file name
				.sorted(String.CASE_INSENSITIVE_ORDER) // sort case-insensitive
				.collect(StringUtils.joinLines());
	}

	/**
	 * Compares a set of class definitions by class file checksum and API checksum.
	 *
	 * @param classDefs Class definitions
	 * @return Similarity
	 */
	private static String getSimilarity(Collection<ClassDef> classDefs) {

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

		return "Different API";
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

}
