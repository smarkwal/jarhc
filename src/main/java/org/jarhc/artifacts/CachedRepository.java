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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import org.jarhc.utils.FileUtils;

public class CachedRepository implements Repository {

	private final File directory;
	private final Repository repository;

	public CachedRepository(File directory, Repository repository) {
		this.directory = directory;
		this.repository = repository;
	}

	@Override
	public Optional<Artifact> findArtifact(String groupId, String artifactId, String version, String type) throws RepositoryException {

		Artifact artifact = new Artifact(groupId, artifactId, version, type);

		// check if file exists in cache
		String path = artifact.getPath();
		File file = new File(directory, path);
		if (file.isFile()) {
			return Optional.of(artifact);
		}

		// if a parent repository is available ...
		if (repository != null) {

			// delegate to parent repository
			Optional<Artifact> result = repository.findArtifact(groupId, artifactId, version, type);
			if (result.isPresent()) {
				return Optional.of(artifact);
			}

		}

		// artifact not found
		return Optional.empty();

	}

	@Override
	public Optional<Artifact> findArtifact(String checksum) throws RepositoryException {
		validateChecksum(checksum);

		// create path to cache file
		String path = "sha1/" + checksum + ".txt";
		File file = new File(directory, path);

		// if cache file exists ...
		if (file.isFile()) {

			// get artifact information from cache
			// (may be null in case of a cached negative response)
			Artifact artifact = getFromCache(file);
			return Optional.ofNullable(artifact);
		}

		// if a parent repository is available ...
		if (repository != null) {

			// delegate to parent repository
			Optional<Artifact> artifact = repository.findArtifact(checksum);

			// update cache with response
			saveToCache(artifact.orElse(null), file);

			return artifact;
		}

		// artifact not found
		return Optional.empty();
	}

	private Artifact getFromCache(File file) throws RepositoryException {

		// if file is empty ...
		long size = file.length();
		if (size == 0) {
			// cached negative response
			return null;
		}

		try {

			// read coordinates from file
			String coordinates = FileUtils.readFileToString(file);
			return new Artifact(coordinates);

		} catch (IOException e) {
			throw new RepositoryException("I/O error", e);
		}

	}

	private void saveToCache(Artifact artifact, File file) throws RepositoryException {

		try {

			// if parent repository has found the artifact ...
			if (artifact != null) {
				// save in local cache
				FileUtils.writeStringToFile(artifact.toString(), file);
			} else {
				// create an empty file (cache negative response)
				FileUtils.touchFile(file);
			}

		} catch (IOException e) {
			throw new RepositoryException("I/O error", e);
		}

	}

	@Override
	public Optional<InputStream> downloadArtifact(Artifact artifact) throws RepositoryException {

		String path = artifact.getPath();
		File file = new File(directory, path);

		// if cache file exists ...
		if (file.isFile()) {

			// get artifact from cache
			// (may be null in case of a cached negative response)
			InputStream stream = downloadFromCache(file);
			return Optional.ofNullable(stream);
		}

		// if a parent repository is available ...
		if (repository != null) {

			// delegate to parent repository
			Optional<InputStream> stream = repository.downloadArtifact(artifact);

			// update cache with response
			downloadToCache(stream.orElse(null), file);

			// get artifact from cache
			InputStream result = downloadFromCache(file);
			return Optional.ofNullable(result);
		}

		// artifact not found
		return Optional.empty();

	}

	private InputStream downloadFromCache(File file) throws RepositoryException {

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
			throw new RepositoryException("I/O error", e);
		}

	}

	private void downloadToCache(InputStream stream, File file) throws RepositoryException {

		try {

			// if parent repository has found the artifact ...
			if (stream != null) {
				// save in local cache
				FileUtils.writeStreamToFile(stream, file);
			} else {
				// create an empty file (cache negative response)
				FileUtils.touchFile(file);
			}

		} catch (IOException e) {
			throw new RepositoryException("I/O error", e);
		}

	}

}
