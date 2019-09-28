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

/**
 * Represents a project model as declared in a POM file.
 */
public class Model {

	private final String groupId;
	private final String artifactId;
	private final String version;

	private String name;
	private String description;

	private final List<Dependency> dependencies = new ArrayList<>();

	Model(String groupId, String artifactId, String version) {
		if (groupId == null || groupId.isEmpty()) throw new IllegalArgumentException("groupId");
		if (artifactId == null || artifactId.isEmpty()) throw new IllegalArgumentException("artifactId");
		if (version == null || version.isEmpty()) throw new IllegalArgumentException("version");
		this.groupId = groupId;
		this.artifactId = artifactId;
		this.version = version;
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

	public String getName() {
		return name;
	}

	void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	void setDescription(String description) {
		this.description = description;
	}

	void addDependency(Dependency dependency) {
		dependencies.add(dependency);
	}

	public List<Dependency> getDependencies() {
		return new ArrayList<>(dependencies);
	}

}
