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

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.event.Level;

@SuppressWarnings("unused")
public class LoggerAssertions {

	public static LoggerAssertions assertLogger(Logger logger) {
		return new LoggerAssertions(logger);
	}

	private final String loggerName;
	private boolean ordered = true;
	private final List<LogEvent> events;

	private LoggerAssertions(Logger logger) {
		this.loggerName = logger.getName();
		if (logger instanceof CollectLogger) {
			CollectLogger memoryLogger = (CollectLogger) logger;
			this.events = memoryLogger.getEvents(); // required: direct access to internal list !!!
		} else {
			throw new IllegalArgumentException("Logger is not a MemoryLogger: " + logger);
		}
	}

	public LoggerAssertions inAnyOrder() {
		ordered = false;
		return this;
	}

	public LoggerAssertions hasLog(Level level, String message, Throwable throwable) {

		LogEvent expectedEvent = toEvent(level, message, throwable);

		// check if there are any events at all
		if (events.isEmpty()) {
			String msg = String.format("Log event not found.\nExpected:\n%s", expectedEvent);
			throw new AssertionError(msg);
		}

		LogEvent event;
		if (ordered) {

			// get next event
			event = events.get(0);

			// compare expected and actual event
			if (!expectedEvent.matches(event)) {
				String msg = String.format("Unexpected log event found.\nExpected:\n%s\nActual:\n%s", expectedEvent, event);
				throw new AssertionError(msg);
			}

		} else {

			// search for event
			event = events.stream().filter(expectedEvent::matches).findFirst().orElse(null);

			// check if event has been found
			if (event == null) {
				String msg = String.format("Log event not found.\nExpected:\n%s", expectedEvent);
				throw new AssertionError(msg);
			}

		}

		// remove event
		events.remove(event);

		return this;
	}

	public void isEmpty() {
		if (!events.isEmpty()) {
			StringBuilder buffer = new StringBuilder("Unexpected log events found.\n");
			for (LogEvent event : events) {
				buffer.append(event).append("\n");
			}
			throw new AssertionError(buffer.toString());
		}
	}

	public LoggerAssertions hasTrace(String message) {
		return hasLog(Level.TRACE, message, null);
	}

	public LoggerAssertions hasTrace(String message, Throwable throwable) {
		return hasLog(Level.TRACE, message, throwable);
	}

	public LoggerAssertions hasDebug(String message) {
		return hasLog(Level.DEBUG, message, null);
	}

	public LoggerAssertions hasDebug(String message, Throwable throwable) {
		return hasLog(Level.DEBUG, message, throwable);
	}

	public LoggerAssertions hasInfo(String message) {
		return hasLog(Level.INFO, message, null);
	}

	public LoggerAssertions hasInfo(String message, Throwable throwable) {
		return hasLog(Level.INFO, message, throwable);
	}

	public LoggerAssertions hasWarn(String message) {
		return hasLog(Level.WARN, message, null);
	}

	public LoggerAssertions hasWarn(String message, Throwable throwable) {
		return hasLog(Level.WARN, message, throwable);
	}

	public LoggerAssertions hasError(String message) {
		return hasLog(Level.ERROR, message, null);
	}

	public LoggerAssertions hasError(String message, Throwable throwable) {
		return hasLog(Level.ERROR, message, throwable);
	}

	private LogEvent toEvent(Level level, String message, Throwable throwable) {
		return new LogEvent(level, null, loggerName, message, throwable);
	}

}
