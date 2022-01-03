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

	public static final ModuleInfo UNNAMED = ModuleInfo.forModuleName("UNNAMED").setRelease(-1);

	private String moduleName;

	/**
	 * Version branch in multi-release JAR file.
	 */
	private int release;

	private boolean automatic;
	private final List<String> exports = new ArrayList<>();
	private final List<String> requires = new ArrayList<>();

	public static ModuleInfo forModuleName(String moduleName) {
		ModuleInfo moduleInfo = new ModuleInfo();
		moduleInfo.setModuleName(moduleName);
		return moduleInfo;
	}

	public String getModuleName() {
		return moduleName;
	}

	public ModuleInfo setModuleName(String moduleName) {
		this.moduleName = moduleName;
		return this;
	}

	public int getRelease() {
		return release;
	}

	public ModuleInfo setRelease(int release) {
		this.release = release;
		return this;
	}

	public boolean isAutomatic() {
		return automatic;
	}

	public ModuleInfo setAutomatic(boolean automatic) {
		this.automatic = automatic;
		return this;
	}

	public boolean isNamed() {
		return this != UNNAMED;
	}

	public boolean isUnnamed() {
		return this == UNNAMED;
	}

	public boolean isSame(ModuleInfo moduleInfo) {
		// TODO: compare reference instead of names?
		return moduleName.equals(moduleInfo.moduleName);
	}

	public List<String> getExports() {
		return Collections.unmodifiableList(exports);
	}

	public ModuleInfo addExport(String export) {
		this.exports.add(export);
		return this;
	}

	public List<String> getRequires() {
		return Collections.unmodifiableList(requires);
	}

	public ModuleInfo addRequire(String require) {
		this.requires.add(require);
		return this;
	}

	@Override
	public String toString() {
		if (this == UNNAMED) {
			return "ModuleInfo[UNNAMED]";
		} else if (automatic) {
			return String.format("ModuleInfo[%s,automatic]", getModuleName());
		} else {
			return String.format("ModuleInfo[%s,exports=%s,requires=%s]", getModuleName(), getExports(), getRequires());
		}
	}

}
