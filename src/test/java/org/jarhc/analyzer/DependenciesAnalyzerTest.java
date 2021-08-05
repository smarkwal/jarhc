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

import static org.jarhc.pom.PomUtils.generateDependencies;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import org.jarhc.artifacts.Artifact;
import org.jarhc.artifacts.Repository;
import org.jarhc.artifacts.RepositoryException;
import org.jarhc.java.ClassLoaderStrategy;
import org.jarhc.model.Classpath;
import org.jarhc.model.JarFile;
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

		when(repository.getDependencies(artifactWithDeps)).thenReturn(generateDependencies(artifactWithDeps, 5));
		when(repository.getDependencies(artifactNoDeps)).thenReturn(generateDependencies(artifactNoDeps, 0));
		when(repository.getDependencies(artifactNoPom)).thenThrow(new RepositoryException("test"));
		when(repository.getDependencies(artifactRepoError)).thenThrow(new RepositoryException("test"));

	}

	@Test
	void analyze() {

		// prepare: provided
		List<JarFile> jarFiles = new ArrayList<>();
		jarFiles.add(JarFile.withName("lib-with-deps-1.jar").withCoordinates("group:lib-with-deps-1:1.0:jar").build());
		jarFiles.add(JarFile.withName("lib-with-deps-2.jar").withCoordinates("group:lib-with-deps-2:1.0:jar").build());
		jarFiles.add(JarFile.withName("lib-with-deps-3.jar").withCoordinates("group:lib-with-deps-3:1.0:jar").build());
		jarFiles.add(JarFile.withName("lib-with-deps-4.jar").withCoordinates("group:lib-with-deps-4:1.1:jar").build());
		Classpath provided = new Classpath(jarFiles, null, ClassLoaderStrategy.ParentLast);

		// prepare: classpath
		jarFiles = new ArrayList<>();
		jarFiles.add(JarFile.withName("lib-with-deps.jar").withCoordinates("group:lib-with-deps:1.0:jar").build());
		jarFiles.add(JarFile.withName("lib-no-deps.jar").withCoordinates("group:lib-no-deps:1.0:jar").build());
		jarFiles.add(JarFile.withName("lib-no-pom.jar").withCoordinates("group:lib-no-pom:1.0:jar").build());
		jarFiles.add(JarFile.withName("lib-repo-error.jar").withCoordinates("group:lib-repo-error:1.0:jar").build());
		jarFiles.add(JarFile.withName("lib-unknown.jar").withCoordinates(null).build());
		Classpath classpath = new Classpath(jarFiles, provided, ClassLoaderStrategy.ParentLast);

		DependenciesAnalyzer analyzer = new DependenciesAnalyzer(repository);

		// test
		ReportSection section = analyzer.analyze(classpath);

		// assert
		assertNotNull(section);
		assertEquals("Dependencies", section.getTitle());
		assertEquals("Dependencies as declared in POM file.", section.getDescription());
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
		assertEquals("Status", columns[3]);

		List<String[]> rows = table.getRows();
		assertEquals(5, rows.size());

		String[] values = rows.get(0);
		assertEquals("lib-with-deps.jar", values[0]);
		assertEquals("group:lib-with-deps:1.0:jar", values[1]);
		assertEquals("group:lib-with-deps-1:1.0 (provided, optional)\ngroup:lib-with-deps-2:1.0 (runtime)\ngroup:lib-with-deps-4:1.0 (system)\ngroup:lib-with-deps-5:1.0 (import, optional)", values[2]);
		assertEquals("OK\nOK\nOK (version 1.1)\nUnsatisfied", values[3]);

		values = rows.get(1);
		assertEquals("lib-no-deps.jar", values[0]);
		assertEquals("group:lib-no-deps:1.0:jar", values[1]);
		assertEquals("[none]", values[2]);
		assertEquals("", values[3]);

		values = rows.get(2);
		assertEquals("lib-no-pom.jar", values[0]);
		assertEquals("group:lib-no-pom:1.0:jar", values[1]);
		assertEquals("[error]", values[2]);
		assertEquals("", values[3]);

		values = rows.get(3);
		assertEquals("lib-repo-error.jar", values[0]);
		assertEquals("group:lib-repo-error:1.0:jar", values[1]);
		assertEquals("[error]", values[2]);
		assertEquals("", values[3]);

		values = rows.get(4);
		assertEquals("lib-unknown.jar", values[0]);
		assertEquals("[unknown]", values[1]);
		assertEquals("[unknown]", values[2]);
		assertEquals("", values[3]);

	}

}