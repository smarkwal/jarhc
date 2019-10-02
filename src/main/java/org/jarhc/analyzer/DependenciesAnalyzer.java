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

import java.util.List;
import java.util.stream.Collectors;
import org.jarhc.artifacts.Artifact;
import org.jarhc.model.Classpath;
import org.jarhc.model.JarFile;
import org.jarhc.pom.Dependency;
import org.jarhc.pom.POMException;
import org.jarhc.pom.Scope;
import org.jarhc.pom.resolver.DependencyResolver;
import org.jarhc.pom.resolver.POMNotFoundException;
import org.jarhc.report.ReportSection;
import org.jarhc.report.ReportTable;
import org.jarhc.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DependenciesAnalyzer implements Analyzer {

	private static final Logger LOGGER = LoggerFactory.getLogger(DependenciesAnalyzer.class);

	private static final String NONE = "[none]";
	private static final String UNKNOWN = "[unknown]";
	private static final String ERROR = "[error]";

	private final DependencyResolver dependencyResolver;

	public DependenciesAnalyzer(DependencyResolver dependencyResolver) {
		if (dependencyResolver == null) throw new IllegalArgumentException("dependencyResolver");
		this.dependencyResolver = dependencyResolver;
	}

	@Override
	public ReportSection analyze(Classpath classpath) {

		ReportTable table = buildTable(classpath);

		ReportSection section = new ReportSection("Dependencies", "Direct and transitive dependencies as declared in POM file.");
		section.add(table);
		return section;
	}

	private ReportTable buildTable(Classpath classpath) {

		ReportTable table = new ReportTable("JAR file", "Maven coordinates", "Direct dependencies", "Transitive dependencies");

		// for every JAR file ...
		List<JarFile> jarFiles = classpath.getJarFiles();
		for (JarFile jarFile : jarFiles) {

			// add a row with file name, size and class count
			String fileName = jarFile.getFileName();
			String coordinates = getCoordinates(jarFile);

			String directDependenciesInfo = UNKNOWN;
			String transitiveDependenciesInfo = UNKNOWN;

			if (Artifact.validateCoordinates(coordinates)) {

				List<Dependency> directDependencies = getDirectDependencies(coordinates);
				if (directDependencies == null) { // error
					directDependenciesInfo = ERROR;
				} else if (directDependencies.isEmpty()) { // no direct dependencies
					// show special value "none"
					directDependenciesInfo = NONE;
					transitiveDependenciesInfo = NONE;
				} else {
					List<String> lines = directDependencies.stream()
							.map(Dependency::toString)
							.collect(Collectors.toList());
					directDependenciesInfo = StringUtils.joinLines(lines);

					// TODO: collect transitive dependencies
					transitiveDependenciesInfo = "[todo]";

				}
			}

			table.addRow(fileName, coordinates, directDependenciesInfo, transitiveDependenciesInfo);
		}

		return table;
	}

	private List<Dependency> getDirectDependencies(String coordinates) {

		Artifact artifact = new Artifact(coordinates);

		try {

			// try to find all direct dependencies
			List<Dependency> dependencies = dependencyResolver.getDependencies(artifact);

			// ignore test dependencies
			dependencies.removeIf(d -> d.getScope() == Scope.TEST);

			// return list of dependencies
			return dependencies;

		} catch (POMNotFoundException e) {
			LOGGER.warn(e.getMessage());
			return null;
		} catch (POMException e) {
			LOGGER.error("Resolver error for artifact: {}", artifact, e);
			return null;
		}

	}

	private String getCoordinates(JarFile jarFile) {
		String coordinates = jarFile.getCoordinates();
		if (coordinates == null || coordinates.isEmpty()) {
			return UNKNOWN;
		}
		return coordinates;
	}

}
