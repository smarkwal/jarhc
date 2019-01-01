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

import org.jarhc.utils.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

public class CachedResolver implements Resolver {

	private final File cacheDir;
	private final Resolver resolver;

	public CachedResolver(File cacheDir, Resolver resolver) {
		this.cacheDir = cacheDir;
		this.resolver = resolver;
	}

	@Override
	public Optional<Artifact> getArtifact(String checksum) throws ResolverException {
		validateChecksum(checksum);

		// create path to cache file
		File file = new File(cacheDir, checksum + ".txt");

		// if cache file exists ...
		if (file.isFile()) {

			// get artifact information from cache
			// (may be null in case of a cached negative response)
			Artifact artifact = getFromCache(file);
			return Optional.ofNullable(artifact);
		}

		// if a parent resolver is available ...
		if (resolver != null) {

			// delegate to parent resolver
			Optional<Artifact> artifact = resolver.getArtifact(checksum);

			// update cache with response
			saveToCache(artifact.orElse(null), file);

			return artifact;
		}

		// artifact not found
		return Optional.empty();
	}

	private Artifact getFromCache(File file) throws ResolverException {

		// if file is empty ...
		long size = file.length();
		if (size == 0) {
			// cached negative response
			return null;
		}

		try {

			// create parent directories (if needed)
			file.getParentFile().mkdirs();

			// read coordinates from file
			String coordinates = FileUtils.readFileToString(file);
			return new Artifact(coordinates);

		} catch (IOException e) {
			throw new ResolverException("I/O error", e);
		}

	}

	private void saveToCache(Artifact artifact, File file) throws ResolverException {

		try {

			// if parent resolver has found the artifact ...
			if (artifact != null) {
				// save in local cache
				FileUtils.writeStringToFile(artifact.toString(), file);
			} else {
				// create an empty file (cache negative response)
				FileUtils.touchFile(file);
			}

		} catch (IOException e) {
			throw new ResolverException("I/O error", e);
		}

	}

}
