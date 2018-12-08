package org.jarcheck.analyzer;

import org.jarcheck.env.JavaRuntime;

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
