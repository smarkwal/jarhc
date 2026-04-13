/*
 * Copyright 2025 Stephan Markwalder
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

package org.jarhc.test.log;

import java.lang.reflect.Field;
import org.slf4j.Logger;

/**
 * Utility class to silence an SLF4J {@link Logger}.
 */
public class LoggerSilencer {

	private static final Field CURRENT_LOG_LEVEL_FIELD = findLogLevelField();

	/**
	 * Silences the given logger by setting its log level to {@code Integer.MAX_VALUE},
	 * runs the given runnable, and finally restores the original log level.
	 *
	 * @param logger   Logger to silence
	 * @param runnable Code to run while the logger is disabled
	 */
	public static void run(Logger logger, Runnable runnable) {
		int originalLogLevel = getLogLevel(logger);
		setLogLevel(logger, Integer.MAX_VALUE);
		try {
			runnable.run();
		} finally {
			setLogLevel(logger, originalLogLevel);
		}
	}

	private LoggerSilencer() {
		throw new UnsupportedOperationException("utility class");
	}

	private static int getLogLevel(Logger logger) {
		try {
			return CURRENT_LOG_LEVEL_FIELD.getInt(logger);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	private static void setLogLevel(Logger logger, int logLevel) {
		try {
			CURRENT_LOG_LEVEL_FIELD.setInt(logger, logLevel);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Get field "currentLogLevel" from class "org.slf4j.simple.SimpleLogger".
	 */
	private static Field findLogLevelField() {
		try {
			Class<?> clazz = Class.forName("org.slf4j.simple.SimpleLogger");
			Field field = clazz.getDeclaredField("currentLogLevel");
			field.setAccessible(true);
			return field;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
