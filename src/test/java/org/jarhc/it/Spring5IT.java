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
		super("/it/spring5/", new String[]{
				"spring-aop-5.1.3.RELEASE.jar",
				"spring-beans-5.1.3.RELEASE.jar",
				"spring-context-5.1.3.RELEASE.jar",
				"spring-context-support-5.1.3.RELEASE.jar",
				"spring-core-5.1.3.RELEASE.jar",
				"spring-expression-5.1.3.RELEASE.jar",
				"spring-jdbc-5.1.3.RELEASE.jar",
				"spring-jms-5.1.3.RELEASE.jar",
				"spring-messaging-5.1.3.RELEASE.jar",
				"spring-orm-5.1.3.RELEASE.jar",
				"spring-tx-5.1.3.RELEASE.jar",
				"spring-web-5.1.3.RELEASE.jar"
		});
	}

}
