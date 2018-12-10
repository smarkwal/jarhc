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
		super("/it/commons/", new String[]{
				"commons-beanutils-1.9.2.jar",
				"commons-betwixt-0.8.jar",
				"commons-codec-1.10.jar",
				"commons-collections-3.2.2.jar",
				"commons-compress-1.5.jar",
				"commons-configuration-1.9.jar",
				"commons-dbcp2-2.5.0.jar",
				"commons-dbutils-1.5.jar",
				"commons-digester-2.1.jar",
				"commons-email-1.5.jar",
				"commons-fileupload-1.3.3.jar",
				"commons-io-2.4.jar",
				"commons-jxpath-1.3.jar",
				"commons-lang-2.6.jar",
				"commons-logging-1.2.jar",
				"commons-net-3.3.jar",
				"commons-pool2-2.6.0.jar"
		});
	}

}
