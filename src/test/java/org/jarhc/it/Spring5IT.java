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
