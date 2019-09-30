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

package org.jarhc;

import org.jarhc.artifacts.Repository;
import org.jarhc.env.JavaRuntime;
import org.jarhc.pom.resolver.DependencyResolver;

public class Context {

	private final JavaRuntime javaRuntime;
	private final Repository repository;
	private final DependencyResolver dependencyResolver;

	public Context(JavaRuntime javaRuntime, Repository repository, DependencyResolver dependencyResolver) {
		this.javaRuntime = javaRuntime;
		this.repository = repository;
		this.dependencyResolver = dependencyResolver;
	}

	public JavaRuntime getJavaRuntime() {
		return javaRuntime;
	}

	public Repository getRepository() {
		return repository;
	}

	public DependencyResolver getDependencyResolver() {
		return dependencyResolver;
	}

}
