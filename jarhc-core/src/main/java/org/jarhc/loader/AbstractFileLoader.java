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

package org.jarhc.loader;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import org.jarhc.artifacts.Artifact;
import org.jarhc.artifacts.Repository;
import org.jarhc.artifacts.RepositoryException;
import org.jarhc.loader.archive.Archive;
import org.jarhc.loader.archive.ArchiveEntry;
import org.jarhc.loader.archive.JarStreamArchive;
import org.jarhc.model.ClassDef;
import org.jarhc.model.JarFile;
import org.jarhc.model.ModuleInfo;
import org.jarhc.model.ResourceDef;
import org.jarhc.utils.ByteBuffer;
import org.jarhc.utils.DigestUtils;
import org.slf4j.Logger;

/**
 * Loader for a JAR file, using a file as source.
 * This class is thread-safe and can be used in parallel or multiple times in sequence.
 */
abstract class AbstractFileLoader {

	private final String classLoader;
	private final int maxRelease; // for multi-release JAR files
	private final ClassDefLoader classDefLoader;
	private final ModuleInfoLoader moduleInfoLoader;
	private final FileNameNormalizer fileNameNormalizer;
	private final Repository repository;
	private final Logger logger;

	AbstractFileLoader(String classLoader, int maxRelease, ClassDefLoader classDefLoader, ModuleInfoLoader moduleInfoLoader, FileNameNormalizer fileNameNormalizer, Repository repository, Logger logger) {
		this.classLoader = classLoader;
		this.maxRelease = maxRelease;
		this.classDefLoader = classDefLoader;
		this.moduleInfoLoader = moduleInfoLoader;
		this.fileNameNormalizer = fileNameNormalizer;
		this.repository = repository;
		this.logger = logger;
	}

	protected void load(String fileName, Archive archive, List<JarFile> jarFiles) throws IOException {

		ModuleInfo moduleInfo = ModuleInfo.UNNAMED;
		Set<Integer> releases = new TreeSet<>();
		Map<String, ClassDef> classDefs = new HashMap<>();
		Map<String, ResourceDef> resourceDefs = new HashMap<>();

		// check if JAR is a multi-release JAR file
		boolean multiRelease = archive.isMultiRelease();

		// check if module has a manifest attribute "Automatic-Module-Name"
		String automaticModuleName = archive.getAutomaticModuleName();
		if (automaticModuleName != null) {
			// create module info for an automatic module with the given name
			moduleInfo = ModuleInfo.forModuleName(automaticModuleName).setAutomatic(true);
			// module info can be overridden if module-info.class is found later
			moduleInfo.setRelease(-1);
		}

		// for every entry in the JAR or JMOD file ...
		while (true) {
			ArchiveEntry entry = archive.getNextEntry();
			if (entry == null) break;

			// get entry name (file path side JAR or JMOD file)
			String name = entry.getName();

			// check if file has to be loaded
			name = filter(name);
			if (name == null) {
				// file is ignored
				continue;
			}

			int release = 8;
			if (multiRelease) {
				if (name.startsWith("META-INF/versions/")) {

					// extract Java release version from entry path
					try {
						int pos = name.indexOf('/', 18);
						String version = name.substring(18, pos);
						release = Integer.parseInt(version);
						releases.add(release);

						// remove "META-INF/versions/<release>/" from entry path
						name = name.substring(pos + 1);

					} catch (Exception e) {
						logger.warn("Failed to extract release version: {}", name, e);
					}

				}
			}

			// ignore entries for newer releases
			if (release > maxRelease) {
				continue;
			}

			// ignore files in META-INF // TODO: why?
			if (name.startsWith("META-INF/")) {
				// TODO: support service providers
				continue;
			}

			// read file data
			ByteBuffer data;
			try {
				data = entry.getData();
			} catch (IOException e) {
				throw new IOException(String.format("Unable to parse entry: %s", name), e);
			}

			try {

				if (name.endsWith(".jar")) {

					// load nested JAR file
					InputStream inputStream = new ByteArrayInputStream(data.getBytes(), 0, data.getLength());
					String jarFileName = fileName + "!/" + name;
					try (JarStreamArchive jarStreamArchive = new JarStreamArchive(inputStream)) {
						load(jarFileName, jarStreamArchive, jarFiles);
					}

				} else if (name.endsWith(".class")) {

					if (name.equals("module-info.class")) {

						// ignore module info if it is for an older release
						if (release < moduleInfo.getRelease()) {
							continue;
						}

						// load module info
						moduleInfo = moduleInfoLoader.load(data.getBytes(), 0, data.getLength());
						moduleInfo.setRelease(release);
					}

					// ignore class definition if it is for an older release
					ClassDef classDef = classDefs.get(name);
					if (classDef != null && release < classDef.getRelease()) {
						continue;
					}

					// load class file
					classDef = classDefLoader.load(data.getBytes(), 0, data.getLength());
					classDef.setRelease(release);

					/*
					Certificate[] certificates = entry.getCertificates();
					if (certificates != null && certificates.length > 0) {
						Certificate certificate = certificates[0];
						if (certificate instanceof X509Certificate) {
							X509Certificate x509 = (X509Certificate) certificate;
							String subject = x509.getSubjectDN().getName();
							// TODO: save subject
						}
					}
					// TODO: verify signature
					*/

					classDefs.put(name, classDef);

				} else {

					// ignore resource if it is for an older release
					ResourceDef resourceDef = resourceDefs.get(name);
					if (resourceDef != null && release < resourceDef.getRelease()) {
						continue;
					}

					// calculate SHA-1 checksum
					String checksum = DigestUtils.sha1Hex(data.getBytes(), 0, data.getLength());

					// add as resource
					resourceDef = new ResourceDef(name, checksum);
					resourceDef.setRelease(release);
					resourceDefs.put(name, resourceDef);

				}

			} finally {
				data.release();
			}

		}

		// calculate SHA-1 checksum of JAR file
		String checksum = archive.getFileChecksum();

		// try to identify JAR file as Maven artifact
		// TODO: if artifact was given as coordinates, skip this step and re-use the original coordinates instead.
		String coordinates = null;
		try {
			Optional<Artifact> artifact = repository.findArtifact(checksum);
			coordinates = artifact.map(Artifact::toCoordinates).orElse(null);
		} catch (RepositoryException e) {
			logger.warn("Artifact resolution error", e);
		}

		// normalize file name (optional)
		if (fileNameNormalizer != null) {
			fileName = fileNameNormalizer.getFileName(fileName, checksum);
		}

		JarFile jarFile = JarFile.withName(fileName)
				.withFileSize(archive.getFileSize())
				.withChecksum(checksum)
				.withCoordinates(coordinates)
				.withClassLoader(classLoader)
				.withReleases(releases)
				.withModuleInfo(moduleInfo)
				.withClassDefs(classDefs.values())
				.withResourceDefs(resourceDefs.values())
				.build();

		jarFiles.add(jarFile);
	}

	protected String filter(String name) {
		// accept all entries
		return name;
	}

}
