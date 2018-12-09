package org.jarhc.analyzer;

import org.jarhc.model.Classpath;
import org.jarhc.report.Report;
import org.jarhc.report.ReportSection;

import java.util.Arrays;
import java.util.List;

public class Analysis {

	private final List<Analyzer> analyzers;

	public Analysis(Analyzer... analyzers) {
		this.analyzers = Arrays.asList(analyzers);
	}

	public Report run(Classpath classpath) {
		Report report = new Report();
		for (Analyzer analyzer : analyzers) {
			ReportSection section = analyzer.analyze(classpath);
			report.addSection(section);
		}
		return report;
	}

}
