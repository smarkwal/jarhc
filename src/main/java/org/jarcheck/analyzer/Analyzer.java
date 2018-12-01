package org.jarcheck.analyzer;

import org.jarcheck.model.Classpath;
import org.jarcheck.report.ReportSection;

public abstract class Analyzer {

	public abstract ReportSection analyze(Classpath classpath);

}
