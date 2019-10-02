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

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class POMEvaluatorTest {

	private final POMEvaluator evaluator = new POMEvaluator();

	@Test
	void evaluatePOM() {

		// prepare
		POM pom = new POM("org.jarhc", "jarhc-gui", "1.0");
		pom.setParent("org.jarhc", "jarhc-parent", "1.0");
		pom.setProperty("junit.version", "4.1");
		pom.addDependency(new Dependency("org.jarhc", "jarhc", "${project.version}", Scope.COMPILE, false));
		pom.addDependency(new Dependency("junit", "junit", "${junit.version}", Scope.TEST, false));
		pom.addDependency(new Dependency("devops", "cpu-monitor", "${devops.version}", Scope.PROVIDED, true));
		pom.addDependency(new Dependency("agent", "jvm-dbg", "", Scope.SYSTEM, true));

		// test
		evaluator.evaluatePOM(pom);

		// assert
		assertEquals("1.0", pom.getDependencies().get(0).getVersion());
		assertEquals("4.1", pom.getDependencies().get(1).getVersion());
		assertEquals("${devops.version}", pom.getDependencies().get(2).getVersion());
		assertEquals("?", pom.getDependencies().get(3).getVersion());

	}

}