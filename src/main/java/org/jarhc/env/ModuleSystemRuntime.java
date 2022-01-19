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

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.jarhc.model.ModuleInfo;
import org.jarhc.utils.JarHcException;
import org.jarhc.utils.JavaUtils;
import org.slf4j.Logger;

class ModuleSystemRuntime {

	private final ClassLoader classLoader;
	private final Logger logger;

	/**
	 * Cache for module infos.
	 */
	private final Map<String, ModuleInfo> moduleInfos = new ConcurrentHashMap<>();

	private final int javaVersion = JavaUtils.getJavaVersion();

	private Method classGetModuleMethod;
	private Method moduleGetNameMethod;
	private Method moduleGetPackagesMethod;
	private Method moduleGetDescriptorMethod;
	private Method descriptorRequiresMethod;
	private Method requiresNameMethod;
	private Method descriptorExportsMethod;
	private Method exportsSourceMethod;
	private Method exportsTargetsMethod;
	private Method descriptorOpensMethod;
	private Method opensSourceMethod;
	private Method opensTargetsMethod;

	ModuleSystemRuntime(ClassLoader classLoader, Logger logger) {
		this.classLoader = classLoader;
		this.logger = logger;

		if (javaVersion >= 9) {
			try {
				Class<?> moduleClass = Class.forName("java.lang.Module");
				Class<?> descriptorClass = Class.forName("java.lang.module.ModuleDescriptor");
				Class<?> requiresClass = Class.forName("java.lang.module.ModuleDescriptor$Requires");
				Class<?> exportsClass = Class.forName("java.lang.module.ModuleDescriptor$Exports");
				Class<?> opensClass = Class.forName("java.lang.module.ModuleDescriptor$Opens");

				classGetModuleMethod = Class.class.getDeclaredMethod("getModule");
				moduleGetNameMethod = moduleClass.getDeclaredMethod("getName");
				moduleGetPackagesMethod = moduleClass.getDeclaredMethod("getPackages");
				moduleGetDescriptorMethod = moduleClass.getDeclaredMethod("getDescriptor");
				descriptorRequiresMethod = descriptorClass.getDeclaredMethod("requires");
				requiresNameMethod = requiresClass.getDeclaredMethod("name");
				descriptorExportsMethod = descriptorClass.getDeclaredMethod("exports");
				exportsSourceMethod = exportsClass.getDeclaredMethod("source");
				exportsTargetsMethod = exportsClass.getDeclaredMethod("targets");
				descriptorOpensMethod = descriptorClass.getDeclaredMethod("opens");
				opensSourceMethod = opensClass.getDeclaredMethod("source");
				opensTargetsMethod = opensClass.getDeclaredMethod("targets");
			} catch (Throwable t) {
				throw new JarHcException("Unexpected problem with Java Runtime.", t);
			}
		}
	}

	ModuleInfo findModuleInfo(String className) {

		if (javaVersion < 9) {
			return ModuleInfo.UNNAMED;
		}

		try {

			// try to load class
			Class<?> cls = classLoader.loadClass(className);

			// get module
			Object module = classGetModuleMethod.invoke(cls);

			// get module name
			String moduleName = (String) moduleGetNameMethod.invoke(module);

			synchronized (moduleInfos) {

				// check if module has already been loaded
				ModuleInfo moduleInfo = moduleInfos.get(moduleName);
				if (moduleInfo != null) {
					return moduleInfo;
				}

				moduleInfo = new ModuleInfo();
				moduleInfo.setModuleName(moduleName);

				// get module descriptor
				Object descriptor = moduleGetDescriptorMethod.invoke(module);

				// get list of packages
				@SuppressWarnings("unchecked")
				Set<String> packages = (Set<String>) moduleGetPackagesMethod.invoke(module);
				for (String packageName : packages) {
					moduleInfo.addPackage(packageName);
				}

				// load list of required modules
				Set<?> requires = (Set<?>) descriptorRequiresMethod.invoke(descriptor);
				for (Object require : requires) {
					String name = (String) requiresNameMethod.invoke(require);
					moduleInfo.addRequires(name);
				}

				// load list of exports packages
				Set<?> exports = (Set<?>) descriptorExportsMethod.invoke(descriptor);
				for (Object export : exports) {
					String source = (String) exportsSourceMethod.invoke(export);
					Set<String> targets = (Set<String>) exportsTargetsMethod.invoke(export);
					moduleInfo.addExports(source, targets.toArray(new String[0]));
				}

				// load list of opens packages
				Set<?> opens = (Set<?>) descriptorOpensMethod.invoke(descriptor);
				for (Object open : opens) {
					String source = (String) opensSourceMethod.invoke(open);
					Set<String> targets = (Set<String>) opensTargetsMethod.invoke(open);
					moduleInfo.addOpens(source, targets.toArray(new String[0]));
				}

				logger.trace("Module information loaded: {}", moduleInfo);

				// add module info to cache
				moduleInfos.put(moduleName, moduleInfo);

				return moduleInfo;
			}

		} catch (Throwable t) {
			logger.warn("Unable to load module information for class: {}", className, t);
			return ModuleInfo.UNNAMED;
		}

	}

}
