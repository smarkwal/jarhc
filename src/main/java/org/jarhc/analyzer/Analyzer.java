package org.jarhc.analyzer;

import org.jarhc.model.Classpath;
import org.jarhc.report.ReportSection;

public abstract class Analyzer {

	public abstract ReportSection analyze(Classpath classpath);

}
