/*
 * Copyright 2018 Stephan Markwalder
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

public class Artifact {

	private final String groupId;
	private final String artifactId;
	private final String version;
	private final String type;

	public Artifact(String groupId, String artifactId, String version, String type) {
		if (groupId == null) throw new IllegalArgumentException("groupId");
		if (artifactId == null) throw new IllegalArgumentException("artifactId");
		if (version == null) throw new IllegalArgumentException("version");
		this.groupId = groupId;
		this.artifactId = artifactId;
		this.version = version;
		this.type = type;
	}

	public static boolean validateCoordinates(String coordinates) {
		if (coordinates.contains("/") || coordinates.contains("\\")) return false;
		long separators = coordinates.chars().filter(c -> c == ':').count();
		if (separators < 2 || separators > 3) return false;
		return true;
	}

	public Artifact(String coordinates) {
		String[] parts = coordinates.split(":");
		if (parts.length < 3 || parts.length > 4) throw new IllegalArgumentException("coordinates");
		this.groupId = parts[0];
		this.artifactId = parts[1];
		this.version = parts[2];
		this.type = parts.length > 3 ? parts[3] : null;
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

	public String getType() {
		return type;
	}

	@Override
	public String toString() {
		String result = groupId + ":" + artifactId + ":" + version;
		if (type != null) {
			result += ":" + type;
		}
		return result;
	}

	public String getPath() {
		StringBuilder path = new StringBuilder();
		path.append(groupId.replace('.', '/'));
		path.append('/');
		path.append(artifactId);
		path.append('/');
		path.append(version);
		path.append('/');
		path.append(getFileName());
		return path.toString();
	}

	public String getFileName() {
		// TODO: get extension from type?
		return artifactId + '-' + version + ".jar";
	}

}
