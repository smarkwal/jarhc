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

import java.util.Optional;
import org.jarhc.app.Options;
import org.jarhc.artifacts.Artifact;
import org.jarhc.artifacts.Repository;
import org.jarhc.artifacts.RepositoryException;
import org.slf4j.Logger;

public class FileNameNormalizer {

	private final boolean useArtifactName;
	private final boolean removeVersion;
	private final Repository repository;
	private final Logger logger;

	public FileNameNormalizer(Options options, Repository repository, Logger logger) {
		this.useArtifactName = options.isUseArtifactName();
		this.removeVersion = options.isRemoveVersion();
		this.repository = repository;
		this.logger = logger;
	}

	public String getFileName(String fileName, String checksum) {

		if (useArtifactName) {
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
				logger.warn("Failed to find artifact in repository.", e);
			}
		}

		if (removeVersion) {
			return getFileNameWithoutVersionNumber(fileName);
		} else {
			return fileName;
		}

	}

	/**
	 * Removes the version number from the given file name.
	 * <p>
	 * Example: "asm-7.0.jar" becomes "asm.jar"
	 *
	 * @param fileName Original file name
	 * @return File name without version number
	 */
	public static String getFileNameWithoutVersionNumber(String fileName) {
		return fileName.replaceAll("-[0-9]+(\\.[0-9]+){0,10}(-SNAPSHOT)?", "");
	}

}
