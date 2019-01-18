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

import java.io.*;
import java.util.Optional;

public class CachedResolver implements Resolver {

	private final File cacheDir;
	private final Resolver resolver;

	public CachedResolver(File cacheDir, Resolver resolver) {
		this.cacheDir = cacheDir;
		this.resolver = resolver;
	}

	@Override
	public Optional<Artifact> findArtifact(String groupId, String artifactId, String version, String type) throws ResolverException {

		// create path to cache file
		String fileName = groupId + "_" + artifactId + "_" + version + "_" + type + ".txt";
		File file = new File(cacheDir, fileName);

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
			Optional<Artifact> artifact = resolver.findArtifact(groupId, artifactId, version, type);

			// update cache with response
			saveToCache(artifact.orElse(null), file);

			return artifact;
		}

		// artifact not found
		return Optional.empty();

	}

	@Override
	public Optional<Artifact> findArtifact(String checksum) throws ResolverException {
		validateChecksum(checksum);

		// create path to cache file
		String fileName = checksum + ".txt";
		File file = new File(cacheDir, fileName);

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
			Optional<Artifact> artifact = resolver.findArtifact(checksum);

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

	@Override
	public Optional<InputStream> downloadArtifact(Artifact artifact) throws ResolverException {

		String coordinates = artifact.toString();
		String fileName = coordinates.replace(':', '_') + ".bin";
		File file = new File(cacheDir, fileName);

		// if cache file exists ...
		if (file.isFile()) {

			// get artifact from cache
			// (may be null in case of a cached negative response)
			InputStream stream = downloadFromCache(file);
			return Optional.ofNullable(stream);
		}

		// if a parent resolver is available ...
		if (resolver != null) {

			// delegate to parent resolver
			Optional<InputStream> stream = resolver.downloadArtifact(artifact);

			// update cache with response
			downloadToCache(stream.orElse(null), file);

			// get artifact from cache
			InputStream result = downloadFromCache(file);
			return Optional.ofNullable(result);
		}

		// artifact not found
		return Optional.empty();

	}

	private InputStream downloadFromCache(File file) throws ResolverException {

		// if file is empty ...
		long size = file.length();
		if (size == 0) {
			// cached negative response
			return null;
		}

		// read data from file
		try {
			return new FileInputStream(file);
		} catch (FileNotFoundException e) {
			throw new ResolverException("I/O error", e);
		}

	}

	private void downloadToCache(InputStream stream, File file) throws ResolverException {

		try {

			// if parent resolver has found the artifact ...
			if (stream != null) {
				// save in local cache
				FileUtils.writeStreamToFile(stream, file);
			} else {
				// create an empty file (cache negative response)
				FileUtils.touchFile(file);
			}

		} catch (IOException e) {
			throw new ResolverException("I/O error", e);
		}

	}

}
