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

package org.jarhc.env;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import org.jarhc.loader.ClassDefLoader;
import org.jarhc.loader.LoaderBuilder;
import org.jarhc.model.ClassDef;
import org.jarhc.model.JarFile;
import org.jarhc.model.ModuleInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation of {@link JavaRuntime} based on the Java runtime used to run JarHC.
 */
public class DefaultJavaRuntime extends JavaRuntime {

	/**
	 * Use the parent class loader of the system class loader.
	 * (bootstrap class loader or extension class loader)
	 */
	private final ClassLoader classLoader = ClassLoader.getSystemClassLoader().getParent();

	/**
	 * Helper class to load module information from Java runtime.
	 */
	private final ModuleSystemRuntime moduleSystemRuntime = new ModuleSystemRuntime(classLoader, LoggerFactory.getLogger(ModuleSystemRuntime.class));

	/**
	 * Class definition loader used to load Java runtime classes.
	 * This loader does not scan Java classes for references to other classes, methods or fields.
	 */
	private static final ClassDefLoader loader = LoaderBuilder.create().forClassLoader("Runtime").scanForReferences(false).buildClassDefLoader();

	/**
	 * Java system properties used to retrieve information about the Java runtime.
	 */
	private final Properties systemProperties;

	/**
	 * Cache for class definitions. May contain "negative results" in the form of empty Optionals.
	 */
	private final Map<String, Optional<ClassDef>> classDefs = new ConcurrentHashMap<>();

	private final Logger logger;

	public DefaultJavaRuntime(Logger logger) {
		this.logger = logger;
		this.systemProperties = System.getProperties();
	}

	@Override
	public String getName() {
		return systemProperties.getProperty("java.runtime.name");
	}

	@Override
	public String getJavaVersion() {
		return systemProperties.getProperty("java.version");
	}

	@Override
	public String getJavaVendor() {
		return systemProperties.getProperty("java.vendor");
	}

	@Override
	public String getJavaHome() {
		return systemProperties.getProperty("java.home");
	}

	@Override
	public Optional<JarFile> findJarFile(Predicate<JarFile> predicate) {
		return Optional.empty();
	}

	@Override
	protected boolean findPackage(String packageName) {
		// TODO: implement
		return false;
	}

	@Override
	protected Optional<ClassDef> findClassDef(String className) {

		// check cache
		Optional<ClassDef> classDef = classDefs.getOrDefault(className, Optional.empty());
		if (classDef.isPresent()) {
			return classDef;
		}

		// create class definition
		classDef = createClassDef(className);

		// update cache
		classDefs.put(className, classDef);

		return classDef;
	}

	private Optional<ClassDef> createClassDef(String className) {

		// construct the resource name of the Java class file
		String resourceName = className.replace('.', '/') + ".class";

		try (InputStream stream = classLoader.getResourceAsStream(resourceName)) {

			if (stream == null) {
				// .class resource not found -> class not found
				return Optional.empty();
			}

			ClassDef classDef = loader.load(stream);

			// try to find module of class
			ModuleInfo moduleInfo = moduleSystemRuntime.findModuleInfo(className);
			if (moduleInfo != null) {
				classDef.setModuleInfo(moduleInfo);
			}

			return Optional.of(classDef);

		} catch (IOException e) {
			logger.warn("Failed to load class from resource: {}", resourceName, e);

			// unexpected error -> class not found
			return Optional.empty();
		}

	}

}
