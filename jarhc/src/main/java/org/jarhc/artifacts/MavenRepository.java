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
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.collection.DependencyCollectionContext;
import org.eclipse.aether.collection.DependencyCollectionException;
import org.eclipse.aether.collection.DependencySelector;
import org.eclipse.aether.graph.DependencyNode;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.LocalRepositoryManager;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.aether.resolution.VersionRangeRequest;
import org.eclipse.aether.resolution.VersionRangeResolutionException;
import org.eclipse.aether.resolution.VersionRangeResult;
import org.eclipse.aether.supplier.RepositorySystemSupplier;
import org.eclipse.aether.transfer.ArtifactNotFoundException;
import org.eclipse.aether.util.graph.selector.AndDependencySelector;
import org.eclipse.aether.util.graph.selector.ExclusionDependencySelector;
import org.eclipse.aether.util.graph.selector.OptionalDependencySelector;
import org.eclipse.aether.util.graph.selector.ScopeDependencySelector;
import org.eclipse.aether.util.graph.visitor.PreorderNodeListGenerator;
import org.eclipse.aether.util.repository.AuthenticationBuilder;
import org.eclipse.aether.version.Version;
import org.jarhc.pom.Dependency;
import org.jarhc.pom.Scope;
import org.jarhc.utils.JarHcException;
import org.slf4j.Logger;

public class MavenRepository implements Repository {

	private static final String MAVEN_ERROR = "Maven error";
	private static final String IO_ERROR = "I/O error";

	private final ArtifactFinder artifactFinder;
	private final Logger logger;
	private final RemoteRepository central;
	private final RepositorySystem repoSystem;
	private final RepositorySystemSession session;

	public MavenRepository(int javaVersion, Settings settings, String dataPath, ArtifactFinder artifactFinder, Logger logger) {

		String repositoryUrl = settings.getRepositoryUrl();

		// check if code is running in a test where only local repositories are allowed
		String localOnly = System.getProperty("jarhc.test.maven.local-only", "false");
		if (localOnly.equals("true")) {
			if (!repositoryUrl.startsWith("http://localhost") && !repositoryUrl.startsWith("file://")) {
				throw new JarHcException("Non-local repository URL: " + repositoryUrl);
			}
		}

		RemoteRepository.Builder repoBuilder = new RemoteRepository.Builder("central", "default", repositoryUrl);

		// add authentication (if username or password is set)
		String repositoryUsername = settings.getRepositoryUsername();
		String repositoryPassword = settings.getRepositoryPassword();
		if (repositoryUsername != null || repositoryPassword != null) {
			AuthenticationBuilder authBuilder = new AuthenticationBuilder();
			if (repositoryUsername != null) {
				authBuilder.addUsername(repositoryUsername);
			}
			if (repositoryPassword != null) {
				authBuilder.addPassword(repositoryPassword);
			}
			repoBuilder.setAuthentication(authBuilder.build());
		}

		this.central = repoBuilder.build();
		this.artifactFinder = artifactFinder;
		this.logger = logger;
		this.repoSystem = newRepositorySystem();
		this.session = newSession(repoSystem, javaVersion, dataPath);
	}

	@Override
	public List<Artifact> findArtifacts(String checksum) throws RepositoryException {
		logger.debug("Find artifact: {}", checksum);
		return artifactFinder.findArtifacts(checksum);
	}

