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

package org.jarhc.loader;

import java.util.function.UnaryOperator;

public class LoaderBuilder {

	private String classLoader = "Classpath";
	private boolean scanForReferences = true;
	private UnaryOperator<String> jarFileNameNormalizer = null;

	private final ClassFileParser classFileParser = new ClassFileParser();

	public static LoaderBuilder create() {
		return new LoaderBuilder();
	}

	public LoaderBuilder forClassLoader(String classLoader) {
		this.classLoader = classLoader;
		return this;
	}

	public LoaderBuilder scanForReferences(boolean scanForReferences) {
		this.scanForReferences = scanForReferences;
		return this;
	}

	public LoaderBuilder withJarFileNameNormalizer(UnaryOperator<String> jarFileNameNormalizer) {
		this.jarFileNameNormalizer = jarFileNameNormalizer;
		return this;
	}

	public ClassDefLoader buildClassDefLoader() {
		return new ClassDefLoader(classLoader, classFileParser, scanForReferences);
	}

	public ModuleInfoLoader buildModuleInfoLoader() {
		return new ModuleInfoLoader(classFileParser);
	}

	public JarFileLoader buildJarFileLoader() {
		ClassDefLoader classDefLoader = buildClassDefLoader();
		ModuleInfoLoader moduleInfoLoader = buildModuleInfoLoader();
		return new JarFileLoader(classDefLoader, moduleInfoLoader, jarFileNameNormalizer);
	}

	public ClasspathLoader buildClasspathLoader() {
		JarFileLoader jarFileLoader = buildJarFileLoader();
		return new ClasspathLoader(jarFileLoader);
	}

}
