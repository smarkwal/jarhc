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

package org.jarhc.loader;

import org.jarhc.artifacts.Artifact;
import org.jarhc.artifacts.Repository;
import org.jarhc.artifacts.RepositoryException;

import java.util.Optional;

public interface JarFileNameNormalizer {

	String getFileName(String fileName, String checksum);

	/**
	 * Removes the version number from the given file name.
	 * <p>
	 * Example: "asm-7.0.jar" becomes "asm.jar"
	 *
	 * @param fileName Original file name
	 * @return File name without version number
	 */
	static String getFileNameWithoutVersionNumber(String fileName) {
		return fileName.replaceAll("-[0-9]+(\\.[0-9]+)*(-SNAPSHOT)?", "");
	}

	/**
	 * Get the file name for the given artifact checksum.
	 * If the repository is able to identify the artifact, a file name based on the artifact ID
	 * and (depending on <code>removeVersion</code>) the version number is returned.
	 * If the repository is not able to identify the artifact, the original file name is returned.
	 *
	 * @param checksum      JAR file checksum
	 * @param repository    Repository used to identify artifact
	 * @param removeVersion <code>true</code> to not include the version number in the file name
	 * @param fileName      Original file name
	 * @return Artifact file name
	 */
	static String getArtifactFileName(String checksum, Repository repository, boolean removeVersion, String fileName) {
		try {
			Optional<Artifact> artifact = repository.findArtifact(checksum);
			if (artifact.isPresent()) {
				if (removeVersion) {
					// generate file name without version number
					return artifact.get().getArtifactId() + ".jar";
				} else {
					// generate file name with version number
					return artifact.get().getArtifactId() + "-" + artifact.get().getVersion() + ".jar";
				}
			}
		} catch (RepositoryException e) {
			e.printStackTrace();
		}
		if (removeVersion) {
			return JarFileNameNormalizer.getFileNameWithoutVersionNumber(fileName);
		} else {
			return fileName;
		}
	}

}
