package org.jarcheck.it;

import org.junit.jupiter.api.Test;

import java.io.IOException;

class CamelIT extends AbstractIT {

	@Test
	void test() throws IOException {

		String baseResourcePath = "/it/camel/";
		String[] fileNames = new String[]{
				"camel-core-2.17.7.jar",
				"camel-jdbc-2.17.7.jar",
				"camel-jms-2.17.7.jar",
				"camel-jmx-2.17.7.jar",
				"camel-jxpath-2.17.7.jar",
				"camel-ldap-2.17.7.jar",
				"camel-mail-2.17.7.jar",
				"camel-soap-2.17.7.jar",
				"camel-spring-2.17.7.jar"
		};

		test(baseResourcePath, fileNames);
	}

}
