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

package org.jarhc.it;

class Spring5IT extends AbstractIT {

	Spring5IT() {
		super("org.springframework:spring-aop:5.1.3.RELEASE",
				"org.springframework:spring-beans:5.1.3.RELEASE",
				"org.springframework:spring-context:5.1.3.RELEASE",
				"org.springframework:spring-context-support:5.1.3.RELEASE",
				"org.springframework:spring-core:5.1.3.RELEASE",
				"org.springframework:spring-expression:5.1.3.RELEASE",
				"org.springframework:spring-jdbc:5.1.3.RELEASE",
				"org.springframework:spring-jms:5.1.3.RELEASE",
				"org.springframework:spring-messaging:5.1.3.RELEASE",
				"org.springframework:spring-orm:5.1.3.RELEASE",
				"org.springframework:spring-tx:5.1.3.RELEASE",
				"org.springframework:spring-web:5.1.3.RELEASE"
		);
	}

}
