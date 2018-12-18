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

import org.jarhc.report.writer.ReportWriter;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * A report writer writing to a file on the local disk.
 */
public class FileReportWriter implements ReportWriter {

	private final Writer writer;

	public FileReportWriter(File file) throws FileNotFoundException {
		if (file == null) throw new IllegalArgumentException("file");
		FileOutputStream stream = new FileOutputStream(file);
		writer = new OutputStreamWriter(stream, StandardCharsets.UTF_8);
	}

	@Override
	public void print(String text) {
		try {
			writer.append(text);
		} catch (IOException e) {
			// TODO: use a ReportWriterException?
			throw new RuntimeException(e);
		}
	}

	@Override
	public void close() throws IOException {
		if (writer != null) {
			writer.close();
		}
	}

}
