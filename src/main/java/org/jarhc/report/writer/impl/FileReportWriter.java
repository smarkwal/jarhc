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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import org.jarhc.report.writer.ReportWriter;

/**
 * A report writer writing to a file on the local disk.
 */
public class FileReportWriter implements ReportWriter {

	private final File file;
	private Writer writer;

	public FileReportWriter(File file) {
		if (file == null) throw new IllegalArgumentException("file");
		this.file = file;
	}

	@Override
	public void print(String text) {
		try {
			if (writer == null) {
				// lazy opening of file stream when first text is written
				FileOutputStream stream = new FileOutputStream(file);
				writer = new OutputStreamWriter(stream, StandardCharsets.UTF_8);
			}
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
