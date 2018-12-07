package org.jarcheck.it;

import org.junit.jupiter.api.Test;

import java.io.IOException;

class XercesIT extends AbstractIT {

	@Test
	void test() throws IOException {

		String baseResourcePath = "/it/xerces/";
		String[] fileNames = new String[]{
				"xercesImpl-2.11.0.jar",
				"xml-apis-1.4.01.jar",
				"xml-resolver-1.2.jar"
		};

		test(baseResourcePath, fileNames);
	}

}
