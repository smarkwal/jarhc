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

package org.jarhc.artifacts;

import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import org.jarhc.pom.Dependency;

/**
 * Implementations of this interface are used to find and download
 * artifacts given the artifact coordinates or SHA-1 checksum of a JAR file.
 *
 * @see MavenRepository
 */
public interface Repository {

	/**
	 * Try to find the artifact with the SHA-1 checksum.
	 *
	 * @param checksum SHA-1 checksum of a JAR file
	 * @return Artifact information (if found)
	 * @throws RepositoryException if an unexpected exception occurs
	 */
	Optional<Artifact> findArtifact(String checksum) throws RepositoryException;

	/**
	 * Try to download the artifact file for the given artifact.
	 *
	 * @param artifact Artifact
	 * @return Artifact file (if found)
	 * @throws RepositoryException if an unexpected exception occurs
	 */
	Optional<InputStream> downloadArtifact(Artifact artifact) throws RepositoryException;

	List<Dependency> getDependencies(Artifact artifact) throws RepositoryException;

}
