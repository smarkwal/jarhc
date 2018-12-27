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

import org.jarhc.Context;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Registry for analyzers.
 */
public class AnalyzerRegistry {

	/**
	 * Map with analyzers, sorted by order of insertion.
	 */
	private final List<Analyzer> analyzers = new ArrayList<>();

	private final Context context;

	/**
	 * Create a new registry.
	 *
	 * @param context                  Context
	 * @param registerDefaultAnalyzers Pass <code>true</code> to automatically register all default analyzers.
	 */
	public AnalyzerRegistry(Context context, boolean registerDefaultAnalyzers) {
		if (context == null) throw new IllegalArgumentException("context");
		this.context = context;
		if (registerDefaultAnalyzers) {
			registerDefaultAnalyzers();
		}
	}

	/**
	 * Register all default analyzers.
	 */
	public void registerDefaultAnalyzers() {
		register(new JarFilesAnalyzer(context.getResolver()));
		register(new ClassVersionsAnalyzer());
		register(new PackagesAnalyzer());
		register(new SplitPackagesAnalyzer());
		register(new DuplicateClassesAnalyzer());
		register(new ShadowedClassesAnalyzer(context.getJavaRuntime()));
		register(new JarDependenciesAnalyzer());
		register(new MissingClassesAnalyzer(context.getJavaRuntime()));
		register(new FieldRefAnalyzer(context.getJavaRuntime(), false));
		register(new BlacklistAnalyzer());
	}

	/**
	 * Register the given analyzer.
	 *
	 * @param analyzer Analyzer
	 */
	public void register(Analyzer analyzer) {
		analyzers.add(analyzer);
	}

	/**
	 * Get the names of all analyzers.
	 *
	 * @return Analyzer names
	 */
	public List<String> getAnalyzerNames() {
		return analyzers.stream()
				.map(Analyzer::getName)
				.collect(Collectors.toList());
	}

	/**
	 * Get the analyzer with the given name.
	 * This method returns <code>null</code> if there is no such analyzer.
	 *
	 * @param name Analyzer name
	 * @return Analyzer, or <code>null</code>
	 */
	public Optional<Analyzer> getAnalyzer(String name) {
		return analyzers.stream()
				.filter(a -> hasName(a, name))
				.findFirst();
	}

	private static boolean hasName(Analyzer analyzer, String name) {
		String longName = analyzer.getName();
		if (name.equalsIgnoreCase(longName)) return true;
		String shortName = longName.replaceAll("[^A-Z]", "");
		return name.equalsIgnoreCase(shortName);
	}

	/**
	 * Get all registered analyzers.
	 *
	 * @return List of analyzers
	 */
	public List<Analyzer> getAnalyzers() {
		return new ArrayList<>(analyzers);
	}

}
