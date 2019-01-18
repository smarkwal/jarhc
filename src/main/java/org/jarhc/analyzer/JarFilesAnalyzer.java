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

import org.jarhc.artifacts.Artifact;
import org.jarhc.artifacts.Repository;
import org.jarhc.artifacts.RepositoryException;
import org.jarhc.model.ClassDef;
import org.jarhc.model.Classpath;
import org.jarhc.model.JarFile;
import org.jarhc.report.ReportSection;
import org.jarhc.report.ReportTable;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.jarhc.utils.FileUtils.formatFileSize;

public class JarFilesAnalyzer extends Analyzer {

	private final Repository repository;

	public JarFilesAnalyzer(Repository repository) {
		if (repository == null) throw new IllegalArgumentException("repository");
		this.repository = repository;
	}

	@Override
	public ReportSection analyze(Classpath classpath) {

		ReportTable table = buildTable(classpath);

		ReportSection section = new ReportSection("JAR Files", "List of JAR files found in classpath.");
		section.add(table);
		return section;
	}

	private ReportTable buildTable(Classpath classpath) {

		ReportTable table = new ReportTable("JAR file", "Size", "Classes", "Resources", "Multi-release", "Module", "Checksum (SHA-1)", "Artifact coordinates");

		// total values
		long totalFileSize = 0;
		int totalClassCount = 0;
		int totalResourceCount = 0;

		// for every JAR file ...
		List<JarFile> jarFiles = classpath.getJarFiles();
		for (JarFile jarFile : jarFiles) {

			// add a row with file name, size and class count
			String fileName = jarFile.getFileName();
			long fileSize = jarFile.getFileSize();
			String checksum = getChecksumInfo(jarFile);
			int classCount = (int) jarFile.getClassDefs().stream().filter(ClassDef::isRegularClass).count(); // TODO: make this configurable ?
			int resourceCount = jarFile.getResourceDefs().size();
			String multiReleaseInfo = getMultiReleaseInfo(jarFile);
			String moduleInfo = getModuleInfo(jarFile);
			String coordinates = getCoordinates(jarFile);
			table.addRow(fileName, formatFileSize(fileSize), String.valueOf(classCount), String.valueOf(resourceCount), multiReleaseInfo, moduleInfo, checksum, coordinates);

			// update total values
			totalFileSize += fileSize;
			totalClassCount += classCount;
			totalResourceCount += resourceCount;
		}

		// add a row with total values
		table.addRow("Classpath", formatFileSize(totalFileSize), String.valueOf(totalClassCount), String.valueOf(totalResourceCount), "-", "-", "-", "-");

		return table;
	}

	private String getChecksumInfo(JarFile jarFile) {
		String checksum = jarFile.getChecksum();
		if (checksum == null || checksum.isEmpty()) return "[unknown]";
		return checksum;
	}

	private String getMultiReleaseInfo(JarFile jarFile) {
		if (jarFile.isMultiRelease()) {
			String releases = jarFile.getReleases().stream().map(r -> "Java " + r).collect(Collectors.joining(", "));
			return "Yes (" + releases + ")";
		} else {
			return "No";
		}
	}

	private String getModuleInfo(JarFile jarFile) {
		if (jarFile.isModule()) {
			return "Yes (" + jarFile.getModuleInfo().getModuleName() + ")";
		} else {
			return "No";
		}
	}

	private String getCoordinates(JarFile jarFile) {

		String checksum = jarFile.getChecksum();
		if (checksum == null || checksum.isEmpty()) {
			return "[unknown]";
		}

		Optional<Artifact> artifact;
		try {
			artifact = repository.findArtifact(checksum);
		} catch (RepositoryException e) {
			System.err.println("Repository error for JAR file: " + jarFile.getFileName());
			e.printStackTrace();
			return "[error]";
		}

		return artifact.map(Artifact::toString).orElse("[unknown]");
	}

}
