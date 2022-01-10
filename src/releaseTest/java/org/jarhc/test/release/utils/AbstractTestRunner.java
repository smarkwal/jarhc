/*
 * Copyright 2022 Stephan Markwalder
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

package org.jarhc.test.release.utils;

import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractTestRunner {

	protected final File workDir;
	protected final File dataDir;
	protected final Map<String, File> files = new HashMap<>();

	public AbstractTestRunner(File workDir, File dataDir) {
		this.workDir = workDir;
		this.dataDir = dataDir;
	}

	public abstract String getName();

	public abstract String getJavaVendor();

	protected abstract String getJavaVersion();

	public void installFile(File file, String path) {
		files.put(path, file);
	}

	public String getOutputPath(String resourceName) {
		return "reports/" + resourceName;
	}

	public File getOutputFile(String resourceName) {
		return new File(workDir, getOutputPath(resourceName));
	}

	public String findResourcePath(String resourceName) {

		// try to find report in runner-specific test resources
		String reportPath = "reports/" + getJavaVendor() + "/" + getJavaVersion() + "/" + resourceName;
		if (resourceExists(reportPath)) {
			return reportPath;
		}

		// try to find report in Java version-specific test resources
		reportPath = "reports/all/" + getJavaVersion() + "/" + resourceName;
		if (resourceExists(reportPath)) {
			return reportPath;
		}

		// try to find report in generic test resources
		reportPath = "reports/all/all/" + resourceName;
		if (resourceExists(reportPath)) {
			return reportPath;
		}

		assumeTrue(resourceExists(reportPath), "Test resource found: " + reportPath);
		return reportPath;
	}

	private boolean resourceExists(String reportPath) {
		return AbstractTestRunner.class.getClassLoader().getResource(reportPath) != null;
	}

	public abstract TestResult execute(Command command);

}
