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

package org.jarhc.pom;

import org.jarhc.artifacts.Artifact;
import org.jarhc.artifacts.ArtifactVersion;

public class Dependency implements Comparable<Dependency> {

	private final String groupId;
	private final String artifactId;
	private String version;
	private final Scope scope;
	private final boolean optional;

	public Dependency(String groupId, String artifactId, String version, Scope scope, boolean optional) {
		if (groupId == null || groupId.isEmpty()) throw new IllegalArgumentException("groupId");
		if (artifactId == null || artifactId.isEmpty()) throw new IllegalArgumentException("artifactId");
		if (version == null) throw new IllegalArgumentException("version");
		this.groupId = groupId;
		this.artifactId = artifactId;
		this.version = version;
		this.scope = scope;
		this.optional = optional;
	}

	public Dependency(String coordinates, Scope scope, boolean optional) {
		if (coordinates == null || coordinates.isEmpty()) throw new IllegalArgumentException("coordinates");
		Artifact artifact = new Artifact(coordinates);
		this.groupId = artifact.getGroupId();
		this.artifactId = artifact.getArtifactId();
		this.version = artifact.getVersion();
		this.scope = scope;
		this.optional = optional;
	}

	public String getGroupId() {
		return groupId;
	}

	public String getArtifactId() {
		return artifactId;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		if (version == null) throw new IllegalArgumentException("version");
		this.version = version;
	}

	public Scope getScope() {
		return scope;
	}

	public boolean isOptional() {
		return optional;
	}

	@Override
	public int compareTo(Dependency dependency) {

		// priority 1: sort by group ID
		int diff = groupId.compareTo(dependency.groupId);
		if (diff != 0) return diff;

		// priority 2: sort by artifact ID
		diff = artifactId.compareTo(dependency.artifactId);
		if (diff != 0) return diff;

		// priority 3: sort by version (semantic)
		if (!version.equals(dependency.version)) { // quick check to avoid expensive version comparison
			ArtifactVersion version1 = new ArtifactVersion(version);
			ArtifactVersion version2 = new ArtifactVersion(dependency.version);
			diff = version1.compareTo(version2);
			if (diff != 0) return diff;
		}

		// priority 4: sort by scope
		diff = scope.compareTo(dependency.scope);
		if (diff != 0) return diff;

		// priority 5: sort by optional
		return Boolean.compare(optional, dependency.optional);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null || getClass() != obj.getClass()) return false;
		Dependency dependency = (Dependency) obj;
		if (!groupId.equals(dependency.groupId)) return false;
		if (!artifactId.equals(dependency.artifactId)) return false;
		if (!version.equals(dependency.version)) return false;
		if (scope != dependency.scope) return false;
		return optional == dependency.optional;
	}

	@Override
	public int hashCode() {
		int result = groupId.hashCode();
		result = 31 * result + artifactId.hashCode();
		result = 31 * result + version.hashCode();
		result = 31 * result + scope.hashCode();
		result = 31 * result + (optional ? 1 : 0);
		return result;
	}

	public Artifact toArtifact() {
		return new Artifact(groupId, artifactId, version, "jar");
	}

	public String toMarkdown() {
		return toString(true);
	}

	@Override
	public String toString() {
		return toString(false);
	}

	private String toString(boolean markdown) {
		StringBuilder buffer = new StringBuilder();
		Artifact artifact = toArtifact();
		if (markdown) {
			buffer.append(artifact.toLink());
		} else {
			buffer.append(artifact.toCoordinates());
		}
		if (scope != Scope.COMPILE || optional) {
			buffer.append(" (");
			if (scope != Scope.COMPILE) {
				buffer.append(scope.name().toLowerCase());
				if (optional) {
					buffer.append(", ");
				}
			}
			if (optional) {
				buffer.append("optional");
			}
			buffer.append(")");
		}
		return buffer.toString();
	}

}
