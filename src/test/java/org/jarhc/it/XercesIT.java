package org.jarhc.it;

class XercesIT extends AbstractIT {

	XercesIT() {
		super("/it/xerces/", new String[]{
				"xercesImpl-2.11.0.jar",
				"xml-apis-1.4.01.jar",
				"xml-resolver-1.2.jar"
		});
	}

}
