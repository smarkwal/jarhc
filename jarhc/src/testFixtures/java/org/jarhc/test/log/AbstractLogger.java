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

import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.event.Level;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MessageFormatter;

/**
 * Abstract implementation of SLF4J Logger used to capture and verify log events.
 */
@SuppressWarnings("unused")
abstract class AbstractLogger implements Logger {

	private final String loggerName;
	private Level threshold = null;

	AbstractLogger(String loggerName) {
		this.loggerName = loggerName;
	}

	void setThreshold(Level threshold) {
		this.threshold = threshold;
	}

	protected abstract void log(LogEvent event);

	// Logger interface --------------------------------------------------------

	@Override
	public String getName() {
		return loggerName;
	}

	@Override
	public boolean isTraceEnabled() {
		return isEnabled(Level.TRACE, null);
	}

	@Override
	public void trace(String msg) {
		log(Level.TRACE, null, msg);
	}

	@Override
	public void trace(String format, Object arg) {
		log(Level.TRACE, null, format, arg);
	}

	@Override
	public void trace(String format, Object arg1, Object arg2) {
		log(Level.TRACE, null, format, arg1, arg2);
	}

	@Override
	public void trace(String format, Object... arguments) {
		log(Level.TRACE, null, format, arguments);
	}

	@Override
	public void trace(String msg, Throwable t) {
		log(Level.TRACE, null, msg, t);
	}

	@Override
	public boolean isTraceEnabled(Marker marker) {
		return isEnabled(Level.TRACE, marker);
	}

	@Override
	public void trace(Marker marker, String msg) {
		log(Level.TRACE, marker, msg);
	}

	@Override
	public void trace(Marker marker, String format, Object arg) {
		log(Level.TRACE, marker, format, arg);
	}

	@Override
	public void trace(Marker marker, String format, Object arg1, Object arg2) {
		log(Level.TRACE, marker, format, arg1, arg2);
	}

	@Override
	public void trace(Marker marker, String format, Object... arguments) {
		log(Level.TRACE, marker, format, arguments);
	}

	@Override
	public void trace(Marker marker, String msg, Throwable t) {
		log(Level.TRACE, marker, msg, t);
	}

	@Override
	public boolean isDebugEnabled() {
		return isEnabled(Level.DEBUG, null);
	}

	@Override
	public void debug(String msg) {
		log(Level.DEBUG, null, msg);
	}

	@Override
	public void debug(String format, Object arg) {
		log(Level.DEBUG, null, format, arg);
	}

	@Override
	public void debug(String format, Object arg1, Object arg2) {
		log(Level.DEBUG, null, format, arg1, arg2);
	}

	@Override
	public void debug(String format, Object... arguments) {
		log(Level.DEBUG, null, format, arguments);
	}

	@Override
	public void debug(String msg, Throwable t) {
		log(Level.DEBUG, null, msg, t);
	}

	@Override
	public boolean isDebugEnabled(Marker marker) {
		return isEnabled(Level.DEBUG, marker);
	}

	@Override
	public void debug(Marker marker, String msg) {
		log(Level.DEBUG, marker, msg);
	}

	@Override
	public void debug(Marker marker, String format, Object arg) {
		log(Level.DEBUG, marker, format, arg);
	}

	@Override
	public void debug(Marker marker, String format, Object arg1, Object arg2) {
		log(Level.DEBUG, marker, format, arg1, arg2);
	}

	@Override
	public void debug(Marker marker, String format, Object... arguments) {
		log(Level.DEBUG, marker, format, arguments);
	}

	@Override
	public void debug(Marker marker, String msg, Throwable t) {
		log(Level.DEBUG, marker, msg, t);
	}

	@Override
	public boolean isInfoEnabled() {
		return isEnabled(Level.INFO, null);
	}

	@Override
	public void info(String msg) {
		log(Level.INFO, null, msg);
	}

	@Override
	public void info(String format, Object arg) {
		log(Level.INFO, null, format, arg);
	}

	@Override
	public void info(String format, Object arg1, Object arg2) {
		log(Level.INFO, null, format, arg1, arg2);
	}

	@Override
	public void info(String format, Object... arguments) {
		log(Level.INFO, null, format, arguments);
	}

	@Override
	public void info(String msg, Throwable t) {
		log(Level.INFO, null, msg, t);
	}

	@Override
	public boolean isInfoEnabled(Marker marker) {
		return isEnabled(Level.INFO, marker);
	}

	@Override
	public void info(Marker marker, String msg) {
		log(Level.INFO, marker, msg);
	}

