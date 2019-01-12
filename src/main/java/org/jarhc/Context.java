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

import org.jarhc.artifacts.Resolver;
import org.jarhc.env.JavaRuntime;
import org.jarhc.java.ClassLoader;

public class Context {

	private final ClassLoader parentClassLoader;
	private final JavaRuntime javaRuntime;
	private final Resolver resolver;

	public Context(ClassLoader parentClassLoader, JavaRuntime javaRuntime, Resolver resolver) {
		this.parentClassLoader = parentClassLoader;
		this.javaRuntime = javaRuntime;
		this.resolver = resolver;
	}

	public ClassLoader getParentClassLoader() {
		return parentClassLoader;
	}

	public JavaRuntime getJavaRuntime() {
		return javaRuntime;
	}

	public Resolver getResolver() {
		return resolver;
	}

}
