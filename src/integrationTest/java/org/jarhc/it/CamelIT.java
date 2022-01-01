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

class CamelIT extends AbstractIT {

	CamelIT() {
		super("org.apache.camel:camel-core:2.17.7",
				"org.apache.camel:camel-jdbc:2.17.7",
				"org.apache.camel:camel-jms:2.17.7",
				"org.apache.camel:camel-jmx:2.17.7",
				"org.apache.camel:camel-jxpath:2.17.7",
				"org.apache.camel:camel-ldap:2.17.7",
				"org.apache.camel:camel-mail:2.17.7",
				"org.apache.camel:camel-soap:2.17.7",
				"org.apache.camel:camel-spring:2.17.7"
		);
	}

}
