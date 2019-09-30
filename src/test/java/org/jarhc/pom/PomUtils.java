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

public class PomUtils {

	public static String generatePomXml(Artifact artifact, int dependencies) {
		StringBuilder pom = new StringBuilder();
		pom.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		pom.append("<project>");
		pom.append("<groupId>").append(artifact.getGroupId()).append("</groupId>");
		pom.append("<artifactId>").append(artifact.getArtifactId()).append("</artifactId>");
		pom.append("<version>").append(artifact.getVersion()).append("</version>");
		pom.append("<dependencies>");
		for (int i = 1; i <= dependencies; i++) {
			String scope = Scope.values()[i % Scope.values().length].name().toLowerCase();
			pom.append("<dependency>");
			pom.append("<groupId>").append(artifact.getGroupId()).append("</groupId>");
			pom.append("<artifactId>").append(artifact.getArtifactId()).append("-").append(i).append("</artifactId>");
			pom.append("<version>").append(artifact.getVersion()).append("</version>");
			pom.append("<scope>").append(scope).append("</scope>");
			if (i % 2 > 0) {
				pom.append("<optional>true</optional>");
			}
			pom.append("</dependency>");
		}
		pom.append("</dependencies>");
		pom.append("</project>");
		return pom.toString();
	}

}
