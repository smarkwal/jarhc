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

package org.jarhc.it;

import org.jarhc.it.utils.MavenProxyServerExtension;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(MavenProxyServerExtension.class)
class LoggingLibsTest extends AbstractMainTest {

	LoggingLibsTest() {
		super(
				"report.html",
				"--isolated-scan",
				"--title", "Java Logging Libraries",
				"--classpath", "org.slf4j:slf4j-api:2.0.17",
				"--classpath", "org.apache.logging.log4j:log4j-api:2.24.3",
				"--classpath", "ch.qos.logback:logback-core:1.5.18",
				"--skip-empty"
		);
	}

}