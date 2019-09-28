/*
 * Copyright 2019 Stephan Markwalder
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

package org.jarhc.pom;

import java.io.InputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ModelReader {

	public Model read(InputStream inputStream) throws ModelException {

		try {

			// parse XML document
			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
			Document document = documentBuilder.parse(inputStream);

			// prepare XPath helper
			XPathFactory xPathFactory = XPathFactory.newInstance();
			XPath xPath = xPathFactory.newXPath();

			// extract coordinates
			String groupId = xPath.evaluate("project/groupId", document);
			if (groupId.isEmpty()) {
				// inherit group ID from parent
				groupId = xPath.evaluate("project/parent/groupId", document);
			}
			String artifactId = xPath.evaluate("project/artifactId", document);
			String version = xPath.evaluate("project/version", document);
			if (version.isEmpty()) {
				// inherit version from parent
				version = xPath.evaluate("project/parent/version", document);
			}
			Model model = new Model(groupId, artifactId, version);

			// extract additional project information
			String name = xPath.evaluate("project/name", document);
			model.setName(name);
			String description = xPath.evaluate("project/description", document);
			model.setDescription(description.trim());

			// extract dependencies
			NodeList dependencyNodes = (NodeList) xPath.evaluate("project/dependencies/dependency", document, XPathConstants.NODESET);
			for (int n = 0; n < dependencyNodes.getLength(); n++) {
				Node dependencyNode = dependencyNodes.item(n);

				Dependency dependency = read(dependencyNode, xPath);
				model.addDependency(dependency);
			}

			return model;

		} catch (Exception e) {
			throw new ModelException("Unexpected error", e);
		}

	}

	private Dependency read(Node node, XPath xPath) throws XPathExpressionException {

		// extract coordinates
		String groupId = xPath.evaluate("groupId", node);
		String artifactId = xPath.evaluate("artifactId", node);
		String version = xPath.evaluate("version", node);

		// extract scope (default: "compile")
		String scope = xPath.evaluate("scope", node);
		if (scope.isEmpty()) scope = "compile";

		// extract optional (default: false)
		boolean optional = xPath.evaluate("optional", node).equals("true");

		return new Dependency(groupId, artifactId, version, Scope.parse(scope), optional);
	}

}
