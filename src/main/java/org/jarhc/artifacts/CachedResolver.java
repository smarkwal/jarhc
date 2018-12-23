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

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class CachedResolver implements Resolver {

	private final File cacheDir;
	private final Resolver resolver;

	public CachedResolver(Resolver resolver) {
		this.cacheDir = new File("./.jarhc/cache/resolver");
		this.resolver = resolver;
	}

	public CachedResolver(File cacheDir, Resolver resolver) {
		this.cacheDir = cacheDir;
		this.resolver = resolver;
	}

	@Override
	public Artifact getArtifact(String checksum) throws ResolverException {
		if (checksum == null || checksum.matches("[^a-z0-9]")) throw new IllegalArgumentException("checksum");

		// create path to cache file
		File file = new File(cacheDir, checksum + ".txt");

		// if cache file exists ...
		if (file.isFile()) {

			// get artifact information from cache
			// (may be null in case of a cached negative response)
			return getFromCache(file);
		}

		// if a parent resolver is available ...
		if (resolver != null) {

			// delegate to parent resolver
			Artifact artifact = resolver.getArtifact(checksum);

			// update cache with response
			saveToCache(artifact, file);

			return artifact;
		}

		// artifact not found
		return null;
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
			String coordinates = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
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
				FileUtils.write(file, artifact.toString(), StandardCharsets.UTF_8);
			} else {
				// create an empty file (cache negative response)
				FileUtils.touch(file);
			}

		} catch (IOException e) {
			throw new ResolverException("I/O error", e);
		}

	}

}
