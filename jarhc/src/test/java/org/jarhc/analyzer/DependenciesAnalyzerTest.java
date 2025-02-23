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
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jarhc.artifacts.Artifact;
import org.jarhc.artifacts.ArtifactVersion;
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
	private final Logger logger = LoggerBuilder.collect(DependenciesAnalyzer.class);
	private final DependenciesAnalyzer analyzer = new DependenciesAnalyzer(repository, logger);

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

		// for updates
		when(repository.getVersions("a", "a")).thenReturn(versions("1.0.0", "1.0.1", "1.1.0", "2.0.0"));
		when(repository.getVersions("b", "b")).thenReturn(versions("1.0.0", "1.0.1", "1.1.0", "2.0.0"));
		when(repository.getVersions("c", "c")).thenReturn(versions());
		when(repository.getVersions("d", "d")).thenThrow(new RepositoryException("Test"));

		List<ArtifactVersion> versions = new ArrayList<>();
		for (int major = 1; major <= 9; major++) {
			for (int minor = 0; minor <= 9; minor++) {
				for (int patch = 0; patch <= 9; patch++) {
					versions.add(new ArtifactVersion(major, minor, +patch));
				}
			}
		}
		when(repository.getVersions("e", "e")).thenReturn(versions);

	}

	private List<ArtifactVersion> versions(String... versions) {
		return Stream.of(versions).map(ArtifactVersion::new).collect(Collectors.toList());
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

		// test
		ReportSection section = analyzer.analyze(classpath);

		// assert
		List<String[]> rows = assertTable(section);
		assertEquals(5, rows.size());

		assertValuesEquals(rows.get(0), "lib-with-deps", "[none]", "[none]", "[[group:lib-with-deps:1.0]]", "[unknown]", "[[group:lib-with-deps-1:1.0]] (provided, optional)\n[[group:lib-with-deps-2:1.0]] (runtime)\n[[group:lib-with-deps-4:1.0]] (system)\n[[group:lib-with-deps-5:1.0]] (import, optional)", "OK\nOK\nOK (version 1.1)\nUnsatisfied");
		assertValuesEquals(rows.get(1), "lib-no-deps", "[none]", "[none]", "[[group:lib-no-deps:1.0]]", "[unknown]", "[none]", "");
		assertValuesEquals(rows.get(2), "lib-no-pom", "[none]", "[none]", "[[group:lib-no-pom:1.0]]", "[unknown]", "[error]", "");
		assertValuesEquals(rows.get(3), "lib-repo-error", "[none]", "[none]", "[[group:lib-repo-error:1.0]]", "[unknown]", "[error]", "");
		assertValuesEquals(rows.get(4), "lib-unknown", "[none]", "[none]", "[unknown]", "[unknown]", "[unknown]", "");

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
		ReportSection section = analyzer.analyze(classpath);

		// assert
		List<String[]> rows = assertTable(section);
		assertEquals(4, rows.size());

		assertValuesEquals(rows.get(0), "a", joinLines("b", "c"), "[none]", "[unknown]", "[unknown]", "[unknown]", "");
		assertValuesEquals(rows.get(1), "b", "c", "a", "[unknown]", "[unknown]", "[unknown]", "");
		assertValuesEquals(rows.get(2), "c", "[none]", joinLines("a", "b"), "[unknown]", "[unknown]", "[unknown]", "");
		assertValuesEquals(rows.get(3), "d", "[none]", "[none]", "[unknown]", "[unknown]", "[unknown]", "");
	}

	@Test
	void analyze_Updates() {

		// prepare
		Classpath classpath = ClasspathBuilder.create(null)
				.addJarFile("a.jar", "a:a:1.0.0", 100)
				.addJarFile("b.jar", "b:b:2.0.0", 100)
				.addJarFile("c.jar", "c:c:3.0.0", 100)
				.addJarFile("d.jar", "d:d:4.0.0", 100)
				.addJarFile("e.jar", "e:e:5.0.0", 100)
				.build();

		// test
		ReportSection section = analyzer.analyze(classpath);

		// assert
		List<String[]> rows = assertTable(section);
		assertEquals(5, rows.size());

		assertValuesEquals(rows.get(0), "a", "[none]", "[none]", "[[a:a:1.0.0]]", "[1.0.1](a:a:1.0.1)\n[1.1.0](a:a:1.1.0)\n[2.0.0](a:a:2.0.0)", "[none]", "");
		assertValuesEquals(rows.get(1), "b", "[none]", "[none]", "[[b:b:2.0.0]]", "[none]", "[none]", "");
		assertValuesEquals(rows.get(2), "c", "[none]", "[none]", "[[c:c:3.0.0]]", "[unknown]", "[none]", "");
		assertValuesEquals(rows.get(3), "d", "[none]", "[none]", "[[d:d:4.0.0]]", "[error]", "[none]", "");

		String updates = "[5.0.1](e:e:5.0.1), [5.0.2](e:e:5.0.2), [5.0.3](e:e:5.0.3), [...], [5.0.7](e:e:5.0.7), [5.0.8](e:e:5.0.8), [5.0.9](e:e:5.0.9)\n" +
				"[5.1.0](e:e:5.1.0), [5.1.1](e:e:5.1.1), [5.1.2](e:e:5.1.2), [...], [5.1.7](e:e:5.1.7), [5.1.8](e:e:5.1.8), [5.1.9](e:e:5.1.9)\n" +
				"[5.2.0](e:e:5.2.0), [5.2.1](e:e:5.2.1), [5.2.2](e:e:5.2.2), [...], [5.2.7](e:e:5.2.7), [5.2.8](e:e:5.2.8), [5.2.9](e:e:5.2.9)\n" +
				"[...]\n" +
				"[9.7.0](e:e:9.7.0), [9.7.1](e:e:9.7.1), [9.7.2](e:e:9.7.2), [...], [9.7.7](e:e:9.7.7), [9.7.8](e:e:9.7.8), [9.7.9](e:e:9.7.9)\n" +
				"[9.8.0](e:e:9.8.0), [9.8.1](e:e:9.8.1), [9.8.2](e:e:9.8.2), [...], [9.8.7](e:e:9.8.7), [9.8.8](e:e:9.8.8), [9.8.9](e:e:9.8.9)\n" +
				"[9.9.0](e:e:9.9.0), [9.9.1](e:e:9.9.1), [9.9.2](e:e:9.9.2), [...], [9.9.7](e:e:9.9.7), [9.9.8](e:e:9.9.8), [9.9.9](e:e:9.9.9)";
		assertValuesEquals(rows.get(4), "e", "[none]", "[none]", "[[e:e:5.0.0]]", updates, "[none]", "");

		assertLogger(logger)
				.hasError("Resolver error for artifact: d:d:4.0.0", new RepositoryException("Test"))
				.isEmpty();
	}

	private static List<String[]> assertTable(ReportSection section) {

		assertNotNull(section);
		assertEquals("Dependencies", section.getTitle());
		assertEquals("Dependencies between JAR files, and as declared in POM file.", section.getDescription());
		assertEquals("Dependencies", section.getId());

		List<Object> contents = section.getContent();
		assertEquals(1, contents.size());
		Object content = contents.get(0);
		assertInstanceOf(ReportTable.class, content);
		ReportTable table = (ReportTable) content;

		String[] columns = table.getColumns();
		assertValuesEquals(columns, "Artifact", "Uses", "Used by", "Maven coordinates", "Updates", "Direct dependencies", "Status");

		return table.getRows();
	}

}