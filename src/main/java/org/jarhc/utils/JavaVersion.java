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

package org.jarhc.utils;

public class JavaVersion {

	private JavaVersion() {
		throw new IllegalStateException("utility class");
	}

	/**
	 * Minimum class version (file format) 45 for Java 1.1.
	 */
	public static final int MIN_CLASS_VERSION = 45;

	/**
	 * Get a human readable Java version string for the given class version.
	 *
	 * @param classVersion Class version
	 * @return Java version string
	 */
	public static String fromClassVersion(int classVersion) {
		int version = getJavaVersionNumber(classVersion);
		if (version < 1) {
			return String.format("[unknown:%d]", classVersion);
		} else if (version < 5) {
			return String.format("Java 1.%d", version);
		} else {
			return String.format("Java %d", version);
		}
	}

	public static int getJavaVersionNumber(int classVersion) {
		return classVersion - MIN_CLASS_VERSION + 1;
	}

}
