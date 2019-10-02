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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a project model as declared in a POM file.
 */
public class POM {

	private String groupId;
	private String artifactId;
	private String version;

	private POM parent;

	private String name;
	private String description;

	private final Map<String, String> properties = new LinkedHashMap<>();
	private final List<Dependency> dependencies = new ArrayList<>();

	POM(String groupId, String artifactId, String version) {
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

	void setVersion(String version) {
		if (version == null || version.isEmpty()) throw new IllegalArgumentException("version");
		this.version = version;
	}

	public boolean hasParent() {
		return parent != null;
	}

	public POM getParent() {
		return parent;
	}

	void setParent(String groupId, String artifactId, String version) {
		if (groupId == null || groupId.isEmpty()) throw new IllegalArgumentException("groupId");
		if (artifactId == null || artifactId.isEmpty()) throw new IllegalArgumentException("artifactId");
		if (version == null || version.isEmpty()) throw new IllegalArgumentException("version");
		this.parent = new POM(groupId, artifactId, version);
	}

	public void setParent(POM parent) {
		if (parent == null) throw new IllegalArgumentException("parent");
		this.parent = parent;
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

	void setProperty(String name, String value) {
		properties.put(name, value);
	}

	public boolean hasProperty(String name) {
		return properties.containsKey(name);
	}

	public String getProperty(String name) {
		return properties.get(name);
	}

	public List<String> getPropertyNames() {
		return new ArrayList<>(properties.keySet());
	}

	void addDependency(Dependency dependency) {
		dependencies.add(dependency);
	}

	public List<Dependency> getDependencies() {
		return new ArrayList<>(dependencies);
	}

}
