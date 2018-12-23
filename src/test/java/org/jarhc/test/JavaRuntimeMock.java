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

package org.jarhc.test;

import org.jarhc.TestUtils;
import org.jarhc.env.JavaRuntime;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class JavaRuntimeMock implements JavaRuntime {

	public static JavaRuntime createOracleRuntime() {
		return new JavaRuntimeMock("/classes-oracle-jdk-1.8.0_144.txt");
	}

	private final Set<String> classNames = new HashSet<>();

	/**
	 * Create a fake Java runtime using the class names loaded from the given resource.
	 *
	 * @param resource Resource with class names
	 */
	private JavaRuntimeMock(String resource) {
		try {
			List<String> lines = TestUtils.getResourceAsLines(resource, "UTF-8");
			for (String line : lines) {
				if (line.startsWith("#")) continue;
				classNames.add(line);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public String getName() {
		return "Java(TM) SE Runtime Environment";
	}

	@Override
	public String getJavaVersion() {
		return "1.8.0_144";
	}

	@Override
	public String getJavaVendor() {
		return "Oracle Corporation";
	}

	@Override
	public String getJavaHome() {
		return "/opt/java/jdk-1.8.0_144";
	}

	@Override
	public String getClassLoaderName(String className) {
		if (classNames.contains(className)) {
			return "Bootstrap";
		} else {
			return null;
		}
	}

}
