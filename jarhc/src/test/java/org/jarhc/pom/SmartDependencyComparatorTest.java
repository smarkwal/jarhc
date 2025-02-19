/*
 * Copyright 2025 Stephan Markwalder
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

import static java.util.Collections.shuffle;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.jarhc.artifacts.Artifact;
import org.junit.jupiter.api.Test;

public class SmartDependencyComparatorTest {

	@Test
	void test() {

		// prepare
		Artifact artifact = new Artifact("org.apache:commons-lang:1.0.0");
		SmartDependencyComparator comparator = new SmartDependencyComparator(artifact);
		List<Dependency> dependencies = Arrays.asList(
				new Dependency("org.apache:commons-lang:1.0.0", Scope.COMPILE, false),
				new Dependency("org.apache:commons-lang-i8n:1.0.0", Scope.COMPILE, false),
				new Dependency("org.apache:commons-lang3:3.0.0", Scope.COMPILE, false),
				new Dependency("org.apache:commons-aop:1.0.0", Scope.COMPILE, false),
				new Dependency("org.apache:commons-text:1.0.0", Scope.COMPILE, false),
				new Dependency("org.apache:ant:1.0.0", Scope.COMPILE, false),
				new Dependency("org.apache:commons.api:1.0.0", Scope.COMPILE, false),
				new Dependency("org.apache:commons.impl:1.0.0", Scope.COMPILE, false),
				new Dependency("org.apache:xerces:1.0.0", Scope.COMPILE, false),
				new Dependency("org.apache.http:http-client:1.0.0", Scope.COMPILE, false),
				new Dependency("org:aaa:1.0.0", Scope.COMPILE, false),
				new Dependency("org:zzz:1.0.0", Scope.COMPILE, false),
				new Dependency("com:zzz:1.0.0", Scope.COMPILE, false),
				new Dependency("net:zzz:1.0.0", Scope.COMPILE, false),
				new Dependency("web:commons:1.0.0", Scope.COMPILE, false),
				new Dependency("web:commons:1.0.0", Scope.COMPILE, true),
				new Dependency("web:commons:1.0.0", Scope.PROVIDED, false),
				new Dependency("web:commons:1.0.0", Scope.PROVIDED, true),
				new Dependency("web:commons:1.0.0", Scope.RUNTIME, false),
				new Dependency("web:commons:1.0.0", Scope.RUNTIME, true),
				new Dependency("web:commons:1.0.0", Scope.TEST, false),
				new Dependency("web:commons:1.0.0", Scope.TEST, true),
				new Dependency("web:commons:1.0.0", Scope.SYSTEM, false),
				new Dependency("web:commons:1.0.0", Scope.SYSTEM, true),
				new Dependency("web:commons:1.0.0", Scope.IMPORT, false),
				new Dependency("web:commons:1.0.0", Scope.IMPORT, true),
				new Dependency("web:commons:2.0.0", Scope.COMPILE, false)
		);

		// shuffle list of dependencies
		List<Dependency> temp = new ArrayList<>(dependencies);
		shuffle(temp);

		// test
		temp.sort(comparator);

		// assert
		assertEquals(dependencies, temp);
	}

}
