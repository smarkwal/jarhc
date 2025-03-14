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

package org.jarhc.loader;

public class FileNameNormalizer {

	private FileNameNormalizer() {
		throw new IllegalStateException("utility class");
	}

	/**
	 * Removes the version number from the given file name.
	 * <p>
	 * Example: "asm-7.0.jar" becomes "asm.jar"
	 *
	 * @param fileName Original file name
	 * @return File name without version number
	 */
	public static String getFileNameWithoutVersionNumber(String fileName) {
		return fileName.replaceAll("-[0-9]+(\\.[0-9]+){0,10}(-SNAPSHOT)?", "");
	}

}
