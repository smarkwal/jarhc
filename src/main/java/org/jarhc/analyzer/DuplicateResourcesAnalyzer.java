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

import org.jarhc.model.Classpath;
import org.jarhc.model.JarFile;
import org.jarhc.model.ResourceDef;
import org.jarhc.report.ReportSection;
import org.jarhc.report.ReportTable;
import org.jarhc.utils.StringUtils;

import java.util.*;

public class DuplicateResourcesAnalyzer extends Analyzer {

	@Override
	public ReportSection analyze(Classpath classpath) {

		ReportTable table = buildTable(classpath);

		ReportSection section = new ReportSection("Duplicate Resources", "Resources found in multiple JAR files.");
		section.add(table);
		return section;
	}

	private ReportTable buildTable(Classpath classpath) {

		// map from resource path to resources
		Map<String, List<ResourceDef>> map = new TreeMap<>();

		// for every JAR file ...
		List<JarFile> jarFiles = classpath.getJarFiles();
		for (JarFile jarFile : jarFiles) {

			// for every resource ...
			List<ResourceDef> resourceDefs = jarFile.getResourceDefs();
			for (ResourceDef resourceDef : resourceDefs) {

				String path = resourceDef.getPath();

				// remember resources for resource path
				List<ResourceDef> list = map.computeIfAbsent(path, k -> new ArrayList<>());
				list.add(resourceDef);
			}
		}

		ReportTable table = new ReportTable("Resource path", "JAR files", "Similarity");

		// for every resource path ...
		for (String path : map.keySet()) {
			List<ResourceDef> resourceDefs = map.get(path);
			// if resource has been found in more than one JAR file ...
			if (resourceDefs.size() > 1) {

				// get all JAR files (sorted by file name)
				String jarFileNames = getJarFileNames(resourceDefs);

				// calculates the level of similarity between the resources
				String similarity = getSimilarity(resourceDefs);

				table.addRow(path, jarFileNames, similarity);
			}
		}

		return table;
	}

	private static String getJarFileNames(Collection<ResourceDef> resourceDefs) {
		return resourceDefs.stream()
				.map(ResourceDef::getJarFile) // get JAR file
				.map(JarFile::getFileName) // get file name
				.sorted(String.CASE_INSENSITIVE_ORDER) // sort case-insensitive
				.collect(StringUtils.joinLines());
	}

	/**
	 * Compares a set of resources by file checksum.
	 *
	 * @param resourceDefs Resources
	 * @return Similarity
	 */
	private static String getSimilarity(Collection<ResourceDef> resourceDefs) {
		return "[not implemented]";
	}

}
