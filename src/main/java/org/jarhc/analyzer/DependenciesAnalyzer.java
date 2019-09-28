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

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.jarhc.artifacts.Artifact;
import org.jarhc.artifacts.Repository;
import org.jarhc.artifacts.RepositoryException;
import org.jarhc.model.Classpath;
import org.jarhc.model.JarFile;
import org.jarhc.pom.Dependency;
import org.jarhc.pom.Model;
import org.jarhc.pom.ModelException;
import org.jarhc.pom.ModelReader;
import org.jarhc.pom.Scope;
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

	private final Repository repository;

	public DependenciesAnalyzer(Repository repository) {
		if (repository == null) throw new IllegalArgumentException("repository");
		this.repository = repository;
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

			List<String> dependencies = getDependencies(coordinates);

			table.addRow(fileName, coordinates, StringUtils.joinLines(dependencies), "[todo]");
		}

		return table;
	}

	private List<String> getDependencies(String coordinates) {

		if (!Artifact.validateCoordinates(coordinates)) {
			return Collections.singletonList(UNKNOWN);
		}

		Artifact artifact = new Artifact(coordinates);
		artifact = artifact.withType("pom");

		Optional<InputStream> result;
		try {
			result = repository.downloadArtifact(artifact);
			if (!result.isPresent()) {
				LOGGER.warn("POM file not found: " + artifact);
				return Collections.singletonList(ERROR);
			}
		} catch (RepositoryException e) {
			LOGGER.warn("Repository error for POM file: " + artifact, e);
			return Collections.singletonList(ERROR);
		}

		try (InputStream inputStream = result.get()) {
			ModelReader reader = new ModelReader();
			Model model = reader.read(inputStream);
			List<String> dependencies = model.getDependencies().stream()
					.filter(d -> d.getScope() != Scope.TEST)
					.map(Dependency::toString)
					.collect(Collectors.toList());
			if (dependencies.isEmpty()) {
				return Collections.singletonList(NONE);
			}
			return dependencies;
		} catch (IOException | ModelException e) {
			LOGGER.warn("Repository error for POM file: " + artifact, e);
			return Collections.singletonList(ERROR);
		}

	}

	private String getCoordinates(JarFile jarFile) {

		String checksum = jarFile.getChecksum();
		if (checksum == null || checksum.isEmpty()) {
			return UNKNOWN;
		}

		Optional<Artifact> artifact;
		try {
			artifact = repository.findArtifact(checksum);
		} catch (RepositoryException e) {
			LOGGER.warn("Repository error for JAR file: {}", jarFile.getFileName(), e);
			return ERROR;
		}

		return artifact.map(Artifact::toString).orElse(UNKNOWN);
	}

}
