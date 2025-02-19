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

package org.jarhc.pom;

import java.util.Comparator;
import org.jarhc.artifacts.Artifact;

/**
 * Comparator for dependencies which prefers dependencies with
 * same or similar group ID and artifact ID.
 */
public class SmartDependencyComparator implements Comparator<Dependency> {

	private final String rootGroupId;
	private final String rootArtifactId;
	// private final String rootVersionId;

	public SmartDependencyComparator(Artifact artifact) {
		if (artifact == null) throw new IllegalArgumentException("artifact");
		this.rootGroupId = artifact.getGroupId();
		this.rootArtifactId = artifact.getArtifactId();
		// this.rootVersionId = artifact.getVersion();
	}

	@Override
	public int compare(Dependency dependency1, Dependency dependency2) {
		int order1 = getOrder(dependency1);
		int order2 = getOrder(dependency2);
		if (order1 != order2) {
			return order1 - order2;
		}
		return dependency1.compareTo(dependency2);
	}

	private int getOrder(Dependency dependency) {

		// priority 1: dependencies with same group ID
		String groupId = dependency.getGroupId();
		if (groupId.equals(rootGroupId)) {

			// priority 1.1: dependencies with longer common artifact ID prefix
			String prefix = getCommonPrefix(dependency.getArtifactId(), rootArtifactId);
			if (prefix.contains("-") || prefix.contains(".")) {
				return 1000 - prefix.length();
			}

			// TODO: priority 1.x: dependencies with same version?

			return 1000;
		}

		// priority 2: dependencies with same group ID prefix
		if (rootGroupId.startsWith(groupId + ".")) {
			return 3000;
		}

		// priority 3: dependencies with parent group ID
		if (groupId.startsWith(rootGroupId + ".")) {
			return 2000;
		}

		// all other dependencies
		return 4000;
	}

	private String getCommonPrefix(String value1, String value2) {
		int len = Math.min(value1.length(), value2.length());
		int pos = 0;
		while (pos < len && value1.charAt(pos) == value2.charAt(pos)) {
			pos++;
		}
		return value1.substring(0, pos);
	}

}
