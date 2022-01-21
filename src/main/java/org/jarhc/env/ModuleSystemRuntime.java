/*
 * Copyright 2022 Stephan Markwalder
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

package org.jarhc.env;

import java.lang.module.ModuleDescriptor;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.jarhc.model.ModuleInfo;
import org.slf4j.Logger;

class ModuleSystemRuntime {

	private final ClassLoader classLoader;
	private final Logger logger;

	/**
	 * Cache for module infos.
	 */
	private final Map<String, ModuleInfo> moduleInfos = new ConcurrentHashMap<>();

	ModuleSystemRuntime(ClassLoader classLoader, Logger logger) {
		this.classLoader = classLoader;
		this.logger = logger;
	}

	@SuppressWarnings("java:S1181") // Throwable and Error should not be caught
	public ModuleInfo findModuleInfo(String className) {

		// try to load class
		Class<?> cls;
		try {
			cls = classLoader.loadClass(className);
		} catch (Throwable t) {
			logger.warn("Unable to load module information for class: {}", className, t);
			return ModuleInfo.UNNAMED;
		}

		// get module
		Module module = cls.getModule();

		// get module name
		String moduleName = module.getName();

		synchronized (moduleInfos) {

			// check if module has already been loaded
			ModuleInfo moduleInfo = moduleInfos.get(moduleName);
			if (moduleInfo != null) {
				return moduleInfo;
			}

			moduleInfo = new ModuleInfo();
			moduleInfo.setModuleName(moduleName);

			// get module descriptor
			ModuleDescriptor descriptor = module.getDescriptor();

			// get list of packages
			Set<String> packages = module.getPackages();
			for (String packageName : packages) {
				moduleInfo.addPackage(packageName);
			}

			// load list of required modules
			for (ModuleDescriptor.Requires requires : descriptor.requires()) {
				moduleInfo.addRequires(requires.name());
			}

			// load list of exports packages
			for (ModuleDescriptor.Exports exports : descriptor.exports()) {
				moduleInfo.addExports(exports.source(), exports.targets().toArray(new String[0]));
			}

			// load list of opens packages
			for (ModuleDescriptor.Opens opens : descriptor.opens()) {
				moduleInfo.addOpens(opens.source(), opens.targets().toArray(new String[0]));
			}

			logger.trace("Module information loaded: {}", moduleInfo);

			// add module info to cache
			moduleInfos.put(moduleName, moduleInfo);

			return moduleInfo;
		}

	}

}
