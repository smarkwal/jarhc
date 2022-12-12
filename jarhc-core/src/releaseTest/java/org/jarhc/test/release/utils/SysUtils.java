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

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SysUtils {

	private static final Logger LOGGER = LoggerFactory.getLogger(SysUtils.class);

	/**
	 * UID and GID of root user (used as fallback).
	 */
	private static final String ROOT = "0";

	private static String UID;
	private static String GID;

	private SysUtils() {
	}

	/**
	 * Get username of current user.
	 *
	 * @return Username
	 */
	public static String getUsername() {
		return System.getProperty("user.name");
	}

	/**
	 * Get UID of current user.
	 *
	 * @return UID
	 */
	public static String getUID() {
		if (UID == null) {
			String username = getUsername();
			String output = runSysCommand(new String[] { "id", "-u", username });
			if (!output.matches("[0-9]+")) {
				LOGGER.warn("Unexpected UID: " + output);
				output = ROOT;
			}
			UID = output;
		}
		return UID;
	}

	/**
	 * Get GID of current user.
	 *
	 * @return GID
	 */
	public static String getGID() {
		if (GID == null) {
			String username = getUsername();
			String output = runSysCommand(new String[] { "id", "-g", username });
			if (!output.matches("[0-9]+")) {
				LOGGER.warn("Unexpected GID: " + output);
				output = ROOT;
			}
			GID = output;
		}
		return GID;
	}

	/**
	 * Run the given system command and return its output.
	 *
	 * @param command System command.
	 * @return Output
	 */
	private static String runSysCommand(String[] command) {
		try {
			Runtime runtime = Runtime.getRuntime();
			Process process = runtime.exec(command);
			try (InputStream stream = process.getInputStream()) {
				String output = IOUtils.toString(stream, StandardCharsets.US_ASCII);
				return output.trim();
			}
		} catch (IOException e) {
			LOGGER.error("Unexpected I/O error.", e);
			return ROOT;
		}
	}

}
