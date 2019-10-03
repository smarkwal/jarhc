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

class CommonsIT extends AbstractIT {

	CommonsIT() {
		super("commons-beanutils:commons-beanutils:1.9.2",
				"commons-betwixt:commons-betwixt:0.8",
				"commons-codec:commons-codec:1.10",
				"commons-collections:commons-collections:3.2.2",
				"org.apache.commons:commons-compress:1.5",
				"commons-configuration:commons-configuration:1.9",
				"org.apache.commons:commons-dbcp2:2.5.0",
				"commons-dbutils:commons-dbutils:1.5",
				"commons-digester:commons-digester:2.1",
				"org.apache.commons:commons-email:1.5",
				"commons-fileupload:commons-fileupload:1.3.3",
				"commons-io:commons-io:2.4",
				"commons-jxpath:commons-jxpath:1.3",
				"commons-lang:commons-lang:2.6",
				"commons-logging:commons-logging:1.2",
				"commons-net:commons-net:3.3",
				"org.apache.commons:commons-pool2:2.6.0"
		);
	}

}
