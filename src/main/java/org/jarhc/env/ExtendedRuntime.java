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

package org.jarhc.env;

import org.jarhc.model.ClassDef;
import org.jarhc.model.Classpath;

import java.util.Optional;

public class ExtendedRuntime implements JavaRuntime {

	private final Classpath providedClasspath;
	private final JavaRuntime javaRuntime;

	public ExtendedRuntime(Classpath providedClasspath, JavaRuntime javaRuntime) {
		this.providedClasspath = providedClasspath;
		this.javaRuntime = javaRuntime;
	}

	@Override
	public String getName() {
		return javaRuntime.getName();
	}

	@Override
	public String getJavaVersion() {
		return javaRuntime.getJavaVersion();
	}

	@Override
	public String getJavaVendor() {
		return javaRuntime.getJavaVendor();
	}

	@Override
	public String getJavaHome() {
		return javaRuntime.getJavaHome();
	}

	@Override
	public Optional<String> getClassLoaderName(String className) {
		return getClassDef(className).map(ClassDef::getClassLoader);
	}

	@Override
	public Optional<ClassDef> getClassDef(String className) {
		Optional<ClassDef> classDef = providedClasspath.getClassDef(className);
		if (classDef.isPresent()) return classDef;
		return javaRuntime.getClassDef(className);
	}

}
