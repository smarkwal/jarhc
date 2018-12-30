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

import java.util.Optional;

/**
 * Implementations of this interface are used to identify an artifact
 * given the SHA-1 checksum of a JAR file.
 *
 * @see MavenCentralResolver
 */
public interface Resolver {

	/**
	 * Try to resolve the given checksum and return information
	 * about the artifact with that checksum.
	 *
	 * @param checksum SHA-1 checksum of a JAR file
	 * @return Artifact information (if found)
	 * @throws ResolverException if an unexpected exception occurs
	 */
	Optional<Artifact> getArtifact(String checksum) throws ResolverException;

	/**
	 * Checks if the given checksum is valid:
	 * <ol>
	 * <li>checksum must not be <code>null</code>.</li>
	 * <li>checksum must contain only the digits '0' - '9' and letters 'a' - 'f' (hex numbers).</li>
	 * </ol>
	 * <p>
	 * This method can be used by resolver implementations to validate the input value.
	 *
	 * @param checksum Checksum
	 * @throws IllegalArgumentException if the given checksum is not valid.
	 */
	default void validateChecksum(String checksum) {
		if (checksum == null || !checksum.matches("[0-9a-f]+")) {
			throw new IllegalArgumentException("checksum: " + checksum);
		}
	}

}
