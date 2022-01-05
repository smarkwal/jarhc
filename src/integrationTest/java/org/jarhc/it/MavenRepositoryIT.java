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

package org.jarhc.it;

import static org.jarhc.test.log.LoggerAssertions.assertLogger;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import org.jarhc.TestUtils;
import org.jarhc.artifacts.Artifact;
import org.jarhc.artifacts.MavenRepository;
import org.jarhc.artifacts.RepositoryException;
import org.jarhc.pom.Dependency;
import org.jarhc.pom.Scope;
import org.jarhc.test.log.LoggerBuilder;
import org.jarhc.utils.IOUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.Logger;

@SuppressWarnings("NewClassNamingConvention")
class MavenRepositoryIT {

	private final Logger logger = LoggerBuilder.collect(MavenRepository.class);

	private MavenRepository repository;

	@BeforeEach
	void setUp(@TempDir Path tempDir) {
		repository = new MavenRepository(TestUtils.getFileRepositoryURL(), tempDir.toString(), null, logger);
	}

	@AfterEach
	void tearDown() {
		assertLogger(logger).isEmpty();
	}

	@Test
	void test_downloadArtifact_CommonsIO() throws RepositoryException, IOException {

		Artifact artifact = new Artifact("commons-io", "commons-io", "2.6", "jar");

		// test
		Optional<InputStream> stream = repository.downloadArtifact(artifact);

		// assert
		assertTrue(stream.isPresent());
		byte[] data = IOUtils.toByteArray(stream.get());
		assertEquals(214788, data.length);
		assertLogger(logger).hasDebug("Download artifact: commons-io:commons-io:2.6:jar");

	}

	@Test
	void test_downloadArtifact_CommonsIO_POM() throws RepositoryException, IOException {

		Artifact artifact = new Artifact("commons-io", "commons-io", "2.6", "pom");

		// test
		Optional<InputStream> stream = repository.downloadArtifact(artifact);

		// assert
		assertTrue(stream.isPresent());
		byte[] data = IOUtils.toByteArray(stream.get());
		assertEquals(14256, data.length);
		assertLogger(logger).hasDebug("Download artifact: commons-io:commons-io:2.6:pom");

	}

	@Test
	void test_downloadArtifact_CommonsCodec() throws RepositoryException, IOException {

		Artifact artifact = new Artifact("commons-codec", "commons-codec", "1.11", "jar");

		// test
		Optional<InputStream> stream = repository.downloadArtifact(artifact);

		// assert
		assertTrue(stream.isPresent());
		byte[] data = IOUtils.toByteArray(stream.get());
		assertEquals(335042, data.length);
		assertLogger(logger).hasDebug("Download artifact: commons-codec:commons-codec:1.11:jar");

	}

	@Test
	void test_downloadArtifact_ASM() throws RepositoryException, IOException {

		Artifact artifact = new Artifact("org.ow2.asm", "asm", "7.0", "jar");

		// test
		Optional<InputStream> stream = repository.downloadArtifact(artifact);

		// assert
		assertTrue(stream.isPresent());
		byte[] data = IOUtils.toByteArray(stream.get());
		assertEquals(113676, data.length);
		assertLogger(logger).hasDebug("Download artifact: org.ow2.asm:asm:7.0:jar");

	}

	@Test
	void test_downloadArtifact_TestJettyWebApp() throws RepositoryException, IOException {

		Artifact artifact = new Artifact("org.eclipse.jetty", "test-jetty-webapp", "9.4.20.v20190813", "war");

		// test
		Optional<InputStream> stream = repository.downloadArtifact(artifact);

		// assert
		assertTrue(stream.isPresent());
		byte[] data = IOUtils.toByteArray(stream.get());
		assertEquals(1213592, data.length);
		assertLogger(logger).hasDebug("Download artifact: org.eclipse.jetty:test-jetty-webapp:9.4.20.v20190813:war");

	}

