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

import static org.jarhc.TestUtils.assertValuesEquals;
import static org.jarhc.pom.PomUtils.generateDependencies;
import static org.jarhc.test.log.LoggerAssertions.assertLogger;
import static org.jarhc.utils.StringUtils.joinLines;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
import org.jarhc.test.ClasspathBuilder;
import org.jarhc.test.log.LoggerBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

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
		jarFiles.add(JarFile.withName("lib-with-deps-1.jar").withArtifact("group:lib-with-deps-1:1.0:jar").build());
		jarFiles.add(JarFile.withName("lib-with-deps-2.jar").withArtifact("group:lib-with-deps-2:1.0:jar").build());
		jarFiles.add(JarFile.withName("lib-with-deps-3.jar").withArtifact("group:lib-with-deps-3:1.0:jar").build());
		jarFiles.add(JarFile.withName("lib-with-deps-4.jar").withArtifact("group:lib-with-deps-4:1.1:jar").build());
		Classpath provided = new Classpath(jarFiles, null, ClassLoaderStrategy.ParentLast);

		// prepare: classpath
		jarFiles = new ArrayList<>();
		jarFiles.add(JarFile.withName("lib-with-deps.jar").withArtifact("group:lib-with-deps:1.0:jar").build());
		jarFiles.add(JarFile.withName("lib-no-deps.jar").withArtifact("group:lib-no-deps:1.0:jar").build());
		jarFiles.add(JarFile.withName("lib-no-pom.jar").withArtifact("group:lib-no-pom:1.0:jar").build());
		jarFiles.add(JarFile.withName("lib-repo-error.jar").withArtifact("group:lib-repo-error:1.0:jar").build());
		jarFiles.add(JarFile.withName("lib-unknown.jar").withArtifacts(List.of()).build());
		Classpath classpath = new Classpath(jarFiles, provided, ClassLoaderStrategy.ParentLast);

		Logger logger = LoggerBuilder.collect(DependenciesAnalyzer.class);
		DependenciesAnalyzer analyzer = new DependenciesAnalyzer(repository, logger);

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
		assertInstanceOf(ReportTable.class, content);
		ReportTable table = (ReportTable) content;

		String[] columns = table.getColumns();
		assertValuesEquals(columns, "Artifact", "Uses", "Used by", "Maven coordinates", "Direct dependencies", "Status");

		List<String[]> rows = table.getRows();
		assertEquals(5, rows.size());

		assertValuesEquals(rows.get(0), "lib-with-deps", "[none]", "[none]", "[[group:lib-with-deps:1.0]]", "[[group:lib-with-deps-1:1.0]] (provided, optional)\n[[group:lib-with-deps-2:1.0]] (runtime)\n[[group:lib-with-deps-4:1.0]] (system)\n[[group:lib-with-deps-5:1.0]] (import, optional)", "OK\nOK\nOK (version 1.1)\nUnsatisfied");
		assertValuesEquals(rows.get(1), "lib-no-deps", "[none]", "[none]", "[[group:lib-no-deps:1.0]]", "[none]", "");
		assertValuesEquals(rows.get(2), "lib-no-pom", "[none]", "[none]", "[[group:lib-no-pom:1.0]]", "[error]", "");
		assertValuesEquals(rows.get(3), "lib-repo-error", "[none]", "[none]", "[[group:lib-repo-error:1.0]]", "[error]", "");
		assertValuesEquals(rows.get(4), "lib-unknown", "[none]", "[none]", "[unknown]", "[unknown]", "");

		assertLogger(logger).inAnyOrder()
				.hasError("Resolver error for artifact: group:lib-no-pom:1.0", new RepositoryException("test"))
				.hasError("Resolver error for artifact: group:lib-repo-error:1.0", new RepositoryException("test"))
				.isEmpty();
	}

	@Test
	void analyze_UsesUsedBy() {

		// prepare
		Classpath classpath = ClasspathBuilder.create(null)
				.addJarFile("a.jar")
				.addClassDef("a.A").addClassRef("b.B1").addClassRef("c.C").addClassRef("x.X")
				.addJarFile("b.jar")
				.addClassDef("b.B1")
				.addClassDef("b.B2").addClassRef("c.C").addClassRef("b.B1")
				.addJarFile("c.jar")
				.addClassDef("c.C")
				.addJarFile("d.jar")
				.build();

		// test
		Logger logger = LoggerBuilder.collect(DependenciesAnalyzer.class);
		DependenciesAnalyzer analyzer = new DependenciesAnalyzer(repository, logger);
		ReportSection section = analyzer.analyze(classpath);

		// assert
		assertNotNull(section);
		assertEquals("Dependencies", section.getTitle());
		assertEquals("Dependencies as declared in POM file.", section.getDescription());
		assertEquals("Dependencies", section.getId());
		assertEquals(1, section.getContent().size());
		assertInstanceOf(ReportTable.class, section.getContent().get(0));

		ReportTable table = (ReportTable) section.getContent().get(0);

		String[] columns = table.getColumns();
		assertValuesEquals(columns, "Artifact", "Uses", "Used by", "Maven coordinates", "Direct dependencies", "Status");

		List<String[]> rows = table.getRows();
		assertEquals(4, rows.size());
		assertValuesEquals(rows.get(0), "a", joinLines("b", "c"), "[none]", "[unknown]", "[unknown]", "");
		assertValuesEquals(rows.get(1), "b", "c", "a", "[unknown]", "[unknown]", "");
		assertValuesEquals(rows.get(2), "c", "[none]", joinLines("a", "b"), "[unknown]", "[unknown]", "");
		assertValuesEquals(rows.get(3), "d", "[none]", "[none]", "[unknown]", "[unknown]", "");
	}

}