package org.jarhc.it;

class CamelIT extends AbstractIT {

	CamelIT() {
		super("/it/camel/", new String[]{
				"camel-core-2.17.7.jar",
				"camel-jdbc-2.17.7.jar",
				"camel-jms-2.17.7.jar",
				"camel-jmx-2.17.7.jar",
				"camel-jxpath-2.17.7.jar",
				"camel-ldap-2.17.7.jar",
				"camel-mail-2.17.7.jar",
				"camel-soap-2.17.7.jar",
				"camel-spring-2.17.7.jar"
		});
	}

}