	@Test
	void test_downloadArtifact_TestJettyWebApp_POM() throws RepositoryException, IOException {

		Artifact artifact = new Artifact("org.eclipse.jetty", "test-jetty-webapp", "9.4.20.v20190813", "pom");

		// test
		Optional<InputStream> stream = repository.downloadArtifact(artifact);

		// assert
		assertTrue(stream.isPresent());
		byte[] data = IOUtils.toByteArray(stream.get());
		assertEquals(8851, data.length);
		assertLogger(logger).hasDebug("Download artifact: org.eclipse.jetty:test-jetty-webapp:9.4.20.v20190813:pom");

	}

	@Test
	void test_downloadArtifact_notFound() throws RepositoryException {

		Artifact artifact = new Artifact("unknown", "unknown", "1.0", "jar");

		// test
		Optional<InputStream> stream = repository.downloadArtifact(artifact);

		// assert
		assertFalse(stream.isPresent());
		assertLogger(logger).hasDebug("Download artifact: unknown:unknown:1.0:jar");

	}

	@Test
	void test_getDependencies_AsmCommons() throws RepositoryException {

		// prepare
		Artifact artifact = new Artifact("org.ow2.asm:asm-commons:7.0");

		// test
		List<Dependency> dependencies = repository.getDependencies(artifact);

		// assert: https://mvnrepository.com/artifact/org.ow2.asm/asm-commons/7.0
		assertEquals(3, dependencies.size());
		assertTrue(dependencies.contains(new Dependency("org.ow2.asm", "asm", "7.0", Scope.COMPILE, false)));
		assertTrue(dependencies.contains(new Dependency("org.ow2.asm", "asm-tree", "7.0", Scope.COMPILE, false)));
		assertTrue(dependencies.contains(new Dependency("org.ow2.asm", "asm-analysis", "7.0", Scope.COMPILE, false)));
		assertLogger(logger).hasDebug("Get dependencies: org.ow2.asm:asm-commons:7.0:jar");
	}

	@Test
	void test_getDependencies_SpringWeb() throws RepositoryException {

		// prepare
		Artifact artifact = new Artifact("org.springframework:spring-web:5.3.9");

		// test
		List<Dependency> dependencies = repository.getDependencies(artifact);

		// assert: https://mvnrepository.com/artifact/org.springframework/spring-web/5.3.9
		assertEquals(2, dependencies.size());
		assertTrue(dependencies.contains(new Dependency("org.springframework", "spring-beans", "5.3.9", Scope.COMPILE, false)));
		assertTrue(dependencies.contains(new Dependency("org.springframework", "spring-core", "5.3.9", Scope.COMPILE, false)));
		assertLogger(logger).hasDebug("Get dependencies: org.springframework:spring-web:5.3.9:jar");
	}

	@Test
	void test_getDependencies_CamelCore() throws RepositoryException {

		// prepare
		Artifact artifact = new Artifact("org.apache.camel:camel-core:2.17.7");

		// test
		List<Dependency> dependencies = repository.getDependencies(artifact);

		// assert: https://mvnrepository.com/artifact/org.apache.camel/camel-core/2.17.7
		assertEquals(6, dependencies.size());
		assertTrue(dependencies.contains(new Dependency("com.sun.xml.bind", "jaxb-core", "2.2.11", Scope.COMPILE, false)));
		assertTrue(dependencies.contains(new Dependency("com.sun.xml.bind", "jaxb-impl", "2.2.11", Scope.COMPILE, false)));
		assertTrue(dependencies.contains(new Dependency("org.apache.camel", "spi-annotations", "2.17.7", Scope.COMPILE, true)));
		assertTrue(dependencies.contains(new Dependency("org.slf4j", "slf4j-api", "1.7.21", Scope.COMPILE, false)));
		assertTrue(dependencies.contains(new Dependency("org.apache.camel", "apt", "2.17.7", Scope.PROVIDED, false)));
		assertTrue(dependencies.contains(new Dependency("org.osgi", "org.osgi.core", "4.3.1", Scope.PROVIDED, true)));
		assertLogger(logger).hasDebug("Get dependencies: org.apache.camel:camel-core:2.17.7:jar");
	}

