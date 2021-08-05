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

import java.util.ArrayList;
import java.util.List;
import org.jarhc.artifacts.Artifact;

public class PomUtils {

	public static List<Dependency> generateDependencies(Artifact artifact, int count) {
		String groupId = artifact.getGroupId();
		String artifactId = artifact.getArtifactId();
		String version = artifact.getVersion();

		List<Dependency> dependencies = new ArrayList<>();
		for (int i = 1; i <= count; i++) {
			Scope scope = Scope.values()[i % Scope.values().length];
			boolean optional = i % 2 > 0;
			Dependency dependency = new Dependency(groupId, artifactId + "-" + i, version, scope, optional);
			dependencies.add(dependency);
		}
		return dependencies;
	}

}
