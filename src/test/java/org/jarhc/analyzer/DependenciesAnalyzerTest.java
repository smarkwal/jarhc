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

package org.jarhc.analyzer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.jarhc.artifacts.Artifact;
import org.jarhc.artifacts.Repository;
import org.jarhc.artifacts.RepositoryException;
import org.jarhc.java.ClassLoaderStrategy;
import org.jarhc.model.Classpath;
import org.jarhc.model.JarFile;
import org.jarhc.pom.PomUtils;
import org.jarhc.report.ReportSection;
import org.jarhc.report.ReportTable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DependenciesAnalyzerTest {

	private final Repository repository = mock(Repository.class);

	@BeforeEach
	void setUp() throws RepositoryException {

		Artifact artifactWithDeps = new Artifact("group:lib-with-deps:1.0:jar");
		Artifact artifactNoDeps = new Artifact("group:lib-no-deps:1.0:jar");
		Artifact artifactNoPom = new Artifact("group:lib-no-pom:1.0:jar");
		Artifact artifactRepoError = new Artifact("group:lib-repo-error:1.0:jar");

		when(repository.findArtifact("checksum-with-deps")).thenReturn(Optional.of(artifactWithDeps));
		when(repository.findArtifact("checksum-no-deps")).thenReturn(Optional.of(artifactNoDeps));
		when(repository.findArtifact("checksum-no-pom")).thenReturn(Optional.of(artifactNoPom));
		when(repository.findArtifact("checksum-repo-error")).thenReturn(Optional.of(artifactRepoError));
		when(repository.findArtifact("checksum-unknown")).thenReturn(Optional.empty());

		when(repository.downloadArtifact(artifactWithDeps.withType("pom"))).thenReturn(generatePom(artifactWithDeps, 3));
		when(repository.downloadArtifact(artifactNoDeps.withType("pom"))).thenReturn(generatePom(artifactNoDeps, 0));
		when(repository.downloadArtifact(artifactNoPom.withType("pom"))).thenReturn(Optional.empty());
		when(repository.downloadArtifact(artifactRepoError.withType("pom"))).thenThrow(new RepositoryException("test"));

	}

	@Test
	void analyze() {

		// prepare
		List<JarFile> jarFiles = new ArrayList<>();
		jarFiles.add(JarFile.withName("lib-with-deps.jar").withChecksum("checksum-with-deps").build());
		jarFiles.add(JarFile.withName("lib-no-deps.jar").withChecksum("checksum-no-deps").build());
		jarFiles.add(JarFile.withName("lib-no-pom.jar").withChecksum("checksum-no-pom").build());
		jarFiles.add(JarFile.withName("lib-repo-error.jar").withChecksum("checksum-repo-error").build());
		jarFiles.add(JarFile.withName("lib-unknown.jar").withChecksum("checksum-unknown").build());
		Classpath classpath = new Classpath(jarFiles, null, ClassLoaderStrategy.ParentFirst);

		DependenciesAnalyzer analyzer = new DependenciesAnalyzer(repository);

		// test
		ReportSection section = analyzer.analyze(classpath);

		// assert
		assertNotNull(section);
		assertEquals("Dependencies", section.getTitle());
		assertEquals("Direct and transitive dependencies as declared in POM file.", section.getDescription());
		assertEquals("Dependencies", section.getId());

		List<Object> contents = section.getContent();
		assertEquals(1, contents.size());
		Object content = contents.get(0);
		assertTrue(content instanceof ReportTable);
		ReportTable table = (ReportTable) content;

		String[] columns = table.getColumns();
		assertEquals(4, columns.length);
		assertEquals("JAR file", columns[0]);
		assertEquals("Maven coordinates", columns[1]);
		assertEquals("Direct dependencies", columns[2]);
		assertEquals("Transitive dependencies", columns[3]);

		List<String[]> rows = table.getRows();
		assertEquals(5, rows.size());

		String[] values = rows.get(0);
		assertEquals("lib-with-deps.jar", values[0]);
		assertEquals("group:lib-with-deps:1.0:jar", values[1]);
		assertEquals("group:lib-with-deps-1:1.0 (provided, optional)\ngroup:lib-with-deps-2:1.0 (runtime)", values[2]);
		assertEquals("[todo]", values[3]);

		values = rows.get(1);
		assertEquals("lib-no-deps.jar", values[0]);
		assertEquals("group:lib-no-deps:1.0:jar", values[1]);
		assertEquals("[none]", values[2]);
		assertEquals("[todo]", values[3]);

		values = rows.get(2);
		assertEquals("lib-no-pom.jar", values[0]);
		assertEquals("group:lib-no-pom:1.0:jar", values[1]);
		assertEquals("[error]", values[2]);
		assertEquals("[todo]", values[3]);

		values = rows.get(3);
		assertEquals("lib-repo-error.jar", values[0]);
		assertEquals("group:lib-repo-error:1.0:jar", values[1]);
		assertEquals("[error]", values[2]);
		assertEquals("[todo]", values[3]);

		values = rows.get(4);
		assertEquals("lib-unknown.jar", values[0]);
		assertEquals("[unknown]", values[1]);
		assertEquals("[unknown]", values[2]);
		assertEquals("[todo]", values[3]);

	}

	private Optional<InputStream> generatePom(Artifact artifact, int dependencies) {
		String xml = PomUtils.generatePomXml(artifact, dependencies);
		ByteArrayInputStream stream = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
		return Optional.of(stream);
	}

}