	@Test
	void test_getDependencies_CamelJDBC() throws RepositoryException {

		// prepare
		Artifact artifact = new Artifact("org.apache.camel:camel-jdbc:2.17.7");

		// test
		List<Dependency> dependencies = repository.getDependencies(artifact);

		// assert: https://mvnrepository.com/artifact/org.apache.camel/camel-jdbc/2.17.7
		//         parent: https://mvnrepository.com/artifact/org.apache.camel/components/2.17.7
		//         parent: https://mvnrepository.com/artifact/org.apache.camel/camel-parent/2.17.7
		//         parent: https://mvnrepository.com/artifact/org.apache.camel/camel/2.17.7
		assertEquals(4, dependencies.size());
		assertTrue(dependencies.contains(new Dependency("org.apache.camel:camel-core:2.17.7", Scope.COMPILE, false)));
		assertTrue(dependencies.contains(new Dependency("org.apache.camel", "apt", "2.17.7", Scope.PROVIDED, false)));
		assertTrue(dependencies.contains(new Dependency("com.sun.xml.bind", "jaxb-core", "2.2.11", Scope.COMPILE, false)));
		assertTrue(dependencies.contains(new Dependency("com.sun.xml.bind", "jaxb-impl", "2.2.11", Scope.COMPILE, false)));
		assertLogger(logger).hasDebug("Get dependencies: org.apache.camel:camel-jdbc:2.17.7:jar");
	}

	@Test
	void test_getDependencies_HazelcastClient() throws RepositoryException {

		// prepare
		Artifact artifact = new Artifact("com.hazelcast:hazelcast-client:3.11.2");

		// test
		List<Dependency> dependencies = repository.getDependencies(artifact);

		// assert: https://mvnrepository.com/artifact/com.hazelcast/hazelcast-client/3.11.2
		//         parent: https://mvnrepository.com/artifact/com.hazelcast/hazelcast-root/3.11.2
		assertEquals(7, dependencies.size());
		assertTrue(dependencies.contains(new Dependency("com.hazelcast:hazelcast:3.11.2", Scope.COMPILE, false)));
		assertTrue(dependencies.contains(new Dependency("com.hazelcast:hazelcast-aws:2.0.0", Scope.PROVIDED, true)));
		assertTrue(dependencies.contains(new Dependency("com.hazelcast:hazelcast-client-protocol:1.7.0", Scope.PROVIDED, false)));
		assertTrue(dependencies.contains(new Dependency("org.apache.logging.log4j:log4j-api:2.3", Scope.PROVIDED, true)));
		assertTrue(dependencies.contains(new Dependency("org.apache.logging.log4j:log4j-core:2.3", Scope.PROVIDED, true)));
		assertTrue(dependencies.contains(new Dependency("javax.cache:cache-api:1.1.0", Scope.PROVIDED, true)));
		assertTrue(dependencies.contains(new Dependency("com.google.code.findbugs:annotations:3.0.0", Scope.PROVIDED, true)));
		assertLogger(logger).hasDebug("Get dependencies: com.hazelcast:hazelcast-client:3.11.2:jar");
	}

	@Test
	void test_getDependencies_notFound() throws RepositoryException {

		// prepare
		Artifact artifact = new Artifact("net.markwalder:unknown:1.0");

		// test
		List<Dependency> dependencies = repository.getDependencies(artifact); // TODO: should this throw an exception?

		// assert
		assertEquals(0, dependencies.size());
		assertLogger(logger).hasDebug("Get dependencies: net.markwalder:unknown:1.0:jar");
	}

}