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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
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

		ReportSection section = new ReportSection("Dependencies", "Dependencies as declared in POM file.");
		section.add(table);
		return section;
	}

	private ReportTable buildTable(Classpath classpath) {

		ReportTable table = new ReportTable("JAR file", "Maven coordinates", "Direct dependencies", "Status");

		// for every JAR file ...
		List<JarFile> jarFiles = classpath.getJarFiles();
		for (JarFile jarFile : jarFiles) {

			// add a row with file name, size and class count
			String fileName = jarFile.getFileName();
			String coordinates = getCoordinates(jarFile);

			String dependenciesInfo = UNKNOWN;
			String status = "";

			if (Artifact.validateCoordinates(coordinates)) {

				// get list of direct dependencies
				List<Dependency> dependencies = null;
				try {
					dependencies = getDependencies(coordinates);
				} catch (POMNotFoundException e) {
					LOGGER.warn("POM not found: {}", e.getMessage());
				} catch (POMException e) {
					LOGGER.error("Resolver error for artifact: {}", coordinates, e);
				}

				if (dependencies == null) { // error
					dependenciesInfo = ERROR;
				} else if (dependencies.isEmpty()) { // no direct dependencies
					// show special value "none"
					dependenciesInfo = NONE;
				} else {
					List<String> lines = dependencies.stream()
							.map(Dependency::toString)
							.collect(Collectors.toList());
					dependenciesInfo = StringUtils.joinLines(lines);
				}

				if (dependencies != null) {
					status = getStatus(dependencies, classpath);
				}

			}

			table.addRow(fileName, coordinates, dependenciesInfo, status);
		}

		return table;
	}

	private List<Dependency> getDependencies(String coordinates) throws POMException {

		Artifact artifact = new Artifact(coordinates);

		// try to find all direct dependencies
		List<Dependency> dependencies = dependencyResolver.getDependencies(artifact);

		// ignore test dependencies
		dependencies.removeIf(d -> d.getScope() == Scope.TEST);

		// return list of dependencies
		return dependencies;
	}

	private String getStatus(List<Dependency> dependencies, Classpath classpath) {

		List<String> lines = new ArrayList<>(dependencies.size());

		for (Dependency dependency : dependencies) {

			// search for JAR file with same group ID and artifact ID
			Predicate<JarFile> predicate = jarFile -> matches(jarFile, dependency);
			Optional<JarFile> result = classpath.getJarFile(predicate);
			if (result.isPresent()) {
				JarFile jarFile = result.get();

				// check if it is an exact match
				StringBuilder line = new StringBuilder("OK");
				String coordinates = jarFile.getCoordinates();
				Artifact artifact = new Artifact(coordinates);
				if (!artifact.equals(dependency.toArtifact())) {
					line.append(" (version ").append(artifact.getVersion()).append(")");
				}

				String classLoader = jarFile.getClassLoader();
				if (classLoader != null && !classLoader.equals("Classpath")) {
					line.append(" [").append(classLoader).append("]");
				}

				lines.add(line.toString());
			} else {
				lines.add("Unsatisfied");
			}
		}

		return StringUtils.joinLines(lines);
	}

	private boolean matches(JarFile jarFile, Dependency dependency) {

		String coordinates = jarFile.getCoordinates();
		if (coordinates == null) {
			// skip JAR files without coordinates
			return false;
		}

		Artifact jarArtifact = new Artifact(coordinates);
		Artifact dependencyArtifact = dependency.toArtifact();

		// check if group ID matches
		if (!Objects.equals(jarArtifact.getGroupId(), dependencyArtifact.getGroupId())) {
			return false;
		}

		// check if artifact ID matches
		if (!Objects.equals(jarArtifact.getArtifactId(), dependencyArtifact.getArtifactId())) {
			return false;
		}

		// TODO: check if type matches?

		// note: version may be different
		return true;
	}

	private String getCoordinates(JarFile jarFile) {
		String coordinates = jarFile.getCoordinates();
		if (coordinates == null) {
			return UNKNOWN;
		}
		return coordinates;
	}

}
