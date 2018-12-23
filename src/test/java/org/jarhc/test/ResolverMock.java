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

package org.jarhc.test;

import org.jarhc.TestUtils;
import org.jarhc.artifacts.Artifact;
import org.jarhc.artifacts.Resolver;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ResolverMock implements Resolver {

	public static Resolver createResolver() {
		return new ResolverMock("/resolver.properties");
	}

	private final Properties properties = new Properties();

	private ResolverMock(String resource) {
		try {
			try (InputStream stream = TestUtils.getResourceAsStream(resource)) {
				properties.load(stream);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Artifact getArtifact(String checksum) {
		String coordinates = properties.getProperty("artifact." + checksum);
		if (coordinates != null) {
			return new Artifact(coordinates);
		} else {
			// artifact not found
			return null;
		}
	}

}
