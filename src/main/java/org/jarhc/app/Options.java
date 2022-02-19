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

import java.util.ArrayList;
import java.util.List;
import org.jarhc.java.ClassLoaderStrategy;
import org.jarhc.utils.JavaUtils;

public class Options {

	private int release = JavaUtils.getJavaVersion();

	private final List<String> classpathJarPaths = new ArrayList<>();
	private final List<String> providedJarPaths = new ArrayList<>();
	private final List<String> runtimeJarPaths = new ArrayList<>();
	private ClassLoaderStrategy classLoaderStrategy = ClassLoaderStrategy.ParentLast;
	private boolean removeVersion = false;
	private boolean useArtifactName = false;
	private boolean ignoreMissingAnnotations = false;
	private boolean ignoreExactCopy = false;

	private List<String> sections = null; // all sections
	private boolean skipEmpty = false;

	private String reportTitle = "JAR Health Check Report";
	private final List<String> reportFiles = new ArrayList<>();

	private String dataPath = null;
	private boolean debug = false;
	private boolean trace = false;

	public int getRelease() {
		return release;
	}

	public void setRelease(int release) {
		this.release = release;
	}

	public List<String> getClasspathJarPaths() {
		return classpathJarPaths;
	}

	public void addClasspathJarPath(String path) {
		this.classpathJarPaths.add(path);
	}

	public List<String> getProvidedJarPaths() {
		return providedJarPaths;
	}

	public void addProvidedJarPath(String path) {
		this.providedJarPaths.add(path);
	}

	public List<String> getRuntimeJarPaths() {
		return runtimeJarPaths;
	}

	public void addRuntimeJarPath(String path) {
		this.runtimeJarPaths.add(path);
	}

	public ClassLoaderStrategy getClassLoaderStrategy() {
		return classLoaderStrategy;
	}

	public void setClassLoaderStrategy(ClassLoaderStrategy classLoaderStrategy) {
		this.classLoaderStrategy = classLoaderStrategy;
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

	public boolean isIgnoreMissingAnnotations() {
		return ignoreMissingAnnotations;
	}

	public void setIgnoreMissingAnnotations(boolean ignoreMissingAnnotations) {
		this.ignoreMissingAnnotations = ignoreMissingAnnotations;
	}

	public boolean isIgnoreExactCopy() {
		return ignoreExactCopy;
	}

	public void setIgnoreExactCopy(boolean ignoreExactCopy) {
		this.ignoreExactCopy = ignoreExactCopy;
	}

	public List<String> getSections() {
		return sections;
	}

	public void setSections(List<String> sections) {
		this.sections = sections;
	}

	public boolean isSkipEmpty() {
		return skipEmpty;
	}

	public void setSkipEmpty(boolean skipEmpty) {
		this.skipEmpty = skipEmpty;
	}

	public String getReportTitle() {
		return reportTitle;
	}

	public void setReportTitle(String reportTitle) {
		this.reportTitle = reportTitle;
	}

	public void addReportFile(String reportFile) {
		this.reportFiles.add(reportFile);
	}

	public List<String> getReportFiles() {
		return reportFiles;
	}

	public String getDataPath() {
		return dataPath;
	}

	public void setDataPath(String dataPath) {
		this.dataPath = dataPath;
	}

	public boolean isDebug() {
		return debug;
	}

	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	public boolean isTrace() {
		return trace;
	}

	public void setTrace(boolean trace) {
		this.trace = trace;
	}

}
