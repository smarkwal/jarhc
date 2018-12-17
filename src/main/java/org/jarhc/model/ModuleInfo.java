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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ModuleInfo {

	/**
	 * Module name
	 */
	private final String moduleName;

	/**
	 * List of exports
	 */
	private final List<String> exports;

	/**
	 * List of requires
	 */
	private final List<String> requires;

	public ModuleInfo(String moduleName, List<String> exports, List<String> requires) {
		if (moduleName == null) throw new IllegalArgumentException("moduleName");
		if (exports == null) throw new IllegalArgumentException("exports");
		if (requires == null) throw new IllegalArgumentException("requires");
		this.moduleName = moduleName;
		this.exports = new ArrayList<>(exports);
		this.requires = new ArrayList<>(requires);
	}

	public String getModuleName() {
		return moduleName;
	}

	public List<String> getExports() {
		return Collections.unmodifiableList(exports);
	}

	public List<String> getRequires() {
		return Collections.unmodifiableList(requires);
	}

	@Override
	public String toString() {
		return String.format("ModuleInfo[%s,exports=%s,requires=%s]", moduleName, exports, requires);
	}

}
