package org.jarhc.app;

import org.jarhc.analyzer.Analysis;
import org.jarhc.analyzer.Analyzer;
import org.jarhc.analyzer.AnalyzerRegistry;

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
