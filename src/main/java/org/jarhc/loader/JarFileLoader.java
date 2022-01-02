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
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;
import org.jarhc.app.JarSource;
import org.jarhc.artifacts.Artifact;
import org.jarhc.artifacts.Repository;
import org.jarhc.artifacts.RepositoryException;
import org.jarhc.model.ClassDef;
import org.jarhc.model.JarFile;
import org.jarhc.model.ModuleInfo;
import org.jarhc.model.ResourceDef;
import org.jarhc.utils.DigestUtils;
import org.jarhc.utils.FileUtils;
import org.jarhc.utils.IOUtils;
import org.slf4j.Logger;

/**
 * Loader for a JAR file, using a file as source.
 * This class is thread-safe and can be used in parallel or multiple times in sequence.
 */
class JarFileLoader {

	private final String classLoader;
	private final int maxRelease; // for multi-rlease JAR files
	private final ClassDefLoader classDefLoader;
	private final ModuleInfoLoader moduleInfoLoader;
	private final JarFileNameNormalizer jarFileNameNormalizer;
	private final Repository repository;
	private final Logger logger;

	JarFileLoader(String classLoader, int maxRelease, ClassDefLoader classDefLoader, ModuleInfoLoader moduleInfoLoader, JarFileNameNormalizer jarFileNameNormalizer, Repository repository, Logger logger) {
		this.classLoader = classLoader;
		this.maxRelease = maxRelease;
		this.classDefLoader = classDefLoader;
		this.moduleInfoLoader = moduleInfoLoader;
		this.jarFileNameNormalizer = jarFileNameNormalizer;
		this.repository = repository;
		this.logger = logger;
	}

	/**
	 * Load a JAR file from the given file.
	 * This method does not check whether the given file
	 * has a correct JAR file name.
	 *
	 * @param file File
	 * @return JAR file
	 * @throws IllegalArgumentException If <code>file</code> is <code>null</code>.
	 * @throws FileNotFoundException    If the file does not exist.
	 * @throws IOException              If the file cannot be parsed.
	 */
	List<JarFile> load(File file) throws IOException {
		if (file == null) throw new IllegalArgumentException("file");
		if (!file.isFile()) throw new FileNotFoundException(file.getAbsolutePath());

		String fileName = file.getName();
		byte[] data = FileUtils.readFileToByteArray(file);
		return load(fileName, data);
	}

	List<JarFile> load(JarSource source) throws IOException {
		if (source == null) throw new IllegalArgumentException("source");

		String fileName = source.getName();
		byte[] data;
		try (InputStream stream = source.getData()) {
			data = IOUtils.toByteArray(stream);
		}
		return load(fileName, data);
	}

	List<JarFile> load(String fileName, byte[] fileData) throws IOException {
		if (fileName == null) throw new IllegalArgumentException("fileName");
		if (fileData == null) throw new IllegalArgumentException("fileData");

		// load JAR file (and nested JAR files if found)
		List<JarFile> jarFiles = new ArrayList<>();
		load(fileName, fileData, jarFiles);

		// sort JAR files by name
		jarFiles.sort((f1, f2) -> String.CASE_INSENSITIVE_ORDER.compare(f1.getFileName(), f2.getFileName()));

		return jarFiles;
	}

	private void load(String fileName, byte[] fileData, List<JarFile> jarFiles) throws IOException {

		ModuleInfo moduleInfo = null;
		Set<Integer> releases = new TreeSet<>();
		Map<String, ClassDef> classDefs = new HashMap<>();
		Map<String, ResourceDef> resourceDefs = new HashMap<>();

		// open JAR file for reading
		try (JarInputStream stream = new JarInputStream(new ByteArrayInputStream(fileData), false)) {

			boolean multiRelease = isMultiRelease(stream);

			// check if module has a manifest attribute "Automatic-Module-Name"
			String automaticModuleName = getAutomaticModuleName(stream);
			if (automaticModuleName != null) {
				// create module info for an automatic module with the given name
				moduleInfo = ModuleInfo.forModuleName(automaticModuleName).setAutomatic(true);
				// module info can be overridden if module-info.class is found later
				moduleInfo.setRelease(-1);
			}

			// for every entry in the JAR file ...
			while (true) {
				JarEntry entry = stream.getNextJarEntry();
				if (entry == null) break;

				// skip directories
				if (entry.isDirectory()) {
					continue;
				}

				String name = entry.getName();

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
				byte[] data;
				try {
					data = IOUtils.toByteArray(stream);
				} catch (IOException e) {
					throw new IOException(String.format("Unable to parse entry: %s", name), e);
				}

				if (name.endsWith(".jar")) {

					// load nested JAR file
					load(fileName + "!/" + name, data, jarFiles);

				} else if (name.endsWith(".class")) {

					if (name.equals("module-info.class")) {

						// ignore module info if it is for an older release
						if (moduleInfo != null && release < moduleInfo.getRelease()) {
							continue;
						}

						// load module info
						moduleInfo = moduleInfoLoader.load(data);
						moduleInfo.setRelease(release);
					}

					// ignore class definition if it is for an older release
					ClassDef classDef = classDefs.get(name);
					if (classDef != null && release < classDef.getRelease()) {
						continue;
					}

					// load class file
					classDef = classDefLoader.load(data);
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
					String checksum = DigestUtils.sha1Hex(data);

					// add as resource
					resourceDef = new ResourceDef(name, checksum);
					resourceDef.setRelease(release);
					resourceDefs.put(name, resourceDef);

				}

			}
		}

		// calculate SHA-1 checksum of JAR file
		String checksum = DigestUtils.sha1Hex(fileData);

		// try to identify JAR file as Maven artifact
		// TODO: if artifact was given as coordinates, skip this step and re-use the original coordinates instead.
		String coordinates = null;
		try {
			Optional<Artifact> artifact = repository.findArtifact(checksum);
			coordinates = artifact.map(Artifact::toCoordinates).orElse(null);
		} catch (RepositoryException e) {
			logger.warn("Artifact resolution error", e);
		}

		// normalize JAR file name (optional)
		if (jarFileNameNormalizer != null) {
			fileName = jarFileNameNormalizer.getFileName(fileName, checksum);
		}

		// append JAR file name to class loader name
		// String jarFileName = fileName;
		// classDefs.forEach(classDef -> classDef.setClassLoader(classDef.getClassLoader() + " (" + jarFileName + ")"));

		JarFile jarFile = JarFile.withName(fileName)
				.withFileSize(fileData.length)
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

	private boolean isMultiRelease(JarInputStream stream) {
		Manifest manifest = stream.getManifest();
		if (manifest == null) return false;
		Attributes attributes = manifest.getMainAttributes();
		String value = attributes.getValue("Multi-Release");
		return value != null && value.equals("true");
	}

	private String getAutomaticModuleName(JarInputStream stream) {
		Manifest manifest = stream.getManifest();
		if (manifest == null) return null;
		Attributes attributes = manifest.getMainAttributes();
		return attributes.getValue("Automatic-Module-Name");
	}

}
