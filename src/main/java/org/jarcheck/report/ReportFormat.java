package org.jarcheck.report;

import java.io.PrintStream;

public interface ReportFormat {
	void format(Report report, PrintStream out);
}
