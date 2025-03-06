/*
 * Copyright 2025 Stephan Markwalder
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

package org.jarhc.app;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import org.jarhc.report.Report;
import org.jarhc.report.ReportFormat;
import org.jarhc.report.html.HtmlReportFormat;
import org.jarhc.report.writer.ReportWriter;
import org.jarhc.report.writer.impl.FileReportWriter;
import org.jarhc.utils.FileUtils;
import org.jarhc.utils.JarHcException;
import org.jarhc.utils.StringUtils;
import org.jarhc.utils.VersionUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Diff {

	private PrintStream out = System.out;

	public void setOut(PrintStream out) {
		this.out = out;
	}

	public int run(Options options) {

		String version = VersionUtils.getVersion();
		out.println("JarHC - JAR Health Check " + version);
		out.println("=========================" + StringUtils.repeat("=", version.length()));
		out.println();

		// TODO: output progress
		// TODO: log debug messages

		// load report 1
		String inputPath1 = options.getInput1();
		out.println("Load report 1: " + inputPath1);
		Report report1 = loadJsonReport(inputPath1);
		if (!version.equals(report1.getVersion())) {
			String errorMessage = String.format("Report 1 was generated with a different version of JarHC: %s", report1.getVersion());
			out.println(errorMessage);
		}

		// load report 2
		String inputPath2 = options.getInput2();
		out.println("Load report 2: " + inputPath2);
		Report report2 = loadJsonReport(inputPath2);
		if (!version.equals(report2.getVersion())) {
			String errorMessage = String.format("Report 2 was generated with a different version of JarHC: %s", report2.getVersion());
			out.println(errorMessage);
		}

		// diff reports
		out.println("Compare reports ...");
		Logger logger = LoggerFactory.getLogger(DiffReportGenerator.class);
		DiffReportGenerator generator = new DiffReportGenerator(logger);
		Report report = generator.diff(report1, report2, options);

		out.println("Create diff report ...");

		ReportFormat reportFormat = new HtmlReportFormat();
		String outputPath = options.getReportFiles().get(0);
		try (ReportWriter writer = new FileReportWriter(new File(outputPath))) {
			reportFormat.format(report, writer);
		} catch (IOException e) {
			throw new JarHcException("I/O error when writing report: " + e.getMessage(), e);
		}

		return 0;
	}

	private Report loadJsonReport(String path) throws JarHcException {

		File file = new File(path);
		if (!file.isFile()) {
			throw new JarHcException("File not found: " + path);
		}

		String text;
		try {
			text = FileUtils.readFileToString(file);
		} catch (IOException e) {
			throw new JarHcException("I/O error when reading file: " + file.getAbsolutePath(), e);
		}

		// special handling for HTML report
		if (text.startsWith("<!DOCTYPE html>")) {
			// extract JSON data from HTML report
			text = HtmlReportFormat.extractJsonData(text);
		}

		JSONObject json;
		try {
			json = new JSONObject(text);
		} catch (JSONException e) {
			throw new JarHcException("Invalid JSON data in file: " + file.getAbsolutePath(), e);
		}

		try {
			return Report.fromJSON(json);
		} catch (Exception e) {
			throw new JarHcException("Invalid JSON report data in file: " + file.getAbsolutePath(), e);
		}

	}

}
