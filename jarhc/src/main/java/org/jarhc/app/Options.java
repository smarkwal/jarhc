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
import java.util.Properties;
import org.jarhc.artifacts.MavenRepository;
import org.jarhc.java.ClassLoaderStrategy;
import org.jarhc.utils.JavaUtils;

public class Options implements MavenRepository.Settings {

	public static final String MAVEN_CENTRAL_URL = "https://repo1.maven.org/maven2/";

	private static final String REPOSITORY_URL = "repository.url";
	private static final String REPOSITORY_USERNAME = "repository.username";
	private static final String REPOSITORY_PASSWORD = "repository.password";

	private static final String DEFAULT_TITLE = "JAR Health Check Report";

	private final Command command;

	// general options
	private boolean debug = false;
	private boolean trace = false;

	// scan options
	private int release = JavaUtils.getJavaVersion();
	private final List<String> classpathJarPaths = new ArrayList<>();
	private final List<String> providedJarPaths = new ArrayList<>();
	private final List<String> runtimeJarPaths = new ArrayList<>();
	private ClassLoaderStrategy classLoaderStrategy = ClassLoaderStrategy.ParentLast;
	private boolean ignoreMissingAnnotations = false;
	private boolean ignoreExactCopy = false;
	private String repositoryUrl = MAVEN_CENTRAL_URL;
	private String repositoryUsername = null;
	private String repositoryPassword = null;
	private boolean skipEmpty = false;
	private String dataPath = null;

	// diff options
	private String input1 = null;
	private String input2 = null;

	// report options
	private final List<String> reportFiles = new ArrayList<>();
	private String reportTitle = DEFAULT_TITLE;
	private List<String> sections = null; // all sections

	public Options() {
		this(Command.SCAN, new Properties());
	}

	public Options(Command command, Properties properties) {
		this.command = command;

		if (properties.containsKey(REPOSITORY_URL)) {
			this.repositoryUrl = properties.getProperty(REPOSITORY_URL);
		}
		if (properties.containsKey(REPOSITORY_USERNAME)) {
			this.repositoryUsername = properties.getProperty(REPOSITORY_USERNAME);
		}
		if (properties.containsKey(REPOSITORY_PASSWORD)) {
			this.repositoryPassword = properties.getProperty(REPOSITORY_PASSWORD);
		}
	}

	public Command getCommand() {
		return command;
	}

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

	public String getRepositoryUrl() {
		return repositoryUrl;
	}

	public void setRepositoryUrl(String repositoryUrl) {
		this.repositoryUrl = repositoryUrl;
	}

	public String getRepositoryUsername() {
		return repositoryUsername;
	}

	public void setRepositoryUsername(String repositoryUsername) {
		this.repositoryUsername = repositoryUsername;
	}

	public String getRepositoryPassword() {
		return repositoryPassword;
	}

	public void setRepositoryPassword(String repositoryPassword) {
		this.repositoryPassword = repositoryPassword;
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

	//-------------------------------------------------------------------------

	public String getInput1() {
		return input1;
	}

	public void setInput1(String input1) {
		this.input1 = input1;
	}

	public String getInput2() {
		return input2;
	}

	public void setInput2(String input2) {
		this.input2 = input2;
	}

	//-------------------------------------------------------------------------

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

	// ------------------------------------------------------------------------

	public enum Command {
		SCAN,
		DIFF
	}

}
