/*
 * Copyright 2025 Stephan Markwalder
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
import java.util.TreeMap;
import java.util.stream.Collectors;
import org.jarhc.artifacts.Artifact;
import org.jarhc.artifacts.ArtifactVersion;
import org.jarhc.artifacts.Repository;
import org.jarhc.artifacts.RepositoryException;
import org.jarhc.model.Classpath;
import org.jarhc.model.JarFile;
import org.jarhc.report.ReportSection;
import org.jarhc.report.ReportTable;
import org.jarhc.utils.StringUtils;
import org.slf4j.Logger;

public class UpdatesAnalyzer implements Analyzer {

	private static final String UNKNOWN = "[unknown]";
	private static final String ERROR = "[error]";

	private final Repository repository;
	private final Logger logger;

	public UpdatesAnalyzer(Repository repository, Logger logger) {
		if (repository == null) throw new IllegalArgumentException("repository");
		this.repository = repository;
		this.logger = logger;
	}

	@Override
	public ReportSection analyze(Classpath classpath) {

		ReportTable table = buildTable(classpath);

		ReportSection section = new ReportSection("Updates", "Information about newer major, minor, and patch versions of artifacts.");
		section.add(table);
		return section;
	}

	private ReportTable buildTable(Classpath classpath) {

		ReportTable table = new ReportTable("Artifact", "Current version", "Newer versions");

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

		String version = "";
		String versionsInfo = "";

		if (Artifact.validateCoordinates(coordinates)) {

			Artifact artifact = new Artifact(coordinates);
			String groupId = artifact.getGroupId();
			String artifactId = artifact.getArtifactId();
			version = artifact.getVersion();

			// get list of versions
			List<ArtifactVersion> versions = null;
			try {
				versions = repository.getVersions(groupId, artifactId);
			} catch (RepositoryException e) {
				logger.error("Resolver error for artifact: {}", coordinates, e);
			}

			if (versions == null) { // error
				versionsInfo = ERROR;
			} else if (versions.isEmpty()) { // no versions found
				versionsInfo = UNKNOWN;
			} else {

				// keep only newer versions
				ArtifactVersion currentVersion = new ArtifactVersion(version);
				List<ArtifactVersion> newerVersions = versions.stream()
						.filter(v -> v.compareTo(currentVersion) > 0) // keep only newer versions
						.filter(ArtifactVersion::isStable) // keep only stable versions // TODO: make this configurable?
						.collect(Collectors.toList());

				if (!newerVersions.isEmpty()) {
					// TODO: render Markdown link to version on Maven Central
					versionsInfo = getVersionsInfo(newerVersions);
				}
			}

		}

		return new String[] { artifactName, version, versionsInfo };
	}

	private String getVersionsInfo(List<ArtifactVersion> newerVersions) {

		// group versions by minor version
		TreeMap<ArtifactVersion, List<ArtifactVersion>> versions = new TreeMap<>();
		for (ArtifactVersion version : newerVersions) {
			ArtifactVersion key = new ArtifactVersion(version.getMajor(), version.getMinor(), 0);
			List<ArtifactVersion> group = versions.computeIfAbsent(key, k -> new ArrayList<>());
			group.add(version);
		}

		List<String> lines = new ArrayList<>(versions.size());
		for (ArtifactVersion version : versions.keySet()) {
			// convert versions to strings
			List<String> group = versions.get(version).stream().map(ArtifactVersion::toString).collect(Collectors.toList());
			// keep at max 7 versions per group
			if (group.size() > 7) {
				group = List.of(group.get(0), group.get(1), group.get(2), "[...]", group.get(group.size() - 3), group.get(group.size() - 2), group.get(group.size() - 1));
			}
			lines.add(String.join(", ", group));
		}

		// TODO: keep the group for the latest minor version of the same major version branch

		// keep at max 7 groups
		if (lines.size() > 7) {
			lines = List.of(lines.get(0), lines.get(1), lines.get(2), "[...]", lines.get(lines.size() - 3), lines.get(lines.size() - 2), lines.get(lines.size() - 1));
		}

		return StringUtils.joinLines(lines);
	}

	private String getCoordinates(JarFile jarFile) {

		// prefer artifact coordinates given as command line argument
		String coordinates = jarFile.getCoordinates();
		if (coordinates != null) {
			return coordinates;
		}

		List<Artifact> artifacts = jarFile.getArtifacts();
		if (artifacts == null) {
			return ERROR; // most likely a response timeout
		} else if (artifacts.isEmpty()) {
			return UNKNOWN;
		}
		// return only coordinates of "primary" artifact
		return artifacts.get(0).toCoordinates();
	}

}
