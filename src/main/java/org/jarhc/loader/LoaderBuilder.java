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

import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.jarhc.artifacts.Artifact;
import org.jarhc.artifacts.Repository;
import org.jarhc.java.ClassLoader;
import org.jarhc.java.ClassLoaderStrategy;
import org.jarhc.pom.Dependency;
import org.jarhc.utils.JavaUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoaderBuilder {

	private String classLoader = "Classpath";
	private int release = JavaUtils.getJavaVersion();
	private boolean scanForReferences = true;
	private FileNameNormalizer fileNameNormalizer = null;
	private ClassLoader parentClassLoader = null;
	private ClassLoaderStrategy strategy = ClassLoaderStrategy.ParentLast;
	private Repository repository = new NoOpRepository();

	public static LoaderBuilder create() {
		return new LoaderBuilder();
	}

	public LoaderBuilder forClassLoader(String classLoader) {
		this.classLoader = classLoader;
		return this;
	}

	public LoaderBuilder forRelease(int release) {
		this.release = release;
		return this;
	}

	public LoaderBuilder scanForReferences(boolean scanForReferences) {
		this.scanForReferences = scanForReferences;
		return this;
	}

	public LoaderBuilder withFileNameNormalizer(FileNameNormalizer fileNameNormalizer) {
		this.fileNameNormalizer = fileNameNormalizer;
		return this;
	}

	public LoaderBuilder withParentClassLoader(ClassLoader parentClassLoader) {
		this.parentClassLoader = parentClassLoader;
		return this;
	}

	public LoaderBuilder withClassLoaderStrategy(ClassLoaderStrategy strategy) {
		this.strategy = strategy;
		return this;
	}

	public LoaderBuilder withRepository(Repository repository) {
		this.repository = repository;
		return this;
	}

	public ClassDefLoader buildClassDefLoader() {
		return new ClassDefLoader(classLoader, scanForReferences);
	}

	ModuleInfoLoader buildModuleInfoLoader() {
		return new ModuleInfoLoader();
	}

	JarFileLoader buildJarFileLoader() {
		ClassDefLoader classDefLoader = buildClassDefLoader();
		ModuleInfoLoader moduleInfoLoader = buildModuleInfoLoader();
		Logger logger = LoggerFactory.getLogger(JarFileLoader.class);
		return new JarFileLoader(classLoader, release, classDefLoader, moduleInfoLoader, fileNameNormalizer, repository, logger);
	}

	JmodFileLoader buildJmodFileLoader() {
		ClassDefLoader classDefLoader = buildClassDefLoader();
		ModuleInfoLoader moduleInfoLoader = buildModuleInfoLoader();
		Logger logger = LoggerFactory.getLogger(JmodFileLoader.class);
		return new JmodFileLoader(classLoader, release, classDefLoader, moduleInfoLoader, fileNameNormalizer, repository, logger);
	}

	public ClasspathLoader buildClasspathLoader() {
		JarFileLoader jarFileLoader = buildJarFileLoader();
		JmodFileLoader jmodFileLoader = buildJmodFileLoader();
		WarFileLoader warFileLoader = new WarFileLoader(jarFileLoader);
		Logger logger = LoggerFactory.getLogger(ClasspathLoader.class);
		return new ClasspathLoader(jarFileLoader, jmodFileLoader, warFileLoader, parentClassLoader, strategy, logger);
	}

	private static class NoOpRepository implements Repository {

		@Override
		public Optional<Artifact> findArtifact(String checksum) {
			return Optional.empty();
		}

		@Override
		public Optional<InputStream> downloadArtifact(Artifact artifact) {
			return Optional.empty();
		}

		@Override
		public List<Dependency> getDependencies(Artifact artifact) {
			return Collections.emptyList();
		}

	}

}