	@Override
	public void info(Marker marker, String format, Object arg) {
		log(Level.INFO, marker, format, arg);
	}

	@Override
	public void info(Marker marker, String format, Object arg1, Object arg2) {
		log(Level.INFO, marker, format, arg1, arg2);
	}

	@Override
	public void info(Marker marker, String format, Object... arguments) {
		log(Level.INFO, marker, format, arguments);
	}

	@Override
	public void info(Marker marker, String msg, Throwable t) {
		log(Level.INFO, marker, msg, t);
	}

	@Override
	public boolean isWarnEnabled() {
		return isEnabled(Level.WARN, null);
	}

	@Override
	public void warn(String msg) {
		log(Level.WARN, null, msg);
	}

	@Override
	public void warn(String format, Object arg) {
		log(Level.WARN, null, format, arg);
	}

	@Override
	public void warn(String format, Object arg1, Object arg2) {
		log(Level.WARN, null, format, arg1);
	}

	@Override
	public void warn(String format, Object... arguments) {
		log(Level.WARN, null, format, arguments);
	}

	@Override
	public void warn(String msg, Throwable t) {
		log(Level.WARN, null, msg, t);
	}

	@Override
	public boolean isWarnEnabled(Marker marker) {
		return isEnabled(Level.WARN, marker);
	}

	@Override
	public void warn(Marker marker, String msg) {
		log(Level.WARN, marker, msg);
	}

	@Override
	public void warn(Marker marker, String format, Object arg) {
		log(Level.WARN, marker, format, arg);
	}

	@Override
	public void warn(Marker marker, String format, Object arg1, Object arg2) {
		log(Level.WARN, marker, format, arg1, arg2);
	}

	@Override
	public void warn(Marker marker, String format, Object... arguments) {
		log(Level.WARN, marker, format, arguments);
	}

	@Override
	public void warn(Marker marker, String msg, Throwable t) {
		log(Level.WARN, marker, msg, t);
	}

	@Override
	public boolean isErrorEnabled() {
		return isEnabled(Level.ERROR, null);
	}

	@Override
	public void error(String msg) {
		log(Level.ERROR, null, msg);
	}

	@Override
	public void error(String format, Object arg) {
		log(Level.ERROR, null, format, arg);
	}

	@Override
	public void error(String format, Object arg1, Object arg2) {
		log(Level.ERROR, null, format, arg1, arg2);
	}

	@Override
	public void error(String format, Object... arguments) {
		log(Level.ERROR, null, format, arguments);
	}

	@Override
	public void error(String msg, Throwable t) {
		log(Level.ERROR, null, msg, t);
	}

	@Override
	public boolean isErrorEnabled(Marker marker) {
		return isEnabled(Level.ERROR, marker);
	}

	@Override
	public void error(Marker marker, String msg) {
		log(Level.ERROR, marker, msg);
	}

	@Override
	public void error(Marker marker, String format, Object arg) {
		log(Level.ERROR, marker, format, arg);
	}

	@Override
	public void error(Marker marker, String format, Object arg1, Object arg2) {
		log(Level.ERROR, marker, format, arg1, arg2);
	}

	@Override
	public void error(Marker marker, String format, Object... arguments) {
		log(Level.ERROR, marker, format, arguments);
	}

	@Override
	public void error(Marker marker, String msg, Throwable t) {
		log(Level.ERROR, marker, msg, t);
	}

	// private methods ---------------------------------------------------------

	private boolean isEnabled(Level level, Marker marker) {
		if (threshold == null) return true;
		return level.toInt() >= threshold.toInt();
	}

	private void log(Level level, Marker marker, String msg) {
		log(level, marker, msg, (Throwable) null);
	}

	private void log(Level level, Marker marker, String msg, Object arg) {
		FormattingTuple tuple = MessageFormatter.format(msg, arg);
		log(level, marker, tuple.getMessage(), tuple.getThrowable());
	}

	private void log(Level level, Marker marker, String msg, Object arg1, Object arg2) {
		FormattingTuple tuple = MessageFormatter.format(msg, arg1, arg2);
		log(level, marker, tuple.getMessage(), tuple.getThrowable());
	}

	private void log(Level level, Marker marker, String msg, Object... arguments) {
		FormattingTuple tuple = MessageFormatter.arrayFormat(msg, arguments);
		log(level, marker, tuple.getMessage(), tuple.getThrowable());
	}

	private synchronized void log(Level level, Marker marker, String msg, Throwable t) {
		if (isEnabled(level, marker)) {
			LogEvent event = new LogEvent(level, marker, getName(), msg, t);
			log(event);
		}
	}

}
