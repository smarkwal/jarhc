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

package org.jarhc.loader;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.jarhc.artifacts.Artifact;
import org.jarhc.model.JarFile;
import org.junit.jupiter.api.Test;

public class ArtifactDisplayNamesTest {

	@Test
	void generateUniqueNames() {

		// prepare
		List<JarFile> jarFiles = new ArrayList<>();
		jarFiles.add(JarFile.withName("lib-1.0.0.jar").withArtifacts(List.of(new Artifact("group-aaa:lib:1.0.0"))).withChecksum("1111111111").build());
		jarFiles.add(JarFile.withName("lib-2.0.0.jar").withArtifacts(List.of(new Artifact("group-aaa:lib:2.0.0"))).withChecksum("2222222222").build());
		jarFiles.add(JarFile.withName("lib-1.0.0.jar").withArtifacts(List.of(new Artifact("group-xxx:lib:1.0.0"))).withChecksum("3333333333").build());
		jarFiles.add(JarFile.withName("lib-1.0.0.jar").withChecksum("4444444444").build()); // unknown artifact
		jarFiles.add(JarFile.withName("lib.jar").withChecksum("5555555555").build()); // unknown artifact, unknown version

		// test
		ArtifactDisplayNames.generateUniqueNames(jarFiles);

		List<String> displayNames = jarFiles.stream().map(JarFile::getDisplayName).collect(Collectors.toList());

		// assert
		assertEquals(List.of("lib [group-aaa, 1.0.0]", "lib [group-aaa, 2.0.0]", "lib [group-xxx]", "lib [unknown, 1.0.0]", "lib [unknown, unknown]"), displayNames);
	}

	@Test
	void generateUniqueNames_allSameGroupId() {

		// prepare
		List<JarFile> jarFiles = new ArrayList<>();
		jarFiles.add(JarFile.withName("lib-1.0.0.jar").withArtifacts(List.of(new Artifact("group-aaa:lib:1.0.0"))).withChecksum("1111111111").build());
		jarFiles.add(JarFile.withName("lib-2.0.0.jar").withArtifacts(List.of(new Artifact("group-aaa:lib:2.0.0"))).withChecksum("2222222222").build());
		jarFiles.add(JarFile.withName("lib-3.0.0.jar").withArtifacts(List.of(new Artifact("group-aaa:lib:3.0.0"))).withChecksum("3333333333").build());

		// test
		ArtifactDisplayNames.generateUniqueNames(jarFiles);

		List<String> displayNames = jarFiles.stream().map(JarFile::getDisplayName).collect(Collectors.toList());

		// assert
		assertEquals(List.of("lib [1.0.0]", "lib [2.0.0]", "lib [3.0.0]"), displayNames);
	}

	@Test
	void generateUniqueNames_allDifferentGroupId() {

		// prepare
		List<JarFile> jarFiles = new ArrayList<>();
		jarFiles.add(JarFile.withName("lib-1.0.0.jar").withArtifacts(List.of(new Artifact("group-aaa:lib:1.0.0"))).withChecksum("1111111111").build());
		jarFiles.add(JarFile.withName("lib-2.0.0.jar").withArtifacts(List.of(new Artifact("group-bbb:lib:2.0.0"))).withChecksum("2222222222").build());
		jarFiles.add(JarFile.withName("lib-3.0.0.jar").withArtifacts(List.of(new Artifact("group-ccc:lib:3.0.0"))).withChecksum("3333333333").build());

		// test
		ArtifactDisplayNames.generateUniqueNames(jarFiles);

		List<String> displayNames = jarFiles.stream().map(JarFile::getDisplayName).collect(Collectors.toList());

		// assert
		assertEquals(List.of("lib [group-aaa]", "lib [group-bbb]", "lib [group-ccc]"), displayNames);
	}

}
