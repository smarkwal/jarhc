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

import java.util.List;
import org.jarhc.artifacts.Artifact;
import org.jarhc.model.ClassDef;
import org.jarhc.model.Classpath;
import org.jarhc.model.JarFile;
import org.jarhc.report.ReportSection;
import org.jarhc.report.ReportTable;
import org.jarhc.utils.StringUtils;

public class JarFilesAnalyzer implements Analyzer {

	private static final String UNKNOWN = "[unknown]";
	private static final String ERROR = "[error]";

	@Override
	public ReportSection analyze(Classpath classpath) {

		ReportTable table = buildTable(classpath);

		ReportSection section = new ReportSection("JAR Files", "List of JAR files found in classpath.");
		section.add(table);
		return section;
	}

	private ReportTable buildTable(Classpath classpath) {

		ReportTable table = new ReportTable("Artifact", "Version", "Source", "Size", "Classes", "Resources", "Checksum (SHA-1)", "Coordinates");

		// total values
		long totalFileSize = 0;
		int totalClassCount = 0;
		int totalResourceCount = 0;

		// for every JAR file ...
		List<JarFile> jarFiles = classpath.getJarFiles();
		for (JarFile jarFile : jarFiles) {

			// add a row with file name, size and class count
			String artifact = jarFile.getArtifactName();
			String version = getVersion(jarFile);
			String source = getSource(jarFile);
			long fileSize = jarFile.getFileSize();
			String checksum = getChecksumInfo(jarFile);
			int classCount = (int) jarFile.getClassDefs().stream().filter(ClassDef::isRegularClass).count(); // TODO: make this configurable ?
			int resourceCount = jarFile.getResourceDefs().size();
			String coordinates = getCoordinates(jarFile);
			table.addRow(artifact, version, source, formatFileSize(fileSize), String.valueOf(classCount), String.valueOf(resourceCount), checksum, coordinates);

			// update total values
			totalFileSize += fileSize;
			totalClassCount += classCount;
			totalResourceCount += resourceCount;
		}

		// add a row with total values
		table.addRow("Classpath", "-", "-", formatFileSize(totalFileSize), String.valueOf(totalClassCount), String.valueOf(totalResourceCount), "-", "-");

		return table;
	}

	private String getVersion(JarFile jarFile) {
		String version = jarFile.getArtifactVersion();
		if (version == null || version.isEmpty()) {
			return UNKNOWN;
		}
		return version;
	}

	private static String getSource(JarFile jarFile) {
		String coordinates = jarFile.getCoordinates();
		if (coordinates != null) {
			Artifact artifact = new Artifact(coordinates);
			return artifact.toCoordinates();
		}
		return jarFile.getFileName();
	}

	private String getChecksumInfo(JarFile jarFile) {
		String checksum = jarFile.getChecksum();
		if (checksum == null || checksum.isEmpty()) return UNKNOWN;
		return checksum;
	}

	private String getCoordinates(JarFile jarFile) {
		List<Artifact> artifacts = jarFile.getArtifacts();
		if (artifacts == null) {
			return ERROR; // most likely a response timeout
		} else if (artifacts.isEmpty()) {
			return UNKNOWN;
		}
		// return coordinates of all artifacts
		return artifacts.stream().map(Artifact::toCoordinates).collect(StringUtils.joinLines());
	}

}
