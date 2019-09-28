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
import java.util.Optional;
import org.jarhc.artifacts.Artifact;
import org.jarhc.artifacts.Repository;
import org.jarhc.artifacts.RepositoryException;
import org.jarhc.model.Classpath;
import org.jarhc.model.JarFile;
import org.jarhc.report.ReportSection;
import org.jarhc.report.ReportTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DependenciesAnalyzer implements Analyzer {

	private static final Logger LOGGER = LoggerFactory.getLogger(DependenciesAnalyzer.class);

	private static final String UNKNOWN = "[unknown]";

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
			table.addRow(fileName, coordinates, "[todo]", "[todo]");
		}

		return table;
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
			return "[error]";
		}

		return artifact.map(Artifact::toString).orElse(UNKNOWN);
	}

}
