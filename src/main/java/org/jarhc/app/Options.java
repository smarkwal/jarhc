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

package org.jarhc.app;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Options {

	private final List<File> classpathJarFiles = new ArrayList<>();
	private final List<File> providedJarFiles = new ArrayList<>();
	private final List<File> runtimeJarFiles = new ArrayList<>();
	private boolean removeVersion = false;
	private boolean useArtifactName = false;

	private List<String> sections = null; // all sections

	private String reportTitle = "JAR Health Check Report";
	private String reportFormat = null;
	private String reportFile = null; // STDOUT

	private boolean debug = false;

	public Options() {
	}

	public List<File> getClasspathJarFiles() {
		return classpathJarFiles;
	}

	public void addClasspathJarFiles(List<File> files) {
		this.classpathJarFiles.addAll(files);
	}

	public List<File> getProvidedJarFiles() {
		return providedJarFiles;
	}

	public void addProvidedJarFiles(List<File> files) {
		this.providedJarFiles.addAll(files);
	}

	public List<File> getRuntimeJarFiles() {
		return runtimeJarFiles;
	}

	public void addRuntimeJarFiles(List<File> files) {
		this.runtimeJarFiles.addAll(files);
	}

	public boolean isRemoveVersion() {
		return removeVersion;
	}

	public void setRemoveVersion(boolean removeVersion) {
		this.removeVersion = removeVersion;
	}

	public boolean isUseArtifactName() {
		return useArtifactName;
	}

	public void setUseArtifactName(boolean useArtifactName) {
		this.useArtifactName = useArtifactName;
	}

	public List<String> getSections() {
		return sections;
	}

	public void setSections(List<String> sections) {
		this.sections = sections;
	}

	public String getReportTitle() {
		return reportTitle;
	}

	public void setReportTitle(String reportTitle) {
		this.reportTitle = reportTitle;
	}

	public String getReportFormat() {
		if (reportFormat != null) {
			return reportFormat;
		}
		if (reportFile != null) {
			// guess report format from filename extension
			if (reportFile.endsWith(".html")) {
				return "html";
			} else if (reportFile.endsWith(".txt")) {
				return "text";
			}
		}
		// use default report format
		return "text";
	}

	public void setReportFormat(String reportFormat) {
		this.reportFormat = reportFormat;
	}

	public String getReportFile() {
		return reportFile;
	}

	public void setReportFile(String reportFile) {
		this.reportFile = reportFile;
	}

	public boolean isDebug() {
		return debug;
	}

	public void setDebug(boolean debug) {
		this.debug = debug;
	}

}
