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

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jarhc.utils.Markdown;

public class Artifact {

	private static final String[] QUALIFIER_PATTERNS = { "-snapshot", "-dev", "-alpha\\d*", "-beta\\d*", "-m\\d*", "-rc\\d*", "-sp\\d*", "-p\\d*", "-ga", "-final", "-release" };
	private static final Pattern VERSION_PATTERN = Pattern.compile("-(\\d+(\\.\\d+){0,10}(-[1-9])?(" + String.join("|", QUALIFIER_PATTERNS) + ")?)", Pattern.CASE_INSENSITIVE);

	private final String groupId;
	private final String artifactId;
	private final String version;
	private final String type;

	public Artifact(String groupId, String artifactId, String version, String type) {
		if (groupId == null) throw new IllegalArgumentException("groupId");
		if (artifactId == null) throw new IllegalArgumentException("artifactId");
		if (version == null) throw new IllegalArgumentException("version");
		if (type == null) throw new IllegalArgumentException("type");
		this.groupId = groupId;
		this.artifactId = artifactId;
		this.version = version;
		this.type = type;
	}

	public static boolean validateCoordinates(String coordinates) {
		if (coordinates.contains("/") || coordinates.contains("\\")) return false;
		String[] parts = coordinates.split(":");
		return parts.length == 3 || parts.length == 4;
	}

	public Artifact(String coordinates) {
		String[] parts = coordinates.split(":");
		if (parts.length < 3 || parts.length > 4) throw new IllegalArgumentException("coordinates");
		this.groupId = parts[0];
		this.artifactId = parts[1];

		if (parts.length == 4 && (parts[2].equals("jar") || parts[2].equals("war"))) {
			// parse as Buildr coordinates: <groupId>:<artifactId>:<type>:<version>
			this.type = parts[2];
			this.version = parts[3];
		} else {
			this.version = parts[2];
			this.type = parts.length > 3 ? parts[3] : "jar";
		}
	}

	/**
	 * Create an artifact from a file name. The artifact ID and version are extracted from the file name.
	 * The type is given by the file extension, and the group ID is always empty.
	 * Note: This method should be used only if the artifact cannot be identified otherwise.
	 *
	 * @param fileName File name, including file extension
	 * @return Artifact
	 */
	public static Artifact fromFileName(String fileName) {

		// remove file extension and use as type
		String type = "";
		int pos = fileName.lastIndexOf(".");
		if (pos >= 0) {
			type = fileName.substring(pos + 1);
			fileName = fileName.substring(0, pos);
		}

		// search for version number in file name
		Matcher matcher = VERSION_PATTERN.matcher(fileName);
		if (matcher.find()) {
			String version = matcher.group(1);
			String artifactId = matcher.replaceFirst("");
			return new Artifact("", artifactId, version, type);
		}

		// fallback: use file name as artifact name (version is unknown)
		return new Artifact("", fileName, "", type);
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

	public boolean isSame(String groupId, String artifactId) {
		return this.groupId.equals(groupId) && this.artifactId.equals(artifactId);
	}

	/**
	 * Create a new derived artifact with the given type.
	 *
	 * @param type Type
	 * @return Artifact with the given type
	 */
	public Artifact withType(String type) {
		return new Artifact(groupId, artifactId, version, type);
	}

	@Override
	public String toString() {
		return String.format("%s:%s:%s:%s", groupId, artifactId, version, type);
	}

	public String toCoordinates() {
		return String.format("%s:%s:%s", groupId, artifactId, version); // TODO: append type if it is not "jar"?
	}

	public String toLink() {
		return Markdown.link(toCoordinates());
	}

	public String toPackageUrl() {
		return String.format("pgk:maven/%s/%s@%s", groupId, artifactId, version); // TODO: how to handle type?
	}

	public String getPath() {
		return String.format("%s/%s/%s/%s", groupId.replace('.', '/'), artifactId, version, getFileName());
	}

	public String getFileName() {
		return String.format("%s-%s.%s", artifactId, version, getFileExtension());
	}

	private String getFileExtension() {
		if (type.equals("bundle")) {
			return "jar";
		}
		return type;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null || getClass() != obj.getClass()) return false;
		Artifact artifact = (Artifact) obj;
		return Objects.equals(groupId, artifact.groupId) &&
				Objects.equals(artifactId, artifact.artifactId) &&
				Objects.equals(version, artifact.version) &&
				Objects.equals(type, artifact.type);
	}

	@Override
	public int hashCode() {
		return Objects.hash(groupId, artifactId, version, type);
	}

}
