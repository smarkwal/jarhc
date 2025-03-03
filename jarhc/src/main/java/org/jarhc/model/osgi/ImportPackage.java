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

package org.jarhc.model.osgi;

import java.util.Map;
import java.util.TreeMap;
import org.jarhc.utils.Markdown;

public class ImportPackage {

	private final String packageName;
	private final String version;
	private final String resolution;
	private final Map<String, String> attributes = new TreeMap<>();

	public ImportPackage(String line) {
		int pos = line.indexOf(';');
		if (pos < 0) {
			this.packageName = line;
			this.version = null;
			this.resolution = null;
		} else {
			this.packageName = line.substring(0, pos);
			String[] parts = line.substring(pos + 1).split(";");
			for (String part : parts) {
				String[] pair = part.split("=", 2);
				String key = pair[0].trim();
				if (pair.length == 2) {
					String value = pair[1].trim();
					// remove quotes
					if (value.startsWith("\"") && value.endsWith("\"")) {
						value = value.substring(1, value.length() - 1);
					}
					attributes.put(key, value);
				} else {
					attributes.put(key, "");
				}
			}
			this.version = attributes.remove("version");
			this.resolution = attributes.remove("resolution:");
		}
	}

	public String getPackageName() {
		return packageName;
	}

	public Map<String, String> getAttributes() {
		return attributes;
	}

	public String getVersion() {
		return version;
	}

	public String getResolution() {
		return resolution;
	}

	public String toMarkdown() {
		StringBuilder buffer = new StringBuilder();
		buffer.append("`").append(packageName).append("`");
		if (version != null) {
			buffer.append(" (Version: ").append(Markdown.code(version)).append(")");
		}
		if (resolution != null) {
			if (resolution.equals("optional")) {
				buffer.append(" (optional)");
			} else {
				buffer.append("\n\t").append("Resolution: ").append(resolution);
			}
		}
		// TODO: implement Markdown formatting for more attributes
		attributes.forEach((key, value) -> {
			buffer.append("\n\t").append(key); // TODO: capitalize key?
			if (!value.isEmpty()) {
				buffer.append(": ").append(Markdown.code(value));
			}
		});
		return buffer.toString();
	}

}
