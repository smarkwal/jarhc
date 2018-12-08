package org.jarcheck;

import org.jarcheck.analyzer.Analyzer;
import org.jarcheck.analyzer.AnalyzerRegistry;

import java.util.List;

public final class FullAnalysis {

	public static Analysis build() {
		AnalyzerRegistry registry = new AnalyzerRegistry(true);
		List<Analyzer> analyzers = registry.getAnalyzers();
		return new Analysis(analyzers.toArray(new Analyzer[0]));
	}

	private FullAnalysis() {
	}

}
