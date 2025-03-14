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

import static org.jarhc.utils.Markdown.code;

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

		String text = "Java home    : " + code(javaRuntime.getJavaHome()) + "\n" +
				"Java runtime : " + code(javaRuntime.getName()) + "\n" +
				"Java version : " + code(javaRuntime.getJavaVersion()) + "\n" +
				"Java vendor  : " + code(javaRuntime.getJavaVendor());

		ReportSection section = new ReportSection("Java Runtime", "Information about JRE/JDK runtime.");
		section.add(text);
		return section;

	}

}
