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

package org.jarhc.artifacts;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ArtifactVersionTest {

	@ParameterizedTest
	@CsvSource({
			"1,true,1,0,0",
			"2.3,true,2,3,0",
			"7.20.134,true,7,20,134",
			"0.0.1-SNAPSHOT,false,0,0,1",
			"0.9.0-alpha,false,0,9,0",
			"1.0.0-beta2,false,1,0,0",
			"10.0-m3,false,10,0,0",
			"2024.5.3-rc4,false,2024,5,3",
			"5.0-ga,true,5,0,0",
			"6.1.0-final,true,6,1,0",
			"9.1.2-sp1,true,9,1,2",
			"10.20.30-fat,true,10,20,30",
			"1.2.3.12345678901234567890,true,1,2,3",
			"9.0.12345678901234567890-snapshot,false,9,0,2147483647"
	})
	void test(String version, boolean stable, int major, int minor, int patch) {
		ArtifactVersion artifactVersion = ArtifactVersion.of(version);
		assertEquals(stable, artifactVersion.isStable());
		assertEquals(stable, !artifactVersion.isUnstable());
		assertEquals(major, artifactVersion.getMajor());
		assertEquals(minor, artifactVersion.getMinor());
		assertEquals(patch, artifactVersion.getPatch());
		assertEquals(version, artifactVersion.toString());
	}

	@Test
	void constructors() {
		ArtifactVersion artifactVersion1 = ArtifactVersion.of(1, 2, 3);
		ArtifactVersion artifactVersion2 = ArtifactVersion.of("1.2.3");
		assertEquals(artifactVersion1, artifactVersion2);
		assertEquals(artifactVersion2, artifactVersion1);
	}

	@Test
	void compareTo() {

		List<String> versions = Arrays.asList(
				"1.0.0",
				"1.0.1",
				"1.0.2",
				"1.1.0",
				"1.1.1",
				"1.1.2",
				"2.0.0",
				"2.0.1",
				"2.0.2",
				"3.0.0",
				"3.0.0.0.0.0.1",
				"4-alpha",
				"4-alpha1",
				"4-alpha2",
				"4-beta",
				"4-beta1",
				"4-beta2",
				"4-m1",
				"4-m2",
				"4-rc",
				"4-rc1",
				"4-rc2",
				"4-SNAPSHOT",
				"4-ga",
				"4-fat",
				"5",
				"5.1",
				"5.1.1",
				"5.1.1.1",
				"6.0.0-min",
				"6.0.0",
				"6.0.0-max",
				"6.0.1.min",
				"6.0.1",
				"6.0.1.max"
		);

		for (int i = 0; i < versions.size(); i++) {
			for (int j = 0; j < versions.size(); j++) {
				String version1 = versions.get(i);
				String version2 = versions.get(j);
				ArtifactVersion artifactVersion1 = ArtifactVersion.of(version1);
				ArtifactVersion artifactVersion2 = ArtifactVersion.of(version2);
				int expected = Integer.compare(i, j);
				int actual1 = artifactVersion1.compareTo(artifactVersion2);
				int actual2 = artifactVersion2.compareTo(artifactVersion1);
				if (expected == 0) {
					assertEquals(0, actual1, version1 + " == " + version2);
					assertEquals(0, actual2, version2 + " == " + version1);
					assertEquals(artifactVersion1, artifactVersion2, version1 + " == " + version2);
					assertEquals(artifactVersion2, artifactVersion1, version2 + " == " + version1);
				} else {
					if (expected < 0) {
						assertTrue(actual1 < 0, version1 + " < " + version2);
						assertTrue(actual2 > 0, version2 + " > " + version1);
					} else {
						assertTrue(actual1 > 0, version1 + " > " + version2);
						assertTrue(actual2 < 0, version2 + " < " + version1);
					}
					assertNotEquals(artifactVersion1, artifactVersion2, version1 + " != " + version2);
					assertNotEquals(artifactVersion2, artifactVersion1, version2 + " != " + version1);
				}
			}
		}
	}

	@Test
	void compareTo_sameVersion() {

		List<String> versions = Arrays.asList(
				"1",
				"1.0",
				"1.0.0",
				"1.0.0.0",
				"1.0.0.0.0",
				"1.0.0.0.0.0",
				"1.0.0-ga",
				"1.0.0-final",
				"1.0.0-release"
		);

		for (String version1 : versions) {
			for (String version2 : versions) {
				ArtifactVersion artifactVersion1 = ArtifactVersion.of(version1);
				ArtifactVersion artifactVersion2 = ArtifactVersion.of(version2);
				int actual1 = artifactVersion1.compareTo(artifactVersion2);
				int actual2 = artifactVersion2.compareTo(artifactVersion1);
				assertEquals(0, actual1, artifactVersion1 + " == " + artifactVersion2);
				assertEquals(0, actual2, artifactVersion2 + " == " + artifactVersion1);
			}
		}
	}

	@Test
	void compareTo_bigInt() {
		ArtifactVersion version1 = ArtifactVersion.of("0.12345678901234567890");
		ArtifactVersion version2 = ArtifactVersion.of("0.12345678901234567891");
		assertTrue(version1.compareTo(version2) < 0);
		assertTrue(version2.compareTo(version1) > 0);
	}

	@Test
	void compareTo_NaN() {
		ArtifactVersion version1 = ArtifactVersion.of("1.2.x");
		ArtifactVersion version2 = ArtifactVersion.of("1.2.3");
		assertTrue(version1.compareTo(version2) < 0);
		assertTrue(version2.compareTo(version1) > 0);
	}

	@Test
	void getPosition() {
		ArtifactVersion version = ArtifactVersion.of("1.23.1000000000.45678901234567890.x");
		assertEquals(1, version.getPosition(0));
		assertEquals(23, version.getPosition(1));
		assertEquals(1000000000, version.getPosition(2));
		assertEquals(Integer.MAX_VALUE, version.getPosition(3));
		assertEquals(0, version.getPosition(4));
		assertEquals(0, version.getPosition(5));
	}

}