	@Override
	public Optional<InputStream> downloadArtifact(Artifact artifact) throws RepositoryException {
		logger.debug("Download artifact: {}", artifact);

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
				return Optional.empty();
			}
			throw new RepositoryException(MAVEN_ERROR, e);
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
			throw new RepositoryException(IO_ERROR, e);
		}
	}

	public List<ArtifactVersion> getVersions(String groupId, String artifactId) throws RepositoryException {
		logger.debug("Get versions: {}:{}", groupId, artifactId);

		// prepare a request to fetch all versions of the artifact
		DefaultArtifact artifact = new DefaultArtifact(groupId, artifactId, "jar", "[0,)");
		VersionRangeRequest request = new VersionRangeRequest();
		request.setArtifact(artifact);
		request.addRepository(central);

		try {
			// fetch all versions of the artifact (list is already sorted)
			VersionRangeResult versionResult = repoSystem.resolveVersionRange(session, request);
			// return list of versions
			return versionResult.getVersions().stream().map(Version::toString).map(ArtifactVersion::new).collect(Collectors.toList());
		} catch (VersionRangeResolutionException e) {
			throw new RepositoryException(MAVEN_ERROR, e);
		}
	}

	@Override
	public List<Dependency> getDependencies(Artifact artifact) throws RepositoryException {
		logger.debug("Get dependencies: {}", artifact);

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
			throw new RepositoryException(MAVEN_ERROR, e);
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
		RepositorySystemSupplier repositorySystemSupplier = new RepositorySystemSupplier();
		return repositorySystemSupplier.get();
	}

	private static RepositorySystemSession newSession(RepositorySystem repoSystem, int javaVersion, String dataPath) {
		DefaultRepositorySystemSession session = MavenRepositorySystemUtils.newSession();

		if (dataPath != null) {
			File basedir = new File(dataPath, "maven");
			LocalRepository localRepo = new LocalRepository(basedir);
			LocalRepositoryManager localRepoManager = repoSystem.newLocalRepositoryManager(session, localRepo);
			session.setLocalRepositoryManager(localRepoManager);
		}

		// do not ignore missing or invalid artifact descriptors
		// session.setArtifactDescriptorPolicy(new SimpleArtifactDescriptorPolicy(false, false));

		// TODO: optimize filter?
		//  - select transitive dependencies, except of optional and/or provided dependencies?
		DependencySelector dependencySelector = new AndDependencySelector(
				new TransitiveDependencySelector(2),
				new ScopeDependencySelector("test"),
				new OptionalDependencySelector(),
				new ExclusionDependencySelector()
		);
		session.setDependencySelector(dependencySelector);

		// clear all system and config properties which are already set
		Map<String, String> systemProperties = session.getSystemProperties();
		List<String> propertyNames = new ArrayList<>(systemProperties.keySet());
		for (String propertyName : propertyNames) {
			session.setSystemProperty(propertyName, null);
			session.setConfigProperty(propertyName, null);
		}

		// set Java version
		if (javaVersion < 9) { // Java 1.5 - 1.8
			session.setSystemProperty("java.version", String.format("1.%d.0", javaVersion));
		} else { // Java 9 and greater
			session.setSystemProperty("java.version", String.format("%d.0.0", javaVersion));
		}

		// set OS information (used by Maven to activate profiles)
		// TODO: make this configurable?
		session.setSystemProperty("os.name", System.getProperty("os.name", "Linux"));
		session.setSystemProperty("os.arch", System.getProperty("os.arch", "amd64"));
		session.setSystemProperty("os.version", System.getProperty("os.version", "6.11"));

		return session;
	}

	private static class TransitiveDependencySelector implements DependencySelector {

		private final int depth;

		public TransitiveDependencySelector(int depth) {
			this.depth = depth;
		}

		@Override
		public boolean selectDependency(org.eclipse.aether.graph.Dependency dependency) {
			String scope = dependency.getScope();
			if (scope.equals("test")) {
				// ignore test dependency
				return false;
			} else if (depth <= 0) {
				// ignore transitive dependency
				return false;
			} else {
				// accept dependency
				return true;
			}
		}

		@Override
		public DependencySelector deriveChildSelector(DependencyCollectionContext context) {
			return new TransitiveDependencySelector(depth - 1);
		}

	}

	/**
	 * Settings for Maven repository.
	 */
	public interface Settings {

		/**
		 * Get the URL of the Maven repository.
		 *
		 * @return URL of Maven repository.
		 */
		String getRepositoryUrl();

		/**
		 * Get the username for the Maven repository.
		 *
		 * @return Username for Maven repository (may be null).
		 */
		String getRepositoryUsername();

		/**
		 * Get the password for the Maven repository.
		 *
		 * @return Password for Maven repository (may be null).
		 */
		String getRepositoryPassword();

	}

}
