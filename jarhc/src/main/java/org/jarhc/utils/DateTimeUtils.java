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

package org.jarhc.utils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class DateTimeUtils {

	private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm");

	private DateTimeUtils() {
		throw new IllegalStateException("utility class");
	}

	public static String formatTimestamp(long timestamp) {
		Date date = new Date(timestamp);
		LocalDateTime dateTime = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
		return DATE_TIME_FORMAT.format(dateTime);
	}

	public static long getTimestamp() {
		String value = System.getProperty("jarhc.timestamp.override");
		if (value != null) {
			return Long.parseLong(value);
		}
		return System.currentTimeMillis();
	}

}
