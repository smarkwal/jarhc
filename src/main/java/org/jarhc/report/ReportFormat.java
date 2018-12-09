package org.jarhc.report;

import java.io.PrintWriter;
import java.io.StringWriter;

public interface ReportFormat {

	void format(Report report, PrintWriter out);

	default String format(Report report) {
		StringWriter writer = new StringWriter();
		PrintWriter out = new PrintWriter(writer);
		format(report, out);
		out.flush();
		return writer.toString();
	}

}
