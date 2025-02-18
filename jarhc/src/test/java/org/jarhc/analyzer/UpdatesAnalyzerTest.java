/*
 * Copyright 2025 Stephan Markwalder
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
import static org.jarhc.test.log.LoggerAssertions.assertLogger;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jarhc.artifacts.ArtifactVersion;
import org.jarhc.artifacts.Repository;
import org.jarhc.artifacts.RepositoryException;
import org.jarhc.model.Classpath;
import org.jarhc.report.ReportSection;
import org.jarhc.report.ReportTable;
import org.jarhc.test.ClasspathBuilder;
import org.jarhc.test.log.LoggerBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

public class UpdatesAnalyzerTest {

	private final Repository repository = mock(Repository.class);

	@BeforeEach
	void setUp() throws RepositoryException {

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

		// prepare
		Classpath classpath = ClasspathBuilder.create(null)
				.addJarFile("a.jar", "a:a:1.0.0", 100)
				.addJarFile("b.jar", "b:b:2.0.0", 100)
				.addJarFile("c.jar", "c:c:3.0.0", 100)
				.addJarFile("d.jar", "d:d:4.0.0", 100)
				.addJarFile("e.jar", "e:e:5.0.0", 100)
				.build();

		Logger logger = LoggerBuilder.collect(UpdatesAnalyzer.class);

		// test
		UpdatesAnalyzer analyzer = new UpdatesAnalyzer(repository, logger);
		ReportSection section = analyzer.analyze(classpath);

		// assert
		assertNotNull(section);
		assertEquals("Updates", section.getTitle());
		assertEquals("Information about newer major, minor, and patch versions of artifacts.", section.getDescription());
		assertEquals(1, section.getContent().size());
		assertInstanceOf(ReportTable.class, section.getContent().get(0));

		ReportTable table = (ReportTable) section.getContent().get(0);

		String[] columns = table.getColumns();
		assertValuesEquals(columns, "Artifact", "Version", "Updates");

		List<String[]> rows = table.getRows();
		assertEquals(5, rows.size());
		assertValuesEquals(rows.get(0), "a", "[1.0.0](a:a:1.0.0)", "[1.0.1](a:a:1.0.1)\n[1.1.0](a:a:1.1.0)\n[2.0.0](a:a:2.0.0)");
		assertValuesEquals(rows.get(1), "b", "[2.0.0](b:b:2.0.0)", "");
		assertValuesEquals(rows.get(2), "c", "[3.0.0](c:c:3.0.0)", "[unknown]");
		assertValuesEquals(rows.get(3), "d", "[4.0.0](d:d:4.0.0)", "[error]");

		String updates = "[5.0.1](e:e:5.0.1), [5.0.2](e:e:5.0.2), [5.0.3](e:e:5.0.3), [...], [5.0.7](e:e:5.0.7), [5.0.8](e:e:5.0.8), [5.0.9](e:e:5.0.9)\n" +
				"[5.1.0](e:e:5.1.0), [5.1.1](e:e:5.1.1), [5.1.2](e:e:5.1.2), [...], [5.1.7](e:e:5.1.7), [5.1.8](e:e:5.1.8), [5.1.9](e:e:5.1.9)\n" +
				"[5.2.0](e:e:5.2.0), [5.2.1](e:e:5.2.1), [5.2.2](e:e:5.2.2), [...], [5.2.7](e:e:5.2.7), [5.2.8](e:e:5.2.8), [5.2.9](e:e:5.2.9)\n" +
				"[...]\n" +
				"[9.7.0](e:e:9.7.0), [9.7.1](e:e:9.7.1), [9.7.2](e:e:9.7.2), [...], [9.7.7](e:e:9.7.7), [9.7.8](e:e:9.7.8), [9.7.9](e:e:9.7.9)\n" +
				"[9.8.0](e:e:9.8.0), [9.8.1](e:e:9.8.1), [9.8.2](e:e:9.8.2), [...], [9.8.7](e:e:9.8.7), [9.8.8](e:e:9.8.8), [9.8.9](e:e:9.8.9)\n" +
				"[9.9.0](e:e:9.9.0), [9.9.1](e:e:9.9.1), [9.9.2](e:e:9.9.2), [...], [9.9.7](e:e:9.9.7), [9.9.8](e:e:9.9.8), [9.9.9](e:e:9.9.9)";
		assertValuesEquals(rows.get(4), "e", "[5.0.0](e:e:5.0.0)", updates);

		assertLogger(logger)
				.hasError("Resolver error for artifact: d:d:4.0.0", new RepositoryException("Test"))
				.isEmpty();
	}

}