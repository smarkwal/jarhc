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

package org.jarhc.model;

public class ResourceDef {

	private final String path;

	private final String checksum;

	/**
	 * Reference to parent JAR file.
	 */
	private JarFile jarFile;

	public ResourceDef(String path, String checksum) {
		this.path = path;
		this.checksum = checksum;
	}

	public String getPath() {
		return path;
	}

	public String getChecksum() {
		return checksum;
	}

	public JarFile getJarFile() {
		return jarFile;
	}

	public void setJarFile(JarFile jarFile) {
		this.jarFile = jarFile;
	}

}
