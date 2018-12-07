package org.jarcheck.it;

import org.junit.jupiter.api.Test;

import java.io.IOException;

class CommonsIT extends AbstractIT {

	@Test
	void test() throws IOException {

		String baseResourcePath = "/it/commons/";
		String[] fileNames = new String[]{
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
		};

		test(baseResourcePath, fileNames);
	}

}
