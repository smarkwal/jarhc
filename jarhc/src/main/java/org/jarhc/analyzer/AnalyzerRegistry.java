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

package org.jarhc.analyzer;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.jarhc.inject.Injector;
import org.jarhc.inject.InjectorException;
import org.jarhc.utils.JarHcException;

/**
 * Registry for analyzers.
 */
public class AnalyzerRegistry {

	private final Injector injector;
	private final List<AnalyzerDescription> descriptions = new ArrayList<>();

	/**
	 * Create a new registry.
	 *
	 * @param injector Injector used to create new instances of analyzers.
	 */
	public AnalyzerRegistry(Injector injector) {
		this.injector = injector;
		descriptions.add(new AnalyzerDescription("jf", "JAR Files", JarFilesAnalyzer.class));
		descriptions.add(new AnalyzerDescription("d", "Dependencies", DependenciesAnalyzer.class));
		descriptions.add(new AnalyzerDescription("dc", "Duplicate Classes", DuplicateClassesAnalyzer.class));
		descriptions.add(new AnalyzerDescription("bc", "Binary Compatibility", BinaryCompatibilityAnalyzer.class));
		descriptions.add(new AnalyzerDescription("bl", "Blacklist", BlacklistAnalyzer.class));
		descriptions.add(new AnalyzerDescription("jm", "JAR Manifests", JarManifestsAnalyzer.class));
		descriptions.add(new AnalyzerDescription("m", "JPMS Modules", JpmsModulesAnalyzer.class));
		descriptions.add(new AnalyzerDescription("ob", "OSGi Bundles", OSGiBundlesAnalyzer.class));
		descriptions.add(new AnalyzerDescription("jr", "Java Runtime", JavaRuntimeAnalyzer.class));
	}

	public List<String> getCodes() {
		return descriptions.stream().map(AnalyzerDescription::getCode).collect(Collectors.toList());
	}

	public AnalyzerDescription getDescription(String code) {
		return descriptions.stream().filter(d -> d.getCode().equals(code)).findFirst().orElse(null);
	}

	public Analyzer createAnalyzer(String code) {

		// try to find analyzer description
		AnalyzerDescription description = getDescription(code);
		if (description == null) {
			throw new JarHcException("Analyzer not found: " + code);
		}

		// get analyzer implementation class
		Class<? extends Analyzer> analyzerClass = description.getAnalyzerClass();

		// try to create an instance of the analyzer
		// (inject dependencies)
		try {
			return injector.createInstance(analyzerClass);
		} catch (InjectorException e) {
			throw new JarHcException("Unable to create analyzer: " + code, e);
		}

	}

}
