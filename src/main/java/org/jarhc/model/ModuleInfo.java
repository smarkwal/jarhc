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

package org.jarhc.model;

import org.objectweb.asm.tree.ModuleExportNode;
import org.objectweb.asm.tree.ModuleNode;
import org.objectweb.asm.tree.ModuleRequireNode;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ModuleInfo {

	private final ModuleNode moduleNode;

	private ModuleInfo(ModuleNode moduleNode) {
		if (moduleNode == null) throw new IllegalArgumentException("moduleNode");
		this.moduleNode = moduleNode;
	}

	public String getModuleName() {
		return moduleNode.name;
	}

	public List<String> getExports() {
		// TODO: cache list?
		return moduleNode.exports.stream().map(e -> e.packaze).collect(Collectors.toList());
	}

	public List<String> getRequires() {
		// TODO: cache list?
		return moduleNode.requires.stream().map(r -> r.module).collect(Collectors.toList());
	}

	@Override
	public String toString() {
		return String.format("ModuleInfo[%s,exports=%s,requires=%s]", getModuleName(), getExports(), getRequires());
	}

	// BUILDER --------------------------------------------------------------------------------------

	public static Builder forModuleNode(ModuleNode moduleNode) {
		return new Builder(moduleNode);
	}

	public static Builder forModuleName(String moduleName) {
		return new Builder(moduleName);
	}

	public static class Builder {

		private final ModuleNode moduleNode;

		private Builder(ModuleNode moduleNode) {
			this.moduleNode = moduleNode;
		}

		private Builder(String moduleName) {
			this(new ModuleNode(moduleName, 0, "1"));
		}

		public Builder exports(String export) {
			if (this.moduleNode.exports == null) {
				this.moduleNode.exports = new ArrayList<>();
			}
			this.moduleNode.exports.add(new ModuleExportNode(export, 0, null));
			return this;
		}

		public Builder requires(String require) {
			if (this.moduleNode.requires == null) {
				this.moduleNode.requires = new ArrayList<>();
			}
			this.moduleNode.requires.add(new ModuleRequireNode(require, 0, "1"));
			return this;
		}

		public ModuleInfo build() {
			return new ModuleInfo(moduleNode);
		}

	}

}
