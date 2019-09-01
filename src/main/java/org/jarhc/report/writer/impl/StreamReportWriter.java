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

package org.jarhc.report.writer.impl;

import java.io.PrintStream;
import org.jarhc.report.writer.ReportWriter;

/**
 * Report writer writing to a print stream.
 * <p>
 * This class may be used to write to STDOUT.
 */
public class StreamReportWriter implements ReportWriter {

	private final PrintStream stream;

	public StreamReportWriter(PrintStream stream) {
		if (stream == null) throw new IllegalArgumentException("stream");
		this.stream = stream;
	}

	@Override
	public void print(String text) {
		stream.print(text);
	}

}
