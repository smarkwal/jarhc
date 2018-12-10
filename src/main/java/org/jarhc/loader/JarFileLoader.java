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

import org.jarhc.model.ClassDef;
import org.jarhc.model.JarFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;

/**
 * Loader for a JAR file, using a file as source.
 * This class is thread-safe and can be used in parallel or multiple times in sequence.
 */
class JarFileLoader {

	private final ClassDefLoader classDefLoader = new ClassDefLoader();

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
	JarFile load(File file) throws IOException {
		if (file == null) throw new IllegalArgumentException("file");
		if (!file.isFile()) throw new FileNotFoundException(file.getAbsolutePath());

		List<ClassDef> classDefs = new ArrayList<>();

		// open JAR file for reading
		try (java.util.jar.JarFile jarFile = new java.util.jar.JarFile(file)) {

			// for every entry in the JAR file ...
			Enumeration<JarEntry> entries = jarFile.entries();
			while (entries.hasMoreElements()) {
				JarEntry entry = entries.nextElement();

				// skip directories
				if (entry.isDirectory()) {
					continue;
				}

				// only accept *.class files
				if (!entry.getName().endsWith(".class")) {
					continue;
				}

				// load class file
				InputStream stream = jarFile.getInputStream(entry);
				ClassDef classDef;
				try {
					classDef = classDefLoader.load(stream);
				} catch (IOException e) {
					throw new IOException(String.format("Unable to parse class entry: %s", entry.getName()), e);
				}

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
			}
		}

		return new JarFile(file.getName(), file.length(), classDefs);
	}

}
