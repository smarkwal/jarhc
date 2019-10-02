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
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class POMReader {

	public POM read(InputStream inputStream) throws POMException {

		try {

			// parse XML document
			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
			documentBuilderFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
			DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
			Document document = documentBuilder.parse(inputStream);

			// prepare XPath helper
			XPathFactory xPathFactory = XPathFactory.newInstance();
			XPath xPath = xPathFactory.newXPath();

			// extract parent coordinates
			String parentGroupId = xPath.evaluate("project/parent/groupId", document);
			String parentArtifactId = xPath.evaluate("project/parent/artifactId", document);
			String parentVersion = xPath.evaluate("project/parent/version", document);

			// extract coordinates
			String groupId = xPath.evaluate("project/groupId", document);
			if (groupId.isEmpty()) {
				// inherit group ID from parent
				groupId = parentGroupId;
			}
			String artifactId = xPath.evaluate("project/artifactId", document);
			String version = xPath.evaluate("project/version", document);
			if (version.isEmpty()) {
				// inherit version from parent
				version = parentVersion;
			}

			// create POM with project and parent coordinates
			POM pom = new POM(groupId, artifactId, version);
			if (!parentGroupId.isEmpty() || !parentArtifactId.isEmpty()) {
				pom.setParent(parentGroupId, parentArtifactId, parentVersion);
			}

			// extract properties
			NodeList propertyNodes = (NodeList) xPath.evaluate("project/properties/*", document, XPathConstants.NODESET);
			for (int n = 0; n < propertyNodes.getLength(); n++) {
				Node propertyNode = propertyNodes.item(n);

				String propertyName = propertyNode.getNodeName();
				String propertyValue = propertyNode.getTextContent();
				pom.setProperty(propertyName, propertyValue);
			}

			// extract additional project information
			String name = xPath.evaluate("project/name", document);
			pom.setName(name);
			String description = xPath.evaluate("project/description", document).trim();
			pom.setDescription(description);

			// extract dependency management
			NodeList dependencyManagementNodes = (NodeList) xPath.evaluate("project/dependencyManagement/dependencies/dependency", document, XPathConstants.NODESET);
			for (int n = 0; n < dependencyManagementNodes.getLength(); n++) {
				Node dependencyNode = dependencyManagementNodes.item(n);

				Dependency dependency = read(dependencyNode, xPath);
				pom.addDependencyManagement(dependency);
			}

			// extract dependencies
			NodeList dependencyNodes = (NodeList) xPath.evaluate("project/dependencies/dependency", document, XPathConstants.NODESET);
			for (int n = 0; n < dependencyNodes.getLength(); n++) {
				Node dependencyNode = dependencyNodes.item(n);

				Dependency dependency = read(dependencyNode, xPath);
				pom.addDependency(dependency);
			}

			return pom;

		} catch (Exception e) {
			throw new POMException("Unexpected error", e);
		}

	}

	private Dependency read(Node node, XPath xPath) throws XPathExpressionException {

		// extract coordinates
		String groupId = xPath.evaluate("groupId", node);
		String artifactId = xPath.evaluate("artifactId", node);
		String version = xPath.evaluate("version", node);

		// extract type (default: "jar")
		String type = xPath.evaluate("type", node);
		if (type.isEmpty()) {
			type = "jar";
			// TODO: do something with this type (-> read documentation)
			//  example: camel-spring-2.17.7 has a dependency of type "test-jar"
		}

		// extract scope (default: "compile")
		String scope = xPath.evaluate("scope", node);
		if (scope.isEmpty()) scope = "compile";

		// extract optional (default: false)
		boolean optional = xPath.evaluate("optional", node).equals("true");

		// TODO: parse exclusions

		return new Dependency(groupId, artifactId, version, Scope.parse(scope), optional);
	}

}
