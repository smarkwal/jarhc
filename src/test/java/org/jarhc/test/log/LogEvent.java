/*
 * Copyright 2021 Stephan Markwalder
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

import org.slf4j.Marker;
import org.slf4j.event.Level;
import org.slf4j.event.LoggingEvent;

class LogEvent implements LoggingEvent {

	private final Level level;
	private final Marker marker;
	private final String loggerName;
	private final String message;
	private final String threadName;
	private final Object[] argumentArray;
	private final long timeStamp;
	private final Throwable throwable;

	LogEvent(Level level, Marker marker, String loggerName, String message, Throwable throwable) {
		this.level = level;
		this.marker = marker;
		this.loggerName = loggerName;
		this.message = message;
		this.threadName = Thread.currentThread().getName();
		this.argumentArray = null;
		this.timeStamp = System.currentTimeMillis();
		this.throwable = throwable;
	}

	@Override
	public Level getLevel() {
		return level;
	}

	@Override
	public Marker getMarker() {
		return marker;
	}

	@Override
	public String getLoggerName() {
		return loggerName;
	}

	@Override
	public String getMessage() {
		return message;
	}

	@Override
	public String getThreadName() {
		return threadName;
	}

	@Override
	public Object[] getArgumentArray() {
		return argumentArray;
	}

	@Override
	public long getTimeStamp() {
		return timeStamp;
	}

	@Override
	public Throwable getThrowable() {
		return throwable;
	}

	@Override
	public String toString() {
		if (throwable == null) {
			return String.format("[%s] %s - %s", level, loggerName, message);
		} else {
			return String.format("[%s] %s - %s\n%s", level, loggerName, message, throwable);
		}
	}

	public boolean matches(LogEvent event) {
		if (event.level != level) return false;
		if (!event.loggerName.equals(loggerName)) return false;
		if (message.endsWith("*")) {
			String prefix = message.substring(0, message.length() - 1);
			if (!event.message.startsWith(prefix)) return false;
		} else {
			if (!event.message.equals(message)) return false;
		}
		if (throwable == null) {
			if (event.throwable != null) return false;
		} else {
			if (event.throwable == null) return false;
			if (!event.throwable.toString().equals(throwable.toString())) return false;
		}
		return true;
	}

}
