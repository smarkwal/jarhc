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

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class JavaRuntime {

	private static JavaRuntime defaultRuntime = new JavaRuntime();

	public static JavaRuntime getDefault() {
		return defaultRuntime;
	}

	public static void setDefault(JavaRuntime javaRuntime) {
		defaultRuntime = javaRuntime;
	}

	// ---------------------------------------------------------------------------

	private static final ClassLoader CLASSLOADER = ClassLoader.getSystemClassLoader().getParent();

	private final Properties systemProperties;

	/**
	 * Cache for class name -> class loader mapping
	 */
	private final Map<String, String> classLoaders = new HashMap<>();

	protected JavaRuntime() {
		systemProperties = System.getProperties();
	}

	public String getName() {
		return systemProperties.getProperty("java.runtime.name");
	}

	public String getJavaVersion() {
		return systemProperties.getProperty("java.version");
	}

	public String getJavaVendor() {
		return systemProperties.getProperty("java.vendor");
	}

	public String getJavaHome() {
		return systemProperties.getProperty("java.home");
	}

	public String getClassLoaderName(String className) {

		// check cache
		synchronized (this) {
			if (classLoaders.containsKey(className)) {
				return classLoaders.get(className);
			}
		}

		String classLoader = getClassLoaderNameInternal(className);

		// update cache
		synchronized (this) {
			classLoaders.put(className, classLoader);
		}

		return classLoader;
	}

	private String getClassLoaderNameInternal(String className) {
		try {
			Class<?> javaClass = Class.forName(className, false, CLASSLOADER);
			ClassLoader classLoader = javaClass.getClassLoader();
			if (classLoader == null) return "Bootstrap";
			return classLoader.toString();
		} catch (ClassNotFoundException e) {
			return null;
		} catch (Throwable t) {
			// TODO: ignore ?
			t.printStackTrace(System.err);
			return null;
		}
	}

}
