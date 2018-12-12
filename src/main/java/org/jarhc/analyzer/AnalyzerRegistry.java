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

import org.jarhc.env.JavaRuntime;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Registry for analyzers.
 */
public class AnalyzerRegistry {

	/**
	 * Map with analyzers, sorted by order of insertion.
	 */
	private final Map<String, Analyzer> analyzers = new LinkedHashMap<>();

	/**
	 * Create a new registry.
	 *
	 * @param registerDefaultAnalyzers Pass <code>true</code> to automatically register all default analyzers.
	 */
	public AnalyzerRegistry(boolean registerDefaultAnalyzers) {
		this(registerDefaultAnalyzers, JavaRuntime.getDefault());
	}

	/**
	 * Create a new registry.
	 *
	 * @param registerDefaultAnalyzers Pass <code>true</code> to automatically register all default analyzers.
	 * @param javaRuntime              Java runtime
	 */
	public AnalyzerRegistry(boolean registerDefaultAnalyzers, JavaRuntime javaRuntime) {
		if (registerDefaultAnalyzers) {
			registerDefaultAnalyzers(javaRuntime);
		}
	}

	/**
	 * Register all default analyzers.
	 *
	 * @param javaRuntime Java runtime used for Shadowed Classes analyzer.
	 */
	public void registerDefaultAnalyzers(JavaRuntime javaRuntime) {
		register(new JarFilesListAnalyzer());
		register(new ClassVersionAnalyzer());
		register(new PackagesAnalyzer());
		register(new SplitPackagesAnalyzer());
		register(new DuplicateClassesAnalyzer());
		register(new ShadowedClassesAnalyzer(javaRuntime));
		register(new JarDependenciesAnalyzer());
		register(new MissingClassesAnalyzer());
	}

	/**
	 * Register the given analyzer.
	 *
	 * @param analyzer Analyzer
	 */
	public void register(Analyzer analyzer) {
		analyzers.put(getAnalyzerName(analyzer), analyzer);
	}

	/**
	 * Get the names of all analyzers.
	 *
	 * @return Analyzer names
	 */
	public List<String> getAnalyzerNames() {
		return new ArrayList<>(analyzers.keySet());
	}

	/**
	 * Get the analyzer with the given name.
	 * This method returns <code>null</code> if there is no such analyzer.
	 *
	 * @param name Analyzer name
	 * @return Analyzer, or <code>null</code>
	 */
	public Analyzer getAnalyzer(String name) {
		return analyzers.get(name);
	}

	/**
	 * Get all registered analyzers.
	 *
	 * @return List of analyzers
	 */
	public List<Analyzer> getAnalyzers() {
		return new ArrayList<>(analyzers.values());
	}

	/**
	 * Get the name of the given analyzer.
	 *
	 * @param analyzer Analyzer
	 * @return Analyzer name
	 */
	public static String getAnalyzerName(Analyzer analyzer) {
		return analyzer.getClass().getSimpleName();
	}

}
