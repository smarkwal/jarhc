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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ModuleInfo {

	public static final ModuleInfo UNNAMED = ModuleInfo.forModuleName("UNNAMED").setRelease(-1);

	private String moduleName;

	/**
	 * Version branch in multi-release JAR file.
	 */
	private int release;

	private boolean automatic;
	private final List<String> packages = new ArrayList<>();
	private final List<String> requires = new ArrayList<>();
	private final Map<String, Set<String>> exports = new HashMap<>();
	private final Map<String, Set<String>> opens = new HashMap<>();

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

	public List<String> getPackages() {
		return Collections.unmodifiableList(packages);
	}

	public ModuleInfo addPackage(String packageName) {
		this.packages.add(packageName);
		return this;
	}

	public List<String> getRequires() {
		return Collections.unmodifiableList(requires);
	}

	public ModuleInfo addRequires(String moduleName) {
		this.requires.add(moduleName);
		return this;
	}

	public List<String> getExports() {
		List<String> packageNames = new ArrayList<>(exports.keySet());
		return Collections.unmodifiableList(packageNames);
	}

	public boolean isExported(String packageName, String moduleName) {
		return mapContainsEntry(exports, packageName, moduleName);
	}

	public ModuleInfo addExports(String packageName, String... moduleNames) {
		this.exports.put(packageName, toSet(moduleNames));
		return this;
	}

	public List<String> getOpens() {
		List<String> packageNames = new ArrayList<>(opens.keySet());
		return Collections.unmodifiableList(packageNames);
	}

	public boolean isOpen(String packageName, String moduleName) {
		return mapContainsEntry(opens, packageName, moduleName);
	}

	public ModuleInfo addOpens(String packageName, String... moduleNames) {
		this.opens.put(packageName, toSet(moduleNames));
		return this;
	}

	@Override
	public String toString() {
		if (this == UNNAMED) {
			return "ModuleInfo[UNNAMED]";
		} else if (automatic) {
			return String.format("ModuleInfo[%s,automatic]", getModuleName());
		} else {
			return String.format("ModuleInfo[%s,requires=%s,exports=%s,opens=%s]", getModuleName(), getRequires(), getExports(), getOpens());
		}
	}

	private static Set<String> toSet(String[] values) {
		if (values == null || values.length == 0) {
			return null;
		}
		return Arrays.stream(values).collect(Collectors.toSet());
	}

	private static boolean mapContainsEntry(Map<String, Set<String>> map, String key, String value) {
		if (key == null) throw new IllegalArgumentException("key");
		if (value == null) throw new IllegalArgumentException("value");
		if (map.containsKey(key)) {
			Set<String> values = map.get(key);
			if (values == null) {
				return true;
			} else {
				if (value.equals("UNNAMED")) {
					value = "ALL-UNNAMED";
				}
				return values.contains(value);
			}
		} else {
			return false;
		}
	}

}
