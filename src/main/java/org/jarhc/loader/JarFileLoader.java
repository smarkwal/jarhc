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
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;
import org.jarhc.app.JarSource;
import org.jarhc.model.ClassDef;
import org.jarhc.model.JarFile;
import org.jarhc.model.ModuleInfo;
import org.jarhc.model.ResourceDef;
import org.jarhc.utils.DigestUtils;
import org.jarhc.utils.FileUtils;
import org.jarhc.utils.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Loader for a JAR file, using a file as source.
 * This class is thread-safe and can be used in parallel or multiple times in sequence.
 */
class JarFileLoader {

	private static final Logger LOGGER = LoggerFactory.getLogger(JarFileLoader.class);

	private final ClassDefLoader classDefLoader;
	private final ModuleInfoLoader moduleInfoLoader;
	private final JarFileNameNormalizer jarFileNameNormalizer;

	JarFileLoader(ClassDefLoader classDefLoader, ModuleInfoLoader moduleInfoLoader, JarFileNameNormalizer jarFileNameNormalizer) {
		this.classDefLoader = classDefLoader;
		this.moduleInfoLoader = moduleInfoLoader;
		this.jarFileNameNormalizer = jarFileNameNormalizer;
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
		List<ClassDef> classDefs = new ArrayList<>();
		List<ResourceDef> resourceDefs = new ArrayList<>();

		// open JAR file for reading
		try (JarInputStream stream = new JarInputStream(new ByteArrayInputStream(fileData), false)) {

			boolean multiRelease = isMultiRelease(stream);

			// for every entry in the JAR file ...
			while (true) {
				JarEntry entry = stream.getNextJarEntry();
				if (entry == null) break;

				// skip directories
				if (entry.isDirectory()) {
					continue;
				}

				String name = entry.getName();

				if (multiRelease) {
					if (name.startsWith("META-INF/versions/")) {

						// extract Java release version from entry path
						try {
							String version = name.substring(18, name.indexOf('/', 18));
							int release = Integer.parseInt(version);
							releases.add(release);
						} catch (Exception e) {
							LOGGER.warn("Failed to extract release version: {}", name, e);
						}

						// TODO: support multi-release JAR files
						continue;
					}
				}

				// ignore files in META-INF // TODO: why?
				if (name.startsWith("META-INF/")) {
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
						// load module info
						moduleInfo = moduleInfoLoader.load(data);
					}

					// load class file
					ClassDef classDef = classDefLoader.load(data);

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

					classDefs.add(classDef);

				} else {

					// calculate SHA-1 checksum
					String checksum = DigestUtils.sha1Hex(data);

					// add as resource
					ResourceDef resourceDef = new ResourceDef(name, checksum);
					resourceDefs.add(resourceDef);

				}

			}
		}

		// calculate SHA-1 checksum of JAR file
		String checksum = DigestUtils.sha1Hex(fileData);

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
				.withReleases(releases)
				.withModuleInfo(moduleInfo)
				.withClassDefs(classDefs)
				.withResourceDefs(resourceDefs)
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

}
