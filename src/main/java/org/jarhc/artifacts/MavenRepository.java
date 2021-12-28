/*
 * Copyright 2021 Stephan Markwalder
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
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.collection.DependencyCollectionContext;
import org.eclipse.aether.collection.DependencyCollectionException;
import org.eclipse.aether.collection.DependencySelector;
import org.eclipse.aether.connector.basic.BasicRepositoryConnectorFactory;
import org.eclipse.aether.graph.DependencyNode;
import org.eclipse.aether.impl.DefaultServiceLocator;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.LocalRepositoryManager;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory;
import org.eclipse.aether.spi.connector.transport.TransporterFactory;
import org.eclipse.aether.transfer.ArtifactNotFoundException;
import org.eclipse.aether.transport.http.HttpTransporterFactory;
import org.eclipse.aether.util.graph.selector.AndDependencySelector;
import org.eclipse.aether.util.graph.selector.ExclusionDependencySelector;
import org.eclipse.aether.util.graph.selector.OptionalDependencySelector;
import org.eclipse.aether.util.graph.selector.ScopeDependencySelector;
import org.eclipse.aether.util.graph.visitor.PreorderNodeListGenerator;
import org.jarhc.pom.Dependency;
import org.jarhc.pom.Scope;
import org.slf4j.Logger;

public class MavenRepository implements Repository {

	private static final RemoteRepository central = new RemoteRepository.Builder("central", "default", "https://repo1.maven.org/maven2/").build();

	private final MavenArtifactFinder artifactFinder = new MavenArtifactFinder();

	private final Logger logger;
	private final RepositorySystem repoSystem;
	private final RepositorySystemSession session;

	public MavenRepository(String dataPath, Logger logger) {
		this.logger = logger;
		this.repoSystem = newRepositorySystem();
		this.session = newSession(repoSystem, dataPath);
	}

	@Override
	public Optional<Artifact> findArtifact(String checksum) throws RepositoryException {
		return artifactFinder.findArtifact(checksum);
	}

	@Override
	public Optional<InputStream> downloadArtifact(Artifact artifact) throws RepositoryException {

		String groupId = artifact.getGroupId();
		String artifactId = artifact.getArtifactId();
		String version = artifact.getVersion();
		String type = artifact.getType();

		ArtifactResult result;
		try {
			result = resolveArtifact(groupId, artifactId, version, type);
		} catch (ArtifactResolutionException e) {
			Throwable cause = e.getCause();
			if (cause instanceof ArtifactNotFoundException) {
				// TODO: log exception
				return Optional.empty();
			}
			throw new RepositoryException("Maven error", e);
		}

		// TODO: what does "missing" and "resolved" mean?
		if (result.isMissing()) {
			return Optional.empty();
		} else if (!result.isResolved()) {
			return Optional.empty();
		}

		File file = result.getArtifact().getFile();
		try {
			InputStream stream = new FileInputStream(file);
			return Optional.of(stream);
		} catch (FileNotFoundException e) {
			throw new RepositoryException("I/O error", e);
		}
	}

	@Override
	public List<Dependency> getDependencies(Artifact artifact) throws RepositoryException {

		String coordinates = artifact.toCoordinates();

		List<Dependency> result = new ArrayList<>();
		try {

			List<org.eclipse.aether.graph.Dependency> mavenDependencies = getDependencies(coordinates);
			for (org.eclipse.aether.graph.Dependency mavenDependency : mavenDependencies) {
				org.eclipse.aether.artifact.Artifact mavenArtifact = mavenDependency.getArtifact();
				String groupId = mavenArtifact.getGroupId();
				String artifactId = mavenArtifact.getArtifactId();
				String version = mavenArtifact.getVersion();
				String scope = mavenDependency.getScope();
				boolean optional = mavenDependency.isOptional();

				Dependency dependency = new Dependency(groupId, artifactId, version, Scope.valueOf(scope.toUpperCase()), optional);
				result.add(dependency);
			}

		} catch (Exception e) {
			// TODO: exception handling
			throw new RepositoryException("Maven error", e);
		}

		return result;
	}

	private List<org.eclipse.aether.graph.Dependency> getDependencies(String coordinates) throws DependencyCollectionException {

		DefaultArtifact rootArtifact = new DefaultArtifact(coordinates);
		org.eclipse.aether.graph.Dependency rootDependency = new org.eclipse.aether.graph.Dependency(rootArtifact, "provided");

		CollectRequest collectRequest = new CollectRequest();
		collectRequest.setRoot(rootDependency);
		collectRequest.addRepository(central);
		DependencyNode rootNode = repoSystem.collectDependencies(session, collectRequest).getRoot();

		// DependencyRequest dependencyRequest = new DependencyRequest();
		// dependencyRequest.setRoot(rootNode);
		// repoSystem.resolveDependencies(session, dependencyRequest);

		// TODO: disable conflict resolution?

		PreorderNodeListGenerator dependencyVisitor = new PreorderNodeListGenerator();
		rootNode.accept(dependencyVisitor);
		List<org.eclipse.aether.graph.Dependency> mavenDependencies = dependencyVisitor.getDependencies(true);

		// remove root dependency
		mavenDependencies.remove(0);

		return mavenDependencies;
	}

	private ArtifactResult resolveArtifact(String groupId, String artifactId, String version, String type) throws ArtifactResolutionException {
		DefaultArtifact artifact = new DefaultArtifact(groupId, artifactId, type, version);
		ArtifactRequest request = new ArtifactRequest();
		request.setArtifact(artifact);
		request.addRepository(central);
		return repoSystem.resolveArtifact(session, request);
	}

	private static RepositorySystem newRepositorySystem() {
		DefaultServiceLocator locator = MavenRepositorySystemUtils.newServiceLocator();
		locator.addService(RepositoryConnectorFactory.class, BasicRepositoryConnectorFactory.class);
		//locator.addService(TransporterFactory.class, FileTransporterFactory.class);
		locator.addService(TransporterFactory.class, HttpTransporterFactory.class);
		return locator.getService(RepositorySystem.class);
	}

	private static RepositorySystemSession newSession(RepositorySystem repoSystem, String dataPath) {
		DefaultRepositorySystemSession session = MavenRepositorySystemUtils.newSession();

		if (dataPath != null) {
			File basedir = new File(dataPath, "maven");
			LocalRepository localRepo = new LocalRepository(basedir);
			LocalRepositoryManager localRepoManager = repoSystem.newLocalRepositoryManager(session, localRepo);
			session.setLocalRepositoryManager(localRepoManager);
		}

		// TODO: optimize filter?
		//  - select transitive dependencies, except of optional and/or provided dependencies?
		DependencySelector dependencySelector = new AndDependencySelector(new TransitiveDependencySelector(2), new ScopeDependencySelector("test"), new OptionalDependencySelector(), new ExclusionDependencySelector()); //
		session.setDependencySelector(dependencySelector);

		return session;
	}

	private static class TransitiveDependencySelector implements DependencySelector {

		private final int depth;

		public TransitiveDependencySelector(int depth) {
			this.depth = depth;
		}

		@Override
		public boolean selectDependency(org.eclipse.aether.graph.Dependency dependency) {
			if (dependency.getScope().equals("test")) {
				// logger.debug(depth + ": " + dependency.toString() + " [ignore test]");
				return false;
			} else if (depth <= 0) {
				// logger.debug(depth + ": " + dependency.toString() + " [ignore depth]");
				return false;
			} else {
				// logger.debug(depth + ": " + dependency.toString() + " [accept]");
				return true;
			}
		}

		@Override
		public DependencySelector deriveChildSelector(DependencyCollectionContext context) {
			return new TransitiveDependencySelector(depth - 1);
		}

	}

}
