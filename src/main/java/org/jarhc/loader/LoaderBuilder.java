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

public class LoaderBuilder {

	private String classLoader = "Classpath";
	private boolean scanForReferences = true;
	private JarFileNameNormalizer jarFileNameNormalizer = null;
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

	public LoaderBuilder scanForReferences(boolean scanForReferences) {
		this.scanForReferences = scanForReferences;
		return this;
	}

	public LoaderBuilder withJarFileNameNormalizer(JarFileNameNormalizer jarFileNameNormalizer) {
		this.jarFileNameNormalizer = jarFileNameNormalizer;
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
		return new JarFileLoader(classLoader, classDefLoader, moduleInfoLoader, jarFileNameNormalizer, repository);
	}

	public ClasspathLoader buildClasspathLoader() {
		JarFileLoader jarFileLoader = buildJarFileLoader();
		WarFileLoader warFileLoader = new WarFileLoader(jarFileLoader);
		return new ClasspathLoader(jarFileLoader, warFileLoader, parentClassLoader, strategy);
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
