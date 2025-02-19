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
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.jarhc.artifacts.Artifact;
import org.jarhc.artifacts.Repository;
import org.jarhc.artifacts.RepositoryException;
import org.jarhc.model.Classpath;
import org.jarhc.model.JarFile;
import org.jarhc.pom.Dependency;
import org.jarhc.pom.Scope;
import org.jarhc.pom.SmartDependencyComparator;
import org.jarhc.report.ReportSection;
import org.jarhc.report.ReportTable;
import org.jarhc.utils.Markdown;
import org.jarhc.utils.StringUtils;
import org.slf4j.Logger;

public class DependenciesAnalyzer implements Analyzer {

	private final Repository repository;
	private final Logger logger;

	public DependenciesAnalyzer(Repository repository, Logger logger) {
		if (repository == null) throw new IllegalArgumentException("repository");
		this.repository = repository;
		this.logger = logger;
	}

	@Override
	public ReportSection analyze(Classpath classpath) {

		ReportTable table = buildTable(classpath);

		ReportSection section = new ReportSection("Dependencies", "Dependencies as declared in POM file.");
		section.add(table);
		return section;
	}

	private ReportTable buildTable(Classpath classpath) {

		ReportTable table = new ReportTable("Artifact", "Maven coordinates", "Direct dependencies", "Status");

		// for every JAR file ...
		List<JarFile> jarFiles = classpath.getJarFiles();
		jarFiles.parallelStream()
				.map(jarFile -> buildRow(jarFile, classpath))
				.forEachOrdered(table::addRow);

		return table;
	}

	private String[] buildRow(JarFile jarFile, Classpath classpath) {

		String artifactName = jarFile.getArtifactName();
		String coordinates = getCoordinates(jarFile);

		String coordinatesInfo = coordinates;
		String dependenciesInfo = Markdown.UNKNOWN;
		String status = "";

		if (Artifact.validateCoordinates(coordinates)) {

			Artifact artifact = new Artifact(coordinates);
			coordinatesInfo = artifact.toLink();

			// get list of direct dependencies
			List<Dependency> dependencies = null;
			try {
				dependencies = getDependencies(coordinates);
			} catch (RepositoryException e) {
				logger.error("Resolver error for artifact: {}", coordinates, e);
			}

			if (dependencies == null) { // error
				dependenciesInfo = Markdown.ERROR;
			} else if (dependencies.isEmpty()) { // no direct dependencies
				// show special value "none"
				dependenciesInfo = Markdown.NONE;
			} else {
				// sort dependencies, prefer dependencies with same or similar group or artifact ID
				dependencies.sort(new SmartDependencyComparator(artifact));
				List<String> lines = dependencies.stream()
						.map(Dependency::toMarkdown)
						.collect(Collectors.toList());
				dependenciesInfo = StringUtils.joinLines(lines);
			}

			if (dependencies != null) {
				status = getStatus(dependencies, classpath);
			}

		}

		return new String[] { artifactName, coordinatesInfo, dependenciesInfo, status };
	}

	private List<Dependency> getDependencies(String coordinates) throws RepositoryException {

		Artifact artifact = new Artifact(coordinates);

		// try to find all direct dependencies
		List<Dependency> dependencies = repository.getDependencies(artifact);

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
			JarFile jarFile = classpath.getJarFile(predicate);
			if (jarFile != null) {

				// check if it is an exact match
				StringBuilder line = new StringBuilder("OK");
				Artifact artifact = jarFile.getArtifacts().get(0);
				if (artifact.getGroupId().equals(dependency.getGroupId()) && artifact.getArtifactId().equals(dependency.getArtifactId())) {
					if (!artifact.getVersion().equals(dependency.getVersion())) {
						line.append(" (version ").append(artifact.getVersion()).append(")");
					}
				} else {
					line.append(" (").append(artifact.toCoordinates()).append(")");
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

		List<Artifact> artifacts = jarFile.getArtifacts();
		if (artifacts == null || artifacts.isEmpty()) {
			// skip JAR files without coordinates
			return false;
		}

		for (Artifact artifact : artifacts) {

			// check if group ID matches
			if (!Objects.equals(artifact.getGroupId(), dependency.getGroupId())) {
				continue;
			}

			// check if artifact ID matches
			if (!Objects.equals(artifact.getArtifactId(), dependency.getArtifactId())) {
				continue;
			}

			// TODO: check if type matches?

			// note: version may be different
			return true;
		}

		return false;
	}

	private String getCoordinates(JarFile jarFile) {

		// prefer artifact coordinates given as command line argument
		String coordinates = jarFile.getCoordinates();
		if (coordinates != null) {
			return coordinates;
		}

		List<Artifact> artifacts = jarFile.getArtifacts();
		if (artifacts == null) {
			return Markdown.ERROR; // most likely a response timeout
		} else if (artifacts.isEmpty()) {
			return Markdown.UNKNOWN;
		}
		// return only coordinates of "primary" artifact
		return artifacts.get(0).toCoordinates();
	}

}
