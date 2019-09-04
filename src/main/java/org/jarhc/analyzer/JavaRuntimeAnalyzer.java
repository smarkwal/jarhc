/*
 * Copyright 2019 Stephan Markwalder
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

package org.jarhc.analyzer;

import org.jarhc.env.JavaRuntime;
import org.jarhc.model.Classpath;
import org.jarhc.report.ReportSection;

public class JavaRuntimeAnalyzer implements Analyzer {

	private final JavaRuntime javaRuntime;

	public JavaRuntimeAnalyzer(JavaRuntime javaRuntime) {
		this.javaRuntime = javaRuntime;
	}

	@Override
	public ReportSection analyze(Classpath classpath) {

		StringBuilder output = new StringBuilder();
		output.append("Java home    : ").append(javaRuntime.getJavaHome()).append(System.lineSeparator());
		output.append("Java runtime : ").append(javaRuntime.getName()).append(System.lineSeparator());
		output.append("Java version : ").append(javaRuntime.getJavaVersion()).append(System.lineSeparator());
		output.append("Java vendor  : ").append(javaRuntime.getJavaVendor());
		String text = output.toString();

		ReportSection section = new ReportSection("Java Runtime", "Information about JRE/JDK runtime.");
		section.add(text);
		return section;

	}

}
