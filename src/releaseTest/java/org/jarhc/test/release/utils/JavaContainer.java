/*
 * Copyright 2021 Stephan Markwalder
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

package org.jarhc.test.release.utils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

public class JavaContainer extends GenericContainer<JavaContainer> {

	public JavaContainer(String javaImageName) {
		super(DockerImageName.parse(javaImageName));
	}

	public ExecResult exec(Command command) {
		try {
			return execInContainer(StandardCharsets.UTF_8, command.build());
		} catch (IOException e) {
			throw new AssertionError("Unexpected I/O error.", e);
		} catch (InterruptedException e) {
			throw new AssertionError("Unexpected interruption.", e);
		}
	}

}
