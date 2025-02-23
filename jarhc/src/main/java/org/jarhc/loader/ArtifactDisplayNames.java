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

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.jarhc.model.JarFile;

public class ArtifactDisplayNames {

	public static void generateUniqueNames(List<JarFile> jarFiles) {

		Map<String, List<JarFile>> groups = jarFiles.stream().collect(Collectors.groupingBy(JarFile::getArtifactName));
		for (String artifactName : groups.keySet()) {
			List<JarFile> list = groups.get(artifactName);
			if (list.size() > 1) {
				generateUniqueNamesForSameArtifact(list);
			}
		}

	}

	private static void generateUniqueNamesForSameArtifact(List<JarFile> jarFiles) {

		// group artifacts by group ID
		Map<String, List<JarFile>> groups = jarFiles.stream().collect(Collectors.groupingBy(ArtifactDisplayNames::getGroupId));
		if (groups.size() == 1) { // all artifacts have the same group ID
			// ignore group ID, use only version
			jarFiles.forEach(jarFile -> jarFile.setDisplayName(getNameWithVersion(jarFile))); // TODO: check if all versions are unique
		} else if (groups.size() == jarFiles.size()) { // all artifacts have a different group ID
			// use only group ID
			jarFiles.forEach(jarFile -> jarFile.setDisplayName(getNameWithGroupId(jarFile)));
		} else { // some artifacts have the same group ID
			for (List<JarFile> group : groups.values()) {
				if (group.size() == 1) { // there is only one artifact with this group ID
					// use only group ID
					JarFile jarFile = group.get(0);
					jarFile.setDisplayName(getNameWithGroupId(jarFile));
				} else { // there are multiple artifacts with the same group ID
					// use group ID and version
					group.forEach(jarFile -> jarFile.setDisplayName(getNameWithGroupIdAndVersion(jarFile))); // TODO: check if all versions are unique
				}
			}
		}
	}

	private static String getNameWithGroupId(JarFile jarFile) {
		String name = jarFile.getArtifactName();
		String groupId = getGroupId(jarFile);
		return name + " [" + groupId + "]";
	}

	private static String getNameWithVersion(JarFile jarFile) {
		String name = jarFile.getArtifactName();
		String version = getVersion(jarFile);
		return name + " [" + version + "]";
	}

	private static String getNameWithGroupIdAndVersion(JarFile jarFile) {
		String name = jarFile.getArtifactName();
		String groupId = getGroupId(jarFile);
		String version = getVersion(jarFile);
		return name + " [" + groupId + ", " + version + "]";
	}

	private static String getGroupId(JarFile jarFile) {
		String groupId = jarFile.getArtifactGroupId();
		if (groupId == null) {
			return "unknown";
		}
		return groupId;
	}

	private static String getVersion(JarFile jarFile) {
		String version = jarFile.getArtifactVersion();
		if (version == null || version.isEmpty()) {
			return "unknown";
		}
		return version;
	}

}
