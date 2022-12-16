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
import org.slf4j.helpers.NOPLogger;

public class LoggerBuilder {

	public static Logger collect(Class<?> clazz) {
		return new CollectLogger(clazz.getSimpleName());
	}

	public static Logger noop() {
		return NOPLogger.NOP_LOGGER;
	}

	public static Logger reject(Class<?> clazz) {
		return new RejectLogger(clazz.getSimpleName());
	}

}